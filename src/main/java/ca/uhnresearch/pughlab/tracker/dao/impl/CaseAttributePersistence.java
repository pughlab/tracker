package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.expr.TemporalExpression;
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
	 * @param query
	 * @param attributeFilter
	 * @return a list of data objects
	 */
	public List<ObjectNode> getJsonData(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, List<? extends Attributes> attributeFilter) {
		
		final SQLQuery caseInfoQuery = template.newSqlQuery().from(cases).where(cases.id.in(query.getQuery().list(cases.id))).orderBy(cases.order.asc());
		final List<CaseInfo> caseInfos = template.query(caseInfoQuery, new CaseInfoProjection(cases));
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		final CaseObjectBuilder builder = new CaseObjectBuilder(caseInfos);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Selecting cases: {}", caseInfoQuery.toString());
		}
		
		final List<String> filter = new ArrayList<String>();
		for(Attributes a : attributeFilter) {
			filter.add(a.getName());
		}
		builder.setAttributeNameFilter(filter);

		for(Class<?> cls : types.keySet()) {
			
			// We can use raw access to the map here, as we're iterating through the keys
			final QCaseAttributeBase<?> atts = types.get(cls);
			
			final SQLQuery sqlQuery = template.newSqlQuery()
					.from(caseQuery.as(cases))
					.innerJoin(atts).on(cases.id.eq(atts.caseId))
					.innerJoin(attributes).on(atts.attributeId.eq(attributes.id));
			final List<Tuple> values = template.query(sqlQuery, new QTuple(atts.caseId, attributes.name, atts.getValue(), atts.notAvailable, atts.notes));
			builder.addTupleAttributes(values);
		}
		
		return builder.getCaseObjects();
	}
	
	/**
	 * Removes all attribute values associated with a given attribute.
	 * @param template the SQL templates
	 * @param attribute the attribute
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
	
	/**
	 * Builds a filter map from an {@link ObjectNode}.
	 * @param filter the incoming filters
	 * @return a map of keyed {@link JsonNode}s.
	 */
	private Map<String, JsonNode> getFilterMap(ObjectNode filter) {
		final Map<String, JsonNode> filterMap = new HashMap<String, JsonNode>();
		final Iterator<Map.Entry<String,JsonNode>> filterIterator = filter.fields();
		while(filterIterator.hasNext()) {
			final Map.Entry<String,JsonNode> field = filterIterator.next();
			filterMap.put(field.getKey(), field.getValue());
		}

		return filterMap;
	}
	
	public SQLSubQuery filterQuery(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, ObjectNode filter) {
		
		// Work within the given study and query
		final Study study = query.getStudy();
		SQLSubQuery sq = query.getQuery();
		
		final Map<String, JsonNode> filterMap = getFilterMap(filter);

		Integer fIndex = 0;
		
		for(Attributes attribute : getStudyAttributes(template, study)) {
			if (filterMap.containsKey(attribute.getName())) {
				final JsonNode filterJson = filterMap.get(attribute.getName());
				
				// If it's an empty string, completely, (not \"\") then skip the filter
				// Resolves #102
				if (filterJson.isTextual() && filterJson.asText().length() == 0) {
					continue;
				}
				
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
	 * @param template the SQL template
	 * @param study the study
	 * @return a list of attributes
	 */
	private List<Attributes> getStudyAttributes(QueryDslJdbcTemplate template, Study study) {
		final SQLQuery sqlQuery = template.newSqlQuery()
				.from(attributes)
				.where(attributes.studyId.eq(study.getId()));

		return template.query(sqlQuery, new AttributeProjection(attributes));
	}
	
	private QCaseAttributeBase<?> newAlias(QCaseAttributeBase<?> ca, Integer fIndex) throws ReflectiveOperationException {
		final Class<?> caClass = ca.getClass();
		final Constructor<?> caConstructor = caClass.getConstructor(new Class[]{String.class});
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
		final QCaseAttributeBase<?> ca = getStringCaseAttribute(a.getType());
		return getFilterExpression(sq, ca, a, filterJson, fIndex);
	}
	
	/**
	 * Returns a boolean expression that can be used in an attribute filter. The 
	 * nature of the boolean expression is fairly open. 
	 * 
	 * @param sq the input subquery
	 * @param ca the case attribute
	 * @param a the attribute
	 * @param filterJson the filter JSON node
	 * @param fIndex the filter index
	 * @return the filtered subquery
	 */
	private SQLSubQuery getFilterExpression(SQLSubQuery sq, QCaseAttributeBase<?> ca, 
			                                Attributes a, JsonNode filterJson, Integer fIndex) 
			            throws ReflectiveOperationException {
		
		final String filterValue = filterJson.asText();
		
		QueryNode node;
		try {
			final QueryParser parser = queryParserFactory.newQueryParser(filterValue);
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
		final Expression<? super Comparable<?>> filterConstant = new ConstantImpl(caClass, filterValue);
		return filterConstant;
	}
	
	private SQLSubQuery getFilterExpression(SQLSubQuery sq, QCaseAttributeBase<?> ca, Attributes a, QueryNode filterNode, Integer fIndex) throws ReflectiveOperationException {
		
		final QCaseAttributeBase<?> cAlias = newAlias(ca, fIndex);
		final QAttributes attAlias = new QAttributes("flt" + fIndex);
		
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
			logger.error("Can't make filter for: {}", filterNode);
			return BooleanExpression.anyOf();
		}
	}
	
	private BooleanExpression getStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		if (filterValue.contains("*")) {
			return getWildcardStringFilter(cAlias, filterValue);
		} else {
			return getExactStringFilter(cAlias, filterValue);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BooleanExpression getBooleanFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		if (filterValue.toLowerCase().equals("yes")) {
			return cAlias.getValue().eq(new ConstantImpl(Integer.class, 1));
		} else if (filterValue.toLowerCase().equals("no")) {
			return cAlias.getValue().eq(new ConstantImpl(Integer.class, 0));
		} else {
			return BooleanExpression.anyOf();
		}
	}
	
	private BooleanExpression getExactStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		final Class<?> caClass = cAlias.getClass();
		return cAlias.getValue().eq(getFilterConstant(caClass, filterValue));
	}
	
	private BooleanExpression getWildcardStringFilter(QCaseAttributeBase<?> cAlias, String filterValue) {
		final String replacedValue = filterValue.replaceAll("\\*", "%");
		return cAlias.getValue().stringValue().like(replacedValue);
	}
	
	private String getQueryNodeValue(QueryNode filterNode) {
		if (filterNode instanceof QuotedStringToken) {
			final String filterValue = filterNode.toString();
			return filterValue.substring(1, filterValue.length() - 1);
		} else {
			return filterNode.toString();
		}
	}
	
	private Date getFilterDate(QueryNode filterNode) {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		final String filterValue = getQueryNodeValue(filterNode);
		try {
			return new Date(formatter.parse(filterValue).getTime());
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date: " + filterValue);
		}
	}
	
	private BooleanExpression getDateFilter(QCaseAttributeBase<?> cAlias, QueryNode filterNode, OperatorToken operator) {
		final Date filterValue = getFilterDate(filterNode);
		final ComparableExpressionBase<? extends Comparable<?>> value = cAlias.getValue();
		if (value instanceof TemporalExpression) {
			
			@SuppressWarnings("unchecked")
			final TemporalExpression<Date> date = (TemporalExpression<Date>) value;

			if (operator.equals(OperatorToken.OPERATOR_BEFORE)) {
				filterValue.setTime(filterValue.getTime() + 1);
				return date.before(filterValue);
			} else if (operator.equals(OperatorToken.OPERATOR_AFTER)) {
				filterValue.setTime(filterValue.getTime() - 1);
				return date.after(filterValue);
			}
		}
		
		throw new RuntimeException("Invalid date filter: " + filterNode.toString());
	}
	
	private BooleanExpression getExpressionFilter(QCaseAttributeBase<?> cAlias, ExpressionNode filterValue) {
		if (filterValue.getOperator().equals(OperatorToken.OPERATOR_AND)) {
			return getFilter(cAlias, filterValue.getOperandLeft()).and(getFilter(cAlias, filterValue.getOperandRight()));
		} else if (filterValue.getOperator().equals(OperatorToken.OPERATOR_OR) || filterValue.getOperator().equals(OperatorToken.OPERATOR_COMMA)) {
			return getFilter(cAlias, filterValue.getOperandLeft()).or(getFilter(cAlias, filterValue.getOperandRight()));
		} else if (filterValue.getOperator().equals(OperatorToken.OPERATOR_BEFORE) || filterValue.getOperator().equals(OperatorToken.OPERATOR_AFTER)) {
			return getDateFilter(cAlias, filterValue.getOperandRight(), (OperatorToken) filterValue.getOperator());
		} else {
			throw new RuntimeException("Invalid query: " + filterValue.toString());
		}
	}


	private BooleanExpression getValueFilter(QCaseAttributeBase<?> cAlias, ValueToken filterNode) {
		final String filterValue = filterNode.getValue();
		if (filterValue.equals("N/A")) {
			return cAlias.notAvailable.isTrue();
		} else if (filterValue.equals("")) {
			return BooleanExpression.anyOf();
		} else if (cAlias.getClass().equals(QCaseAttributeBooleans.class)) {
			return getBooleanFilter(cAlias, filterValue);
		} else {
			return getStringFilter(cAlias, filterValue);
		}
	}
	
	private BooleanExpression getQuotedStringFilter(QCaseAttributeBase<?> cAlias, QuotedStringToken filterNode) {
		String filterValue = filterNode.getValue();
		
		// Special case for an exact empty string. We allow the empty string only when
		// the underlying representation allows empty strings. See #101
		// If we are dealing with a boolean or a date, the empty string option
		// isn't relevant. 
		
		if (filterValue.equals("\"\"")) {
			
			if (cAlias instanceof QCaseAttributeStrings) {
				return cAlias.getValue().isNull().or(getExactStringFilter(cAlias, ""));
			} else {
				return cAlias.getValue().isNull();
			}
			
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
	 * @param template the query template
	 * @param query the query selector
	 * @param values the set of values to set
	 * @return a list of case change records
	 */
	public List<CaseChangeInfo> setQueryAttributes(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, ObjectNode values) throws RepositoryException {
		
		// Because we need to insert or update per case, we need to map the query to a list of cases
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		final SQLQuery caseIdQuery = template.newSqlQuery().from(caseQuery.as(cases));
		final List<Integer> caseIds = template.query(caseIdQuery, cases.id);
		
		// For each case, we need to find the old values for each object
		final Study study = query.getStudy();
		
		final SQLQuery sqlQuery = template.newSqlQuery()
				.from(attributes)
				.where(attributes.studyId.eq(study.getId()));
		
		final List<Attributes> atts = template.query(sqlQuery, new AttributeProjection(attributes));
		final List<Attributes> filteredAtts = new ArrayList<Attributes>();
		
		for(Attributes a : atts) {
			if (values.has(a.getName())) filteredAtts.add(a);
		}

		final List<ObjectNode> oldValues = getJsonData(template, query, filteredAtts);
		final List<CaseChangeInfo> result = new ArrayList<CaseChangeInfo>();
		
		// Right, now we can do an update and check to see what actually we wanted to change. This
		// should do a check to see whether we need to update, first.
		
		for(Integer caseId : caseIds) {
			final ObjectNode oldCase = oldValues.remove(0);
			final CaseChangeInfo newCaseChange = new CaseChangeInfo(caseId);
			result.add(newCaseChange);
			for(Attributes a : filteredAtts) {
				final String name = a.getName();
				final JsonNode oldValue = oldCase.get(name);
				final JsonNode newValue = values.get(name);
				if (oldValue != null && oldValue.equals(newValue)) continue;
				
				newCaseChange.addValueChange(name, oldValue, newValue);
				
				final ValueValidator validator = AttributeMapper.getAttributeValidator(a.getType());
				final WritableValue value = validator.validate(a, newValue);
				
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
	 * @param query
	 * @param attribute
	 * @param cls
	 * @return an object describing the old case attribute value
	 * @throws RepositoryException
	 */
	public Object getOldCaseAttributeValue(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, final String attribute, final Class<?> cls) throws RepositoryException {
		
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		NumberSubQuery<Integer> attributeQuery = new SQLSubQuery()
			.from(attributes)
			.where(attributes.name.eq(attribute).and(attributes.studyId.eq(query.getStudy().getId())))
			.unique(attributes.id);
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);

		final SQLQuery sq = template.newSqlQuery().from(cases).innerJoin(atts).on(cases.id.eq(atts.caseId)).where(cases.id.in(caseQuery).and(atts.attributeId.eq(attributeQuery)));
		final Tuple oldValue = template.queryForObject(sq, new QTuple(atts.getValue(), atts.notAvailable));
		
		final Object oldRawValue = oldValue == null ? null : oldValue.get(0, cls);
		final Boolean oldNotAvailable = oldValue == null ? false : oldValue.get(1, Boolean.class);
    	if (oldNotAvailable) {
    		return SpecialValues.NOT_AVAILABLE;
    	} else {
    		return oldRawValue;
    	}
	}
	
	/**
	 * Deletes a set of cases and all its associated attribute values.
	 * @param template the JdbcTemplate
	 * @param query the case selection query
	 * @throws RepositoryException
	 */
	public void deleteCases(QueryDslJdbcTemplate template, QueryStudyCaseQuery query) throws RepositoryException {
		
		final SQLQuery caseSelectionQuery = template.newSqlQuery().from(cases).where(cases.id.in(query.getQuery().list(cases.id)));
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting cases: {}", caseSelectionQuery.toString());
		}
		
		for(Class<?> cls : types.keySet()) {
			final QCaseAttributeBase<?> atts = types.get(cls);
			
			template.delete(atts, new SqlDeleteCallback() { 
				public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
					return sqlDeleteClause.where(atts.caseId.in(caseQuery)).execute();
				};
			});
		}

		// MySQL is a bit stupid and can't handle deletions involving subqueries
		// most of the time. Alarmingly, we need to pull out the identifiers and
		// then send them back. 
		final SQLQuery caseIdQuery = template.newSqlQuery()
				.from(caseQuery.as(cases));
		
		final List<Integer> casesIds = template.query(caseIdQuery, cases.id);
	
		template.delete(cases, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(cases.id.in(casesIds)).execute();
			};
		});
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
