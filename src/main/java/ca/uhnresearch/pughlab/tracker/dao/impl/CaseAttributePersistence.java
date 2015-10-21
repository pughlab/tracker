package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;
import com.mysema.query.types.query.NumberSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.SpecialValues;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBase;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeNumbers;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.validation.ValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.WritableValue;

public class CaseAttributePersistence {
	
	public Map<Class<?>, QCaseAttributeBase<?>> types = new LinkedHashMap<Class<?>, QCaseAttributeBase<?>>();
	
	private final JsonNodeFactory factory = JsonNodeFactory.instance;
	
	private QCaseAttributeBase<?> getCaseAttribute(Class<?> cls) {
		if (! types.containsKey(cls)) {
			throw new RuntimeException("Invalid attribute class: " + cls.getName());
		}
		return types.get(cls);
	}

	CaseAttributePersistence() {
		types.put(String.class, QCaseAttributeStrings.caseAttributes);
		types.put(java.sql.Date.class, QCaseAttributeDates.caseAttributes);
		types.put(Boolean.class, QCaseAttributeBooleans.caseAttributes);
		types.put(Double.class, QCaseAttributeNumbers.caseAttributes);
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
	public List<ObjectNode> setQueryAttributes(QueryDslJdbcTemplate template, QueryStudyCaseQuery query, ObjectNode values) throws RepositoryException {
		
		// Because we need to insert or update per case, we need to map the query to a list of cases
		final ListSubQuery<Integer> caseQuery = query.getQuery().list(cases.id);
		SQLQuery caseIdQuery = template.newSqlQuery().from(caseQuery.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);
		
		// For each case, we need to find the old values for each object
		Study study = query.getStudy();
		
		SQLQuery sqlQuery = template.newSqlQuery()
				.from(attributes)
				.where(attributes.studyId.eq(study.getId()));
		
		List<Attributes> atts = template.query(sqlQuery, attributes);
		List<Attributes> filteredAtts = new ArrayList<Attributes>();
		
		for(Attributes a : atts) {
			if (values.has(a.getName())) filteredAtts.add(a);
		}

		List<ObjectNode> oldValues = getJsonData(template, query, filteredAtts);
		List<ObjectNode> result = new ArrayList<ObjectNode>();
		
		// Right, now we can do an update and check to see what actually we wanted to change. This
		// should do a check to see whether we need to update, first.
		
		for(Integer caseId : caseIds) {
			ObjectNode oldCase = oldValues.remove(0);
			ObjectNode newCase = factory.objectNode();
			result.add(newCase);
			for(Attributes a : filteredAtts) {
				String name = a.getName();
				JsonNode oldValue = oldCase.get(name);
				JsonNode newValue = values.get(name);
				if (oldValue.equals(newValue)) continue;
				
				newCase.replace(name, oldValue);
				
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
}
