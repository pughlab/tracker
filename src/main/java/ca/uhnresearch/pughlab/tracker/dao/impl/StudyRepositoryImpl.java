package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans.caseAttributeBooleans;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates.caseAttributeDates;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings.caseAttributeStrings;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;
import static ca.uhnresearch.pughlab.tracker.domain.QStudies.studies;
import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;
import static ca.uhnresearch.pughlab.tracker.domain.QViews.views;

// Don't warn about the Law of Demeter here, as we use extensive method chaining in
// the Querydsl implementation. And I do mean extensive. 
@SuppressWarnings("PMD.LawOfDemeter")
public class StudyRepositoryImpl implements StudyRepository {
	
	private final Logger logger = LoggerFactory.getLogger(StudyRepositoryImpl.class);
	
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	private QueryDslJdbcTemplate template;

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Returns a list of studies
     * @param study
     * @return
     */
    public List<Studies> getAllStudies() {
		logger.debug("Looking for all studies");

    	SQLQuery sqlQuery = template.newSqlQuery().from(studies);
    	List<Studies> studyList = template.query(sqlQuery, studies);
    	logger.info("Got some studies: {}", studyList.toString());

    	return studyList;
    }

    /**
     * Returns a named study
     * @param study
     * @return
     */
	public Studies getStudy(String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(studies).where(studies.name.eq(name));
    	Studies study = template.queryForObject(sqlQuery, studies);
    	
    	if (study != null) {
    		logger.info("Got a study: {}", study.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return study;
	}

    /**
     * Returns the list of views associated with a study
     * @param study
     * @return
     */
	public List<Views> getStudyViews(Studies study) {
		logger.debug("Looking for views for study: {}", study.getName());
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.studyId.eq(study.getId()));
    	List<Views> viewList = template.query(sqlQuery, views);
    	logger.info("Got some views: {}", viewList.toString());

		return viewList;
	}

    /**
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public Views getStudyView(Studies study, String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.name.eq(name).and(views.studyId.eq(study.getId())));
    	Views view = template.queryForObject(sqlQuery, views);
    	
    	if (view != null) {
    		logger.info("Got a view: {}", view.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return view;
	}

    /**
     * Returns a list of all the attributes for a given view. 
     * @param study
     * @param view
     * @return
     */
	public List<Attributes> getViewAttributes(Studies study, Views view) {
		logger.debug("Looking for view attributes");
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId()).and(views.id.eq(view.getId())))
    	    .orderBy(viewAttributes.rank.asc());
		
		logger.info("Executing query: {}", sqlQuery.toString());

