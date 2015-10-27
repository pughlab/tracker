package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

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
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.validation.WritableValue;

public class CaseAttributePersistence {
	
	public Map<Class<?>, QCaseAttributeBase<?>> types = new LinkedHashMap<Class<?>, QCaseAttributeBase<?>>();
	
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
	private CaseObjectBuilder getJsonDataBuilder(QueryDslJdbcTemplate template, final Study study, final View view, ListSubQuery<Tuple> caseQuery) {
		
		SQLQuery caseInfoQuery = template.newSqlQuery().from(caseQuery.as(cases));
		List<CaseInfo> caseInfos = template.query(caseInfoQuery, new CaseInfoProjection(cases));
		CaseObjectBuilder builder = new CaseObjectBuilder(caseInfos);
		return builder;
	}
	
	private void addCaseTuples(QueryDslJdbcTemplate template, CaseObjectBuilder builder, final Study study, final View view, ListSubQuery<Tuple> caseQuery) {
		for(Class<?> cls : types.keySet()) {
			
			// We can use raw access to the map here, as we're iterating through the keys
			QCaseAttributeBase<?> atts = types.get(cls);
			
			SQLQuery sqlQuery = template.newSqlQuery()
					.from(caseQuery.as(cases))
					.innerJoin(atts).on(cases.id.eq(atts.caseId))
					.innerJoin(attributes).on(atts.attributeId.eq(attributes.id))
					.innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
					.where(viewAttributes.viewId.eq(view.getId()));
			List<Tuple> values = template.query(sqlQuery, new QTuple(atts.caseId, attributes.name, atts.getValue(), atts.notAvailable, atts.notes));
			builder.addTupleAttributes(values);
		}		
	}
	
	public List<ObjectNode> getJsonData(QueryDslJdbcTemplate template, Study study, View view, ListSubQuery<Tuple> caseQuery) {
		CaseObjectBuilder builder = getJsonDataBuilder(template, study, view, caseQuery);
		addCaseTuples(template, builder, study, view, caseQuery);
		return builder.getCaseObjects();
	}
	
	public List<ObjectNode> getJsonData(QueryDslJdbcTemplate template, Study study, View view, List<? extends Attributes> attributes, ListSubQuery<Tuple> caseQuery) {
		CaseObjectBuilder builder = getJsonDataBuilder(template, study, view, caseQuery);
		List<String> filter = new ArrayList<String>();
		for(Attributes a : attributes) {
			filter.add(a.getName());
		}
		builder.setAttributeNameFilter(filter);
		addCaseTuples(template, builder, study, view, caseQuery);
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
	 * Handles generic writing of a case attribute value.
	 * @param template
	 * @param study
	 * @param view
	 * @param caseValue
	 * @param attribute
	 * @param value
	 */
	public void writeCaseAttributeValue(QueryDslJdbcTemplate template, final Study study, final View view, final Cases caseValue, final Attributes attribute, final WritableValue value) {
		final boolean notAvailable = value.getNotAvailable();
		final Class<?> cls = value.getValueClass();
		final Object storableValue = notAvailable ? null : value.getValue();
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);

		final SQLQuery attributeQuery = template.newSqlQuery()
			.from(attributes)
			.where(attributes.name.eq(attribute.getName()).and(attributes.studyId.eq(study.getId())));
		final Integer attributeId = template.queryForObject(attributeQuery, attributes.id);
		
		if (attributeId == null) {
			throw new RuntimeException("Invalid attribute: " + attribute);
		}

		// First try to update
		long updateCount = template.update(atts, new SqlUpdateCallback() { 
			@SuppressWarnings("unchecked")
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {

				SQLUpdateClause sqlUpdate = sqlUpdateClause.where(atts.caseId.eq(caseValue.getId()).and(atts.attributeId.eq(attributeId)));
				sqlUpdate = sqlUpdate.set(atts.notAvailable, notAvailable);
				sqlUpdate = sqlUpdate.set((Path<Object>)atts.getValuePath(cls), (Object)storableValue);
				return sqlUpdate.execute();
			};
		});
		if (updateCount >= 1) return;
		template.insert(atts, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(atts.caseId, atts.attributeId, atts.getValuePath(cls), atts.notAvailable)
					.values(caseValue.getId(), attributeId, storableValue, notAvailable)
					.execute();
			};
		});
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
	public Object getOldCaseAttributeValue(QueryDslJdbcTemplate template, final Study study, final View view, final Cases caseValue, final String attribute, final Class<?> cls) throws RepositoryException {
		
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		NumberSubQuery<Integer> attributeQuery = new SQLSubQuery()
			.from(attributes)
			.where(attributes.name.eq(attribute).and(attributes.studyId.eq(study.getId())))
			.unique(attributes.id);

		SQLQuery query = template.newSqlQuery().from(cases).innerJoin(atts).on(cases.id.eq(atts.caseId)).where(cases.id.eq(caseValue.getId()).and(atts.attributeId.eq(attributeQuery)));
		Tuple oldValue = template.queryForObject(query, new QTuple(atts.getValue(), atts.notAvailable));
		
    	Object oldRawValue = oldValue == null ? null : oldValue.get(0, cls);
    	Boolean oldNotAvailable = oldValue == null ? false : oldValue.get(1, Boolean.class);
    	if (oldNotAvailable) {
    		return SpecialValues.NOT_AVAILABLE;
    	} else {
    		return oldRawValue;
    	}
	}
}
