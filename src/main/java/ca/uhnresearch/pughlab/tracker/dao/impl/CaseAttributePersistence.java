package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;
import com.mysema.query.types.query.NumberSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseChangeInfo;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.SpecialValues;
import ca.uhnresearch.pughlab.tracker.domain.QAttributes;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBase;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeNumbers;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.query.ExpressionNode;
import ca.uhnresearch.pughlab.tracker.query.InvalidTokenException;
import ca.uhnresearch.pughlab.tracker.query.OperatorToken;
import ca.uhnresearch.pughlab.tracker.query.QueryNode;
import ca.uhnresearch.pughlab.tracker.query.QueryParser;
import ca.uhnresearch.pughlab.tracker.query.QueryParserFactory;
import ca.uhnresearch.pughlab.tracker.query.QuotedStringToken;
import ca.uhnresearch.pughlab.tracker.query.SimpleQueryParserFactory;
import ca.uhnresearch.pughlab.tracker.query.ValueToken;
import ca.uhnresearch.pughlab.tracker.validation.ValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.WritableValue;

public class CaseAttributePersistence {
	
	private final Logger logger = LoggerFactory.getLogger(CaseAttributePersistence.class);

	public Map<Class<?>, QCaseAttributeBase<?>> types = new LinkedHashMap<Class<?>, QCaseAttributeBase<?>>();
	
	public Map<String, QCaseAttributeBase<?>> stringTypes = new LinkedHashMap<String, QCaseAttributeBase<?>>();
	
	private QueryParserFactory queryParserFactory = new SimpleQueryParserFactory();
	
	private QCaseAttributeBase<?> getCaseAttribute(Class<?> cls) {
		if (! types.containsKey(cls)) {
			throw new RuntimeException("Invalid attribute class: " + cls.getName());
		}
		return types.get(cls);
	}

	private QCaseAttributeBase<?> getStringCaseAttribute(String type) {
		if (! stringTypes.containsKey(type)) {
			throw new RuntimeException("Invalid attribute type: " + type);
		}
		return stringTypes.get(type);
	}

	CaseAttributePersistence() {
		types.put(String.class, QCaseAttributeStrings.caseAttributes);
		types.put(java.sql.Date.class, QCaseAttributeDates.caseAttributes);
		types.put(Boolean.class, QCaseAttributeBooleans.caseAttributes);
		types.put(Double.class, QCaseAttributeNumbers.caseAttributes);

		stringTypes.put(Attributes.ATTRIBUTE_TYPE_STRING, QCaseAttributeStrings.caseAttributes);
		stringTypes.put(Attributes.ATTRIBUTE_TYPE_OPTION, QCaseAttributeStrings.caseAttributes);
		stringTypes.put(Attributes.ATTRIBUTE_TYPE_DATE, QCaseAttributeDates.caseAttributes);
		stringTypes.put(Attributes.ATTRIBUTE_TYPE_BOOLEAN, QCaseAttributeBooleans.caseAttributes);
		stringTypes.put(Attributes.ATTRIBUTE_TYPE_NUMBER, QCaseAttributeNumbers.caseAttributes);
	}
	
	/**
	 * Builds a set of JSON data structures for the given study, view, and a case query.
	 * attributes.
	 * @param template
	 * @param study
	 * @param view
	 * @param viewAtts
	 * @param caseQuery
	 * @return
	 */
	public List<ObjectNode> getJsonData(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, List<? extends Attributes> attributeFilter) {
		
		SQLQuery caseInfoQuery = template.newSqlQuery().from(cases).where(cases.id.in(query.getQuery().list(cases.id)));
		List<CaseInfo> caseInfos = template.query(caseInfoQuery, new CaseInfoProjection(cases));
		ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		CaseObjectBuilder builder = new CaseObjectBuilder(caseInfos);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Selecting cases: {}", caseInfoQuery.toString());
			for(CaseInfo ci : caseInfos) {
				logger.debug("Selected case: {}", ci.getId());
			}
		}
		
		List<String> filter = new ArrayList<String>();
		for(Attributes a : attributeFilter) {
			filter.add(a.getName());
		}
		builder.setAttributeNameFilter(filter);

		for(Class<?> cls : types.keySet()) {
			
			// We can use raw access to the map here, as we're iterating through the keys
			QCaseAttributeBase<?> atts = types.get(cls);
			
			SQLQuery sqlQuery = template.newSqlQuery()
					.from(caseQuery.as(cases))
					.innerJoin(atts).on(cases.id.eq(atts.caseId))
					.innerJoin(attributes).on(atts.attributeId.eq(attributes.id));
			List<Tuple> values = template.query(sqlQuery, new QTuple(atts.caseId, attributes.name, atts.getValue(), atts.notAvailable, atts.notes));
			builder.addTupleAttributes(values);
		}
		
