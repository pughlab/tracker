package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import static ca.uhnresearch.pughlab.tracker.domain.QAuditLog.auditLog;
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

	@Override
	public void setStudyViews(Studies study, List<Views> newViewsList) {
		logger.debug("Writing views for study: {}", study.getName());
				
		SQLQuery sqlQuery = template.newSqlQuery().from(views)
    	    .where(views.studyId.eq(study.getId()));

		List<Views> oldViewsList = template.query(sqlQuery, views);
		
		Map<Integer, Views> newViews = new HashMap<Integer, Views>();
		for(Views v : newViewsList) {
			if (v.getId() != null) {
				newViews.put(v.getId(), v);
			} else {
				v.setStudyId(study.getId());
				logger.info("Inserting view: {}", v);
				insertView(v);
			}
		}

		// Here, we should have existing attributes and old attributes to
		// handle.
		for(Views v : oldViewsList) {
			Views newView = newViews.get(v.getId());
			if (newView != null) {
				// We have both old and new -- this is an update!
				updateView(v);
			} else {
				// Old but no new, delete the view, remembering to
				// delete from all attributes too.
				logger.info("Deleting view: {}", v);
				deleteView(v);
			}
		}
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
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public void setStudyView(Studies study, Views view) throws RepositoryException {
		if (study.getId().equals(view.getStudyId())) {
			updateView(view);
		} else {
			throw new NotFoundException("Can't update view for a different study: " + view.getName());
		}
	}

    /**
     * Returns a list of all the attributes for a given view. 
     * @param study
     * @param view
     * @return
     */
	public List<Attributes> getStudyAttributes(Studies study) {
		logger.debug("Looking for study attributes");
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());
		
		logger.info("Executing query: {}", sqlQuery.toString());

    	List<Attributes> attributeList = template.query(sqlQuery, attributes);
    	return attributeList;
	}

	private void insertView(final Views v) {
		template.insert(views, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.populate(v).execute();
			};
		});
	}

	private void updateView(final Views v) {
		template.update(views, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(views.id.eq(v.getId())).populate(v).execute();
			};
		});
	}

	private void deleteView(final Views v) {
		template.delete(views, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(views.id.eq(v.getId())).execute();
			};
		});
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.viewId.eq(v.getId())).execute();
			};
		});
	}

	private void insertAttribute(final Attributes a) {
		template.insert(attributes, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.populate(a).execute();
			};
		});
	}

	private void updateAttribute(final Attributes a) {
		template.update(attributes, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(attributes.id.eq(a.getId())).populate(a).execute();
			};
		});
	}

	private void deleteAttribute(final Attributes a) {
		template.delete(attributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(attributes.id.eq(a.getId())).execute();
			};
		});
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.attributeId.eq(a.getId())).execute();
			};
		});
	}
	
	private void insertViewAttribute(final ViewAttributes va) {
		template.insert(viewAttributes, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.populate(va).execute();
			};
		});
	}

	private void deleteViewAttribute(final ViewAttributes va) {
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.viewId.eq(va.getViewId()).and(viewAttributes.attributeId.eq(va.getAttributeId()))).execute();
			};
		});
	}

	private void updateViewAttribute(final ViewAttributes va) throws RepositoryException {
		template.update(viewAttributes, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(viewAttributes.viewId.eq(va.getViewId()).and(viewAttributes.attributeId.eq(va.getAttributeId()))).populate(va).execute();
			};
		});
	}


	@Override
	public void setStudyAttributes(Studies study, List<Attributes> atts) {
		logger.debug("Updating study attributes");
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());

		List<Attributes> attributeList = template.query(sqlQuery, attributes);
		
		// Now we can go through both lists, checking to see which attributes
		// are to be deleted, which updated, and so on.
		Map<Integer, Attributes> newAttributes = new HashMap<Integer, Attributes>();
		Integer rank = 1;
		for(Attributes a : atts) {
			a.setRank(rank++);
			if (a.getId() != null) {
				newAttributes.put(a.getId(), a);
			} else {
				a.setStudyId(study.getId());
				logger.info("Inserting attribute: {}", a);
				insertAttribute(a);
			}
		}
		
		// Here, we should have existing attributes and old attributes to
		// handle.
		for(Attributes a : attributeList) {
			Attributes newAttribute = newAttributes.get(a.getId());
			if (newAttribute != null) {
				// We have both old and new -- this is an update!
				updateAttribute(a);
			} else {
				// Old but no new, delete the attribute, remembering to
				// delete from all views too.
				logger.info("Deleting attribute: {}", a);
				deleteAttribute(a);
			}
		}
	}

	@Override
	public void setViewAttributes(Studies study, Views view, List<Attributes> newAttributes) throws RepositoryException {
		// First, we need the list of all available attributes in the study.
		List<Attributes> studyAttributes = getStudyAttributes(study);
		Map<Integer, Attributes> studyAttributesTable = new HashMap<Integer, Attributes>();
		for (Attributes a : studyAttributes) {
			studyAttributesTable.put(a.getId(), a);
		}
		
		// Next, we need a list of the current view attributes/
		List<Attributes> oldAttributes = getViewAttributes(study, view);
		
		// Next, build a table of the identifiers
		Map<Integer, Attributes> oldAttributesTable = new HashMap<Integer, Attributes>();
		for(Attributes a : oldAttributes) {
			oldAttributesTable.put(a.getId(), a);
		}
		
		// For each new/existing attribute, we can update the options in the view attribute
		// data. Unmatched old ones will be left in the table and deleted later.
		for(Attributes a : newAttributes) {
			Attributes old = oldAttributesTable.get(a.getId());
			if (old == null) {
				// No old attribute, this is a new one. So first check it exists within
				// the study. If it doesn't, then we can throw an exception.
				Attributes studyAttribute = studyAttributesTable.get(a.getId());
				if (studyAttribute == null) {
					throw new NotFoundException("Missing attribute: " + a.getName());
				}
				
				ViewAttributes va = new ViewAttributes();
				va.setViewId(view.getId());
				va.setAttributeId(studyAttribute.getId());
				va.setOptions(a.getOptions());
				insertViewAttribute(va);
				
			} else {
				// We do have an old attribute as well as a new one, so this is basically
				// an update on the view attribute options. But first we need to locate
				// the existing ViewAttributes.
				
		    	SQLQuery sqlQuery = template.newSqlQuery().from(viewAttributes)
		    		.where(viewAttributes.viewId.eq(view.getId()).and(viewAttributes.attributeId.eq(old.getId())));
		    	ViewAttributes va = template.queryForObject(sqlQuery, viewAttributes);
				va.setOptions(a.getOptions());
				updateViewAttribute(va);
				
				// Mark the old one as seen, so we can delete any left over.
				oldAttributesTable.remove(a.getId());
			}
		}
		
		// Right, now we can simply remove old attributes
		for (Attributes a : oldAttributesTable.values()) {
			ViewAttributes va = new ViewAttributes();
			va.setViewId(view.getId());
			va.setAttributeId(a.getId());
			deleteViewAttribute(va);
		}
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
				obj.replace(attributeName, getNotAvailableValue());
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
	
	private JsonNode getNotAvailableValue() {
		ObjectNode marked = jsonNodeFactory.objectNode();
		marked.put("$notAvailable", Boolean.TRUE);
		return marked;
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

	/**
	 * Retrieves a single value from a case attribute. There is a subtle but important issue here: what is the return value for a
	 * missing attribute. There is a Java null (when it isn't there) and a JSON null, which is there, but is null. Both can happen,
	 * and are to some extent equivalent. One interpretation us that a set to null is actually a deletion. 
	 */
	@Override
	public JsonNode getCaseAttributeValue(Studies study, Views view, Cases caseValue, String attribute) {
		JsonNode caseData = getCaseData(study, view, caseValue);
		return caseData.get(attribute);
	}

	@Override
	public void setCaseAttributeValue(final Studies study, final Views view, final Cases caseValue, final String attribute, final String userName, JsonNode value) throws RepositoryException {
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId())
    	    .and(views.id.eq(view.getId()))
    	    .and(attributes.name.eq(attribute)));
    	Attributes a = template.queryForObject(sqlQuery, attributes);
    	
    	// If there isn't an attribute, we should probably throw an error.
    	if (a == null) {
    		throw new NotFoundException("Can't find attribute: " + attribute);
    	}
    	
    	JsonNode optionsNode = null;
    	String options = a.getOptions();
    	if (options != null) {
			try {
				optionsNode = objectMapper.readTree(options);
			} catch (Exception e) {
				logger.error("Invalid JSON notes: {}, {}", e.getMessage(), a.getOptions());
			}
    	}
    	
    	// Now let's pull the current attribute value, if it exists, mainly so we can generate an audit entry for it.
    	Tuple oldValue;
    	final ObjectNode auditLogValues = jsonNodeFactory.objectNode();

    	if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_STRING) || a.getType().equals(Attributes.ATTRIBUTE_TYPE_OPTION)) {
    		SQLQuery query = template.newSqlQuery().from(cases).innerJoin(caseAttributeStrings).on(cases.id.eq(caseAttributeStrings.caseId)).where(cases.id.eq(caseValue.getId()).and(caseAttributeStrings.attribute.eq(attribute)));
    		oldValue = template.queryForObject(query, new QTuple(caseAttributeStrings.value, caseAttributeStrings.notAvailable));
    	} else if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_DATE)) {
    		SQLQuery query = template.newSqlQuery().from(cases).innerJoin(caseAttributeDates).on(cases.id.eq(caseAttributeDates.caseId)).where(cases.id.eq(caseValue.getId()).and(caseAttributeDates.attribute.eq(attribute)));
    		oldValue = template.queryForObject(query, new QTuple(caseAttributeDates.value, caseAttributeDates.notAvailable));
    	} else if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_BOOLEAN)) {
    		SQLQuery query = template.newSqlQuery().from(cases).innerJoin(caseAttributeBooleans).on(cases.id.eq(caseAttributeBooleans.caseId)).where(cases.id.eq(caseValue.getId()).and(caseAttributeBooleans.attribute.eq(attribute)));
    		oldValue = template.queryForObject(query, new QTuple(caseAttributeBooleans.value, caseAttributeBooleans.notAvailable));    		
    	} else {
    		throw new RuntimeException("Invalid attribute type: " + a.getType());
    	}
    	
    	Object oldRawValue = oldValue.get(0, Object.class);
		Boolean oldNotAvailable = oldValue.get(1, Boolean.class);
		if (oldNotAvailable) {
			auditLogValues.replace("old", getNotAvailableValue());
		} else if (oldRawValue == null) {
			auditLogValues.put("old", (String) oldRawValue);
		} else if (oldRawValue instanceof String) {
			auditLogValues.put("old", (String) oldRawValue);
		} else if (oldRawValue instanceof Date) {
			auditLogValues.put("old", ((Date) oldRawValue).toString());
		} else if (oldRawValue instanceof Boolean) {
			auditLogValues.put("old", (Boolean) oldRawValue);
		} else {
			throw new RuntimeException("Invalid attribute type: " + value.getClass().getCanonicalName());
		}

		// Right. Now we can add the new value. Which is much easier, as it's already encoded.
		auditLogValues.replace("value", value);
		
    	// So here we know what type of attribute we have, and can therefore build a query to
    	// write into the correct table. 
    	    	
    	template.insert(auditLog, new SqlInsertCallback() {
    		public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
    			return sqlInsertClause.columns(auditLog.studyId, auditLog.caseId, auditLog.attribute, auditLog.eventType, auditLog.eventUser, auditLog.eventTime, auditLog.eventArgs)
    				.values(caseValue.getId(), study.getId(), attribute, "set_value", userName, new Timestamp((new java.util.Date()).getTime()), auditLogValues.toString())
    				.execute();
    		};
    	});
    	
    	// And next, we ought to either (a) insert, or (b) update, existing values. There are lots of nasty combinations of
    	// conditions here, because we have several tables and we may need to either insert or update, and of course, to 
    	// figure out which. The easiest is to an attempt an update, and insert if no rows were affected.
    	
    	final Boolean valueNotAvailable = value != null && value.isObject() && value.has("$notAvailable");
    	JsonNode valueNode = valueNotAvailable ? null : value;
    	
    	// Yuck, yuck, yuck! I'd love to do this better, but I can't think of an easy way. The values are of 
    	// different types. Possible multiple inner classes might be one way. 
    	
    	if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_STRING)) {
    		
    		if (valueNode != null && ! valueNode.isTextual()) {
    			throw new InvalidValueException("Invalid string value: " + valueNode.toString());
    		}
    		String finalValue = valueNode == null ? null : valueNode.asText();
    		writeCaseAttributeValue(caseValue, attribute, valueNotAvailable,finalValue);
    		
    	} else if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_OPTION)) {
    		
    		// Option values should also match one of the specified original values from the 
    		// attribute definition.
    		
    		if (optionsNode == null || ! optionsNode.has("values") || ! optionsNode.get("values").isArray()) {
    			throw new InvalidValueException("No option values specified: " + a.getName());
    		}
    		
    		if (valueNode != null) {
        		Boolean found = false;
        		Iterator<JsonNode> elements = optionsNode.get("values").elements();
        		while(elements.hasNext()) {
        			if (elements.next().equals(valueNode)) {
        				found = true;
        				break;
        			}
        		}
        		if (! found) {
        			throw new InvalidValueException("Invalid string value: " + valueNode.toString());
        		}
    		}
    		String finalValue = valueNode == null ? null : valueNode.asText();
    		writeCaseAttributeValue(caseValue, attribute, valueNotAvailable,finalValue);

		} else if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_DATE)) {
			
    		if (valueNode != null && ! valueNode.isTextual()) {
    			throw new InvalidValueException("Invalid date value: " + valueNode.toString());
    		}
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    		try {
				Date finalValue = valueNode == null ? null : new Date(format.parse(valueNode.asText()).getTime());
	    		writeCaseAttributeValue(caseValue, attribute, valueNotAvailable, finalValue);				
			} catch (ParseException e) {
				throw new InvalidValueException("Invalid date value: " + valueNode.toString());
			}
    		
    	} else if (a.getType().equals(Attributes.ATTRIBUTE_TYPE_BOOLEAN)) {
    		
    		if (valueNode != null && ! valueNode.isBoolean()) {
    			throw new InvalidValueException("Invalid boolean value: " + valueNode.toString());
    		}
    		Boolean finalValue = valueNode == null ? null : valueNode.asBoolean();
    		writeCaseAttributeValue(caseValue, attribute, valueNotAvailable, finalValue);
    		
    	}
	}
	
	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final String value) {
		long updateCount = template.update(caseAttributeStrings, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeStrings.caseId.eq(caseValue.getId()).and(caseAttributeStrings.attribute.eq(attribute)))
					.set(caseAttributeStrings.notAvailable, valueNotAvailable)
					.set(caseAttributeStrings.value, valueNotAvailable ? null : value)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeStrings, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeStrings.caseId, caseAttributeStrings.attribute, caseAttributeStrings.value, caseAttributeStrings.notAvailable)
					.values(caseValue.getId(), attribute, valueNotAvailable ? null : value, valueNotAvailable)
					.execute();
			};
		});

	}

	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final Date value) {
		long updateCount = template.update(caseAttributeDates, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeDates.caseId.eq(caseValue.getId()).and(caseAttributeDates.attribute.eq(attribute)))
					.set(caseAttributeDates.notAvailable, valueNotAvailable)
					.set(caseAttributeDates.value, valueNotAvailable ? null : value)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeDates, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeDates.caseId, caseAttributeDates.attribute, caseAttributeDates.value, caseAttributeDates.notAvailable)
					.values(caseValue.getId(), attribute, valueNotAvailable ? null : value, valueNotAvailable)
					.execute();
			};
		});
	}

	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final Boolean value) {
		long updateCount = template.update(caseAttributeBooleans, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeBooleans.caseId.eq(caseValue.getId()).and(caseAttributeBooleans.attribute.eq(attribute)))
					.set(caseAttributeBooleans.notAvailable, valueNotAvailable)
					.set(caseAttributeBooleans.value, valueNotAvailable ? null : value)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeBooleans, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeBooleans.caseId, caseAttributeBooleans.attribute, caseAttributeBooleans.value, caseAttributeBooleans.notAvailable)
					.values(caseValue.getId(), attribute, valueNotAvailable ? null : value, valueNotAvailable)
					.execute();
			};
		});
	}

	@Override
	public List<JsonNode> getAuditData(Studies study, CaseQuery query) {
		
		
		SQLQuery sq = template.newSqlQuery().from(auditLog).where(auditLog.studyId.eq(study.getId())).orderBy(auditLog.eventTime.desc());
		
		if (query.getOffset() != null) {
			sq = sq.offset(query.getOffset());
		}
		if (query.getLimit() != null) {
			sq = sq.limit(query.getLimit());
		}
	
    	List<AuditLog> data = template.query(sq, auditLog);
    	List<JsonNode> result = new ArrayList<JsonNode>();
    	
    	for(AuditLog audit : data) {
    		ObjectNode obj = jsonNodeFactory.objectNode();
    		obj.put("caseId", audit.getCaseId());
    		obj.put("attribute", audit.getAttribute());
    		obj.put("eventTime", audit.getEventTime().toString());
    		obj.put("eventType", audit.getEventType());
    		obj.put("eventUser", audit.getEventUser());
			JsonNode argsNode = null;
			logger.info("Got data: {}", audit.getEventArgs().toString());
			try {
				argsNode = objectMapper.readTree(audit.getEventArgs());
			} catch (Exception e) {
				logger.error("Invalid JSON arguments: {}, {}", e.getMessage(), audit.getEventArgs());
			} finally {
				logger.info("Adding data: {}", argsNode.toString());
				obj.replace("eventArgs", argsNode);
			}

    		result.add(obj);
    	}

		return result;
	}

}