    	List<Attributes> attributeList = template.query(sqlQuery, attributes);
    	return attributeList;
	}

	/**
	 * Generates an SQLQuery on cases from a CaseQuery object. This can then be incorporated
	 * into the queries that are used to access data.
	 * @param query
	 * @return
	 */
	private ListSubQuery<Integer> getStudySubQueryCaseQuery(Studies study, CaseQuery query) {
		assert query != null;
		
		SQLSubQuery sq = new SQLSubQuery().from(cases).where(cases.studyId.eq(study.getId()));
		
		// If we have an ordering, use a left join to get the attribute, and order it later
		if (query.getOrderField() != null) {
			QCaseAttributeStrings c = new QCaseAttributeStrings("c");
			sq = sq.leftJoin(c).on(c.caseId.eq(cases.id).and(c.attribute.eq(query.getOrderField())));
			OrderSpecifier<String> ordering = (query.getOrderDirection() == CaseQuery.OrderDirection.ASC) ? c.value.asc() : c.value.desc();
			sq = sq.orderBy(ordering);
		}
		
		if (query.getOffset() != null) {
			sq = sq.offset(query.getOffset());
		}
		if (query.getLimit() != null) {
			sq = sq.limit(query.getLimit());
		}
	
		return sq.list(cases.id);
	}
	
	/**
	 * Writes values from a set of data tuples retrieved with a very specific order within getData,
	 * and wires them into JSON values for returning. 
	 * @param table
	 * @param values
	 */
	private void writeTupleAttributes(Map<Integer, ObjectNode> table, List<Tuple> values) {
		for(Tuple v : values) {
			Integer caseId = v.get(0, Integer.class);
			String attributeName = v.get(1, String.class);
			Object value = v.get(2, Object.class);
			Boolean notAvailable = v.get(3, Boolean.class);
			String notes = v.get(4, String.class);
			
			ObjectNode obj = table.get(caseId);
			assert obj != null;
			
			// Add the case identifier
			obj.put("id", caseId);
			
			// Add the value
			if (notAvailable != null && notAvailable) {
				ObjectNode marked = jsonNodeFactory.objectNode();
				marked.put("$notAvailable", Boolean.TRUE);
				obj.replace(attributeName, marked);
			} else if (value == null) {
				obj.put(attributeName, (String) null);
			} else if (value instanceof String) {
				obj.put(attributeName, (String) value);
			} else if (value instanceof Date) {
				obj.put(attributeName, ((Date) value).toString());
			} else if (value instanceof Boolean) {
				obj.put(attributeName, (Boolean) value);
			} else {
				throw new RuntimeException("Invalid attribute type: " + value.getClass().getCanonicalName());
			}
			
			// If we have notes, we need to add them. They are added in a per-record
			// holder attribute, $notes, and need to be decoded from JSON. 
			if (notes != null) {
				JsonNode notesNode = null;
				try {
					notesNode = objectMapper.readTree(notes);
				} catch (Exception e) {
					logger.error("Invalid JSON notes: {}, {}", e.getMessage(), notes);
				} finally {
					ObjectNode recordNotesNode;
					
					if (! obj.has("$notes")) {
						recordNotesNode = jsonNodeFactory.objectNode();
						obj.set("$notes", recordNotesNode);
					} else {
						recordNotesNode = (ObjectNode) obj.get("$notes");
					}
					if (notesNode != null) {
						recordNotesNode.set(attributeName, notesNode);
					}
				}
			}
		}
	}
	
	private List<JsonNode> getJsonData(ListSubQuery<Integer> query) {
		Map<Integer, ObjectNode> table = new HashMap<Integer, ObjectNode>();
		
		SQLQuery caseIdQuery = template.newSqlQuery().from(query.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);

		List<JsonNode> objects = new ArrayList<JsonNode>(caseIds.size());
		
		Integer index = 0;
		for(Integer id : caseIds) {
			ObjectNode obj = jsonNodeFactory.objectNode();
			objects.add(index++, (JsonNode) obj);
			table.put(id, obj);
		}
		
		// Right. Now we can add in the attributes from a set of related queries, using the same basic
		// case query as a starting point. Yes, we're re-doing this query more times than I'd like, but
		// we can optimize later.
		
		// Sadly, we are returning all attributes rather than just the ones that are accessible. In
		// effect, we don't use attributes as a filter. 
				
		SQLQuery sqlQuery;
		List<Tuple> values;
		
		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeStrings).on(cases.id.eq(caseAttributeStrings.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeStrings.caseId, caseAttributeStrings.attribute, caseAttributeStrings.value, caseAttributeStrings.notAvailable, caseAttributeStrings.notes));
		writeTupleAttributes(table, values);

		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeDates).on(cases.id.eq(caseAttributeDates.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeDates.caseId, caseAttributeDates.attribute, caseAttributeDates.value, caseAttributeDates.notAvailable, caseAttributeDates.notes));
		writeTupleAttributes(table, values);

		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeBooleans).on(cases.id.eq(caseAttributeBooleans.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeBooleans.caseId, caseAttributeBooleans.attribute, caseAttributeBooleans.value, caseAttributeBooleans.notAvailable, caseAttributeBooleans.notes));
		writeTupleAttributes(table, values);

		return objects;

	}
	
	/**
	 * Main method for extracting record-level case data into something that can be returned. Here
	 * the logic is very schemaless, so this method returns a list of Jackson JsonNode instances,
	 * rather than anything more structured. This can typically be sent straight back to the client
	 * as a response, without needing DTO mediation. 
	 * 
	 * Perhaps most interesting is the CaseQuery, which is a structured version of offsets, limits,
	 * filters, sort orders, and so on.
	 * 
	 * @param study
	 * @param view
	 * @param attributes
	 * @param query
	 */
	public List<JsonNode> getData(Studies study, Views view, List<Attributes> attributes, CaseQuery query) {
		// This method retrieves the attributes we needed. In most implementations, we've done 
		// this as a UNION in SQL and accepted dynamic types. We probably can't assume this, and
		// since UNIONs generally aren't indexable, we are probably genuinely better off running
		// separate queries for each primitive attribute type, and then assembling them in this
		// method. This hugely reduces the complexity of the DSL here too. 
		
		ListSubQuery<Integer> caseQuery = getStudySubQueryCaseQuery(study, query);
		return getJsonData(caseQuery);
	}

	/**
	 * Returns the record count for a study).
	 */
	@Override
	public Long getRecordCount(Studies study, Views view) {
		SQLQuery recordQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()));
		return template.count(recordQuery);
	}

	/**
	 * Generates an SQLQuery on cases from a single case identifier. This can then be incorporated
	 * into the queries that are used to access data. Note that even for a single case this returns a
	 * list, because that way we can re-use the tuple data management. 
	 * @param query
	 * @return
	 */
	private ListSubQuery<Integer> getStudyCaseSubQuery(Studies study, Integer caseId) {
		SQLSubQuery sq = new SQLSubQuery().from(cases).where(cases.studyId.eq(study.getId()).and(cases.id.eq(caseId)));
		return sq.list(cases.id);
	}
	

	/**
	 * Returns the case data for a single study entity, in JSON format.
	 */
	@Override
	public JsonNode getCaseData(Studies study, Views view, Cases caseValue) {
		ListSubQuery<Integer> caseQuery = getStudyCaseSubQuery(study, caseValue.getId());
		List<JsonNode> listData = getJsonData(caseQuery);
		if (listData.size() == 1) {
			return listData.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public Cases getStudyCase(Studies study, Views view, Integer caseId) {
		logger.debug("Looking for case by identifier: {}", caseId);
    	SQLQuery sqlQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()).and(cases.id.eq(caseId)));
    	Cases caseValue = template.queryForObject(sqlQuery, cases);
    	
    	if (caseValue != null) {
    		logger.info("Got a case: {}", caseValue.toString());
    	} else {
    		logger.info("No case found");
    	}
    	
    	return caseValue;
	}

	@Override
	public JsonNode getCaseAttributeValue(Studies study, Views view,
			Cases caseValue, String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCaseAttributeValue(Studies study, Views view,
			Cases caseValue, String attribute, JsonNode value) {
		// TODO Auto-generated method stub
		
	}
}

