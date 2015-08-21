package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.SpecialValues;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBase;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeNumbers;
import ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings;
import ca.uhnresearch.pughlab.tracker.domain.WritableValue;
import ca.uhnresearch.pughlab.tracker.dto.Cases;

public class CaseAttributePersistence {
	
	public Map<Class<?>, QCaseAttributeBase<?>> types = new HashMap<Class<?>, QCaseAttributeBase<?>>();
	
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
	
	public List<ObjectNode> getJsonData(QueryDslJdbcTemplate template, ListSubQuery<Integer> caseQuery) {
		
		SQLQuery caseIdQuery = template.newSqlQuery().from(caseQuery.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);
		CaseObjectBuilder builder = new CaseObjectBuilder(caseIds);

		for(Class<?> cls : types.keySet()) {
			
			// We can use raw access to the map here, as we're iterating through the keys
			QCaseAttributeBase<?> atts = types.get(cls);
			
			SQLQuery sqlQuery = template.newSqlQuery().from(caseQuery.as(cases)).innerJoin(atts).on(cases.id.eq(atts.caseId));
			List<Tuple> values = template.query(sqlQuery, new QTuple(atts.caseId, atts.attribute, atts.getValue(), atts.notAvailable, atts.notes));
			builder.addTupleAttributes(values);
		}
		
		return builder.getCaseObjects();
	}
	
	public void writeCaseAttributeValue(QueryDslJdbcTemplate template, final Cases caseValue, final String attribute, final WritableValue value) {
		final boolean notAvailable = value.getNotAvailable();
		final Class<?> cls = value.getValueClass();
		final Object storableValue = notAvailable ? null : value.getValue();
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		
		long updateCount = template.update(atts, new SqlUpdateCallback() { 
			@SuppressWarnings("unchecked")
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				SQLUpdateClause sqlUpdate = sqlUpdateClause.where(atts.caseId.eq(caseValue.getId()).and(atts.attribute.eq(attribute)));
				sqlUpdate = sqlUpdate.set(atts.notAvailable, notAvailable);
				sqlUpdate = sqlUpdate.set((Path<Object>)atts.getValuePath(cls), (Object)storableValue);
				return sqlUpdate.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(atts, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(atts.caseId, atts.attribute, atts.getValuePath(cls), atts.notAvailable)
					.values(caseValue.getId(), attribute, storableValue, notAvailable)
					.execute();
			};
		});
	}
	
	public Object getOldCaseAttributeValue(QueryDslJdbcTemplate template, final Cases caseValue, final String attribute, final Class<?> cls) throws RepositoryException {
		
		final QCaseAttributeBase<?> atts = getCaseAttribute(cls);
		
		SQLQuery query = template.newSqlQuery().from(cases).innerJoin(atts).on(cases.id.eq(atts.caseId)).where(cases.id.eq(caseValue.getId()).and(atts.attribute.eq(attribute)));
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