		return builder.getCaseObjects();
	}
	
	/**
	 * Removes all attribute values associated with a given attribute.
	 */
	public void deleteAllAttributes(QueryDslJdbcTemplate template, final Attributes attribute) {
		for(Class<?> cls : types.keySet()) {
			final QCaseAttributeBase<?> atts = types.get(cls);
			template.delete(atts, new SqlDeleteCallback() { 
				public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
					return sqlDeleteClause.where(atts.attributeId.eq(attribute.getId())).execute();
				};
			});
		}
	}
	
	private Map<String, JsonNode> getFilterMap(ObjectNode filter) {
		Map<String, JsonNode> filterMap = new HashMap<String, JsonNode>();
		Iterator<Map.Entry<String,JsonNode>> filterIterator = filter.fields();
		while(filterIterator.hasNext()) {
			Map.Entry<String,JsonNode> field = filterIterator.next();
			filterMap.put(field.getKey(), field.getValue());
		}

		return filterMap;
	}
	
	public SQLSubQuery filterQuery(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, ObjectNode filter) {
		
		// Work within the given study and query
		Study study = query.getStudy();
		SQLSubQuery sq = query.getQuery();
		
		Map<String, JsonNode> filterMap = getFilterMap(filter);

		Integer fIndex = 0;
		
		for(Attributes attribute : getStudyAttributes(template, study)) {
			if (filterMap.containsKey(attribute.getName())) {
				JsonNode filterJson = filterMap.get(attribute.getName());
				try {
					sq = addFilter(sq, attribute, filterJson, fIndex++);
				} catch (ReflectiveOperationException e) {
					logger.error("Internal error: " + e.getLocalizedMessage());
				}
			}
		}
		
		logger.debug("Filter SQL: {}", sq.toString());
		
		return sq;
	}
	
	/**
	 * Retrieves the attributes for a study, used to derive the filters. 
	 * The easiest way to do this is to pull out all the attributes for
	 * the study, which should be more efficient than repeated queries.
	 * @param template
	 * @param study
	 * @return
	 */
	private List<Attributes> getStudyAttributes(QueryDslJdbcTemplate template, Study study) {
		SQLQuery sqlQuery = template.newSqlQuery()
				.from(attributes)
				.where(attributes.studyId.eq(study.getId()));

		return template.query(sqlQuery, new AttributeProjection(attributes));
	}
	
	private QCaseAttributeBase<?> newAlias(QCaseAttributeBase<?> ca, Integer fIndex) throws ReflectiveOperationException {
		Class<?> caClass = ca.getClass();
		Constructor<?> caConstructor = caClass.getConstructor(new Class[]{String.class});
		return (QCaseAttributeBase<?>) caConstructor.newInstance("flv" + fIndex);
	}
	
	/**
	 * Adds an additional filter criterion to the subquery. Uses careful aliasing 
	 * based on a passed fIndex value, so that each filter can be handled separately.
	 * @param sq
	 * @param a
	 * @param filterValue
	 * @param fIndex
	 * @return
	 */
	private SQLSubQuery addFilter(SQLSubQuery sq, Attributes a, JsonNode filterJson, Integer fIndex) throws ReflectiveOperationException {
		QCaseAttributeBase<?> ca = getStringCaseAttribute(a.getType());
		return getFilterExpression(sq, ca, a, filterJson, fIndex);
	}
	
	/**
	 * Returns a boolean expression that can be used in an attribute filter. The 
	 * nature of the boolean expression is fairly open. 
	 * 
	 * @param cAlias
	 * @param a
	 * @param filterJson
	 * @return
	 */
	private SQLSubQuery getFilterExpression(SQLSubQuery sq, QCaseAttributeBase<?> ca, Attributes a, JsonNode filterJson, Integer fIndex) throws ReflectiveOperationException {
		
		String filterValue = filterJson.asText();
		
		QueryParser parser = queryParserFactory.newQueryParser(filterValue);
		QueryNode node;
		try {
			node = parser.parse();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			return null;
		} catch (InvalidTokenException e) {
			logger.error(e.getLocalizedMessage());
			return null;
		}

		return getFilterExpression(sq, ca, a, node, fIndex);
	}
	
	private Expression<? super Comparable<?>> getFilterConstant(Class<?> caClass, String filterValue) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Expression<? super Comparable<?>> filterConstant = new ConstantImpl(caClass, filterValue);
		return filterConstant;
	}
	
	private SQLSubQuery getFilterExpression(SQLSubQuery sq, QCaseAttributeBase<?> ca, Attributes a, QueryNode filterNode, Integer fIndex) throws ReflectiveOperationException {
		
		QCaseAttributeBase<?> cAlias = newAlias(ca, fIndex);
		QAttributes attAlias = new QAttributes("flt" + fIndex);
		
		return sq.innerJoin(attAlias)
			.on(attAlias.id.eq(a.getId()).and(attAlias.studyId.eq(cases.studyId)))
			.leftJoin(cAlias)
			.on(cAlias.attributeId.eq(attAlias.id).and(cAlias.caseId.eq(cases.id)))
			.where(getFilter(cAlias, filterNode));
		
	}
	
	private BooleanExpression getFilter(QCaseAttributeBase<?> cAlias, QueryNode filterNode) {
		if (filterNode instanceof ValueToken) {
			return getValueFilter(cAlias, (ValueToken) filterNode);
		} else if (filterNode instanceof QuotedStringToken) {
			return getQuotedStringFilter(cAlias, (QuotedStringToken) filterNode);
		} else if (filterNode instanceof ExpressionNode) {
			return getExpressionFilter(cAlias, (ExpressionNode) filterNode);
		} else {
			throw new RuntimeException("Can't make filter");
		}
	}
	
	private BooleanExpression getStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		if (filterValue.contains("*")) {
			return getWildcardStringFilter(cAlias, filterValue);
		} else {
			return getExactStringFilter(cAlias, filterValue);
		}
	}
	
	private BooleanExpression getExactStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		Class<?> caClass = cAlias.getClass();
		return cAlias.getValue().eq(getFilterConstant(caClass, filterValue));
	}
	
	private BooleanExpression getWildcardStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		filterValue = filterValue.replaceAll("\\*", "%");
		return cAlias.getValue().stringValue().like(filterValue);
	}
	
	private BooleanExpression getExpressionFilter(QCaseAttributeBase<?> cAlias, ExpressionNode filterValue) {
		if (filterValue.getOperator().equals(OperatorToken.OPERATOR_AND)) {
			return getFilter(cAlias, filterValue.getOperandLeft()).and(getFilter(cAlias, filterValue.getOperandRight()));
		} else {
			return getFilter(cAlias, filterValue.getOperandLeft()).or(getFilter(cAlias, filterValue.getOperandRight()));
		}
	}


	private BooleanExpression getValueFilter(QCaseAttributeBase<?> cAlias, ValueToken filterNode) {
		String filterValue = filterNode.getValue();
		if (filterValue.equals("N/A")) {
			return cAlias.notAvailable.isTrue();
		} else {
			return getStringFilter(cAlias, filterValue);
		}
	}
	
	private BooleanExpression getQuotedStringFilter(QCaseAttributeBase<?> cAlias, QuotedStringToken filterNode) {
		String filterValue = filterNode.getValue();
		Class<?> caClass = cAlias.getClass();
		if (filterValue.equals("\"\"")) {
			return cAlias.getValue().isNull().or(cAlias.getValue().eq(getFilterConstant(caClass, "")));
		} else {
			filterValue = filterValue.substring(1, filterValue.length() - 1);
			return getStringFilter(cAlias, filterValue);
		}
	}
	
	/**
	 * Updates a set of objects, possibly for a set of values. This seems like a performance
	 * issue, but the reality is not so, as the query will almost invariably be a single
	 * case, and updating a single value. The result is a list of objects corresponding to
	 * the query, but with modified values returned. If we get back a list of values where
	 * there are no keys, nothing changed. 
	 * 
	 * @param template
	 * @param query
	 * @param values
	 * @return
	 */
	public List<CaseChangeInfo> setQueryAttributes(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, ObjectNode values) throws RepositoryException {
		
		// Because we need to insert or update per case, we need to map the query to a list of cases
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		SQLQuery caseIdQuery = template.newSqlQuery().from(caseQuery.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);
		
		// For each case, we need to find the old values for each object
		Study study = query.getStudy();
		
		SQLQuery sqlQuery = template.newSqlQuery()
				.from(attributes)
				.where(attributes.studyId.eq(study.getId()));
		
		List<Attributes> atts = template.query(sqlQuery, new AttributeProjection(attributes));
		List<Attributes> filteredAtts = new ArrayList<Attributes>();
		
		for(Attributes a : atts) {
			if (values.has(a.getName())) filteredAtts.add(a);
		}

		List<ObjectNode> oldValues = getJsonData(template, query, filteredAtts);
		List<CaseChangeInfo> result = new ArrayList<CaseChangeInfo>();
		
		// Right, now we can do an update and check to see what actually we wanted to change. This
		// should do a check to see whether we need to update, first.
		
		for(Integer caseId : caseIds) {
			ObjectNode oldCase = oldValues.remove(0);
			CaseChangeInfo newCaseChange = new CaseChangeInfo(caseId);
			result.add(newCaseChange);
			for(Attributes a : filteredAtts) {
				String name = a.getName();
				JsonNode oldValue = oldCase.get(name);
				JsonNode newValue = values.get(name);
				if (oldValue != null && oldValue.equals(newValue)) continue;
				
				newCaseChange.addValueChange(name, oldValue, newValue);
				
				ValueValidator validator = AttributeMapper.getAttributeValidator(a.getType());
				WritableValue value = validator.validate(a, newValue);
				
				// Now we can do the actual update...
				writeCaseAttributeValue(template, study, caseId, name, value);
			}
		}
		
		return result;
	}
	
	/**
	 * Handles generic writing of a case attribute value.
	 * @param template
	 * @param study
	 * @param view
	 * @param caseValue
	 * @param attribute
	 * @param value
	 */
	private void writeCaseAttributeValue(QueryDslJdbcTemplate template, final Study study, final Integer caseId, final String attribute, final WritableValue value) {
		final boolean notAvailable = value.getNotAvailable();
		final Class<?> cls = value.getValueClass();
		final Object storableValue = notAvailable ? null : value.getValue();
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		
		final SQLQuery attributeQuery = template.newSqlQuery()
			.from(attributes)
			.where(attributes.name.eq(attribute).and(attributes.studyId.eq(study.getId())));
		final Integer attributeId = template.queryForObject(attributeQuery, attributes.id);
		
		if (attributeId == null) {
			throw new RuntimeException("Invalid attribute: " + attribute);
		}

		// First try to update
		long updateCount = template.update(atts, new SqlUpdateCallback() { 
			@SuppressWarnings("unchecked")
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {

				SQLUpdateClause sqlUpdate = sqlUpdateClause.where(atts.caseId.eq(caseId).and(atts.attributeId.eq(attributeId)));
				sqlUpdate = sqlUpdate.set(atts.notAvailable, notAvailable);
				sqlUpdate = sqlUpdate.set((Path<Object>)atts.getValuePath(cls), (Object)storableValue);
				return sqlUpdate.execute();
			};
		});
		if (updateCount >= 1) return;
		updateCount = template.insert(atts, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(atts.caseId, atts.attributeId, atts.getValuePath(cls), atts.notAvailable)
					.values(caseId, attributeId, storableValue, notAvailable)
					.execute();
			};
		});
		if (updateCount == 1) return;
		
		// If we get here, all inserts and updates failed
		throw new RuntimeException("Failed to write attribute value: " + attribute);
	}
	
	/**
	 * Retrieves an old case attribute value.
	 * @param template
	 * @param study
	 * @param view
	 * @param caseValue
	 * @param attribute
	 * @param cls
	 * @return
	 * @throws RepositoryException
	 */
	public Object getOldCaseAttributeValue(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, final String attribute, final Class<?> cls) throws RepositoryException {
		
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		NumberSubQuery<Integer> attributeQuery = new SQLSubQuery()
			.from(attributes)
			.where(attributes.name.eq(attribute).and(attributes.studyId.eq(query.getStudy().getId())))
			.unique(attributes.id);
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);

		SQLQuery sq = template.newSqlQuery().from(cases).innerJoin(atts).on(cases.id.eq(atts.caseId)).where(cases.id.in(caseQuery).and(atts.attributeId.eq(attributeQuery)));
		Tuple oldValue = template.queryForObject(sq, new QTuple(atts.getValue(), atts.notAvailable));
		
    	Object oldRawValue = oldValue == null ? null : oldValue.get(0, cls);
    	Boolean oldNotAvailable = oldValue == null ? false : oldValue.get(1, Boolean.class);
    	if (oldNotAvailable) {
    		return SpecialValues.NOT_AVAILABLE;
    	} else {
    		return oldRawValue;
    	}
	}

	/**
	 * @return the queryParserFactory
	 */
	public QueryParserFactory getQueryParserFactory() {
		return queryParserFactory;
	}

	/**
	 * @param queryParserFactory the queryParserFactory to set
	 */
	public void setQueryParserFactory(QueryParserFactory queryParserFactory) {
		this.queryParserFactory = queryParserFactory;
	}
}
