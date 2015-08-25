package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlInsertWithKeyCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;
import ca.uhnresearch.pughlab.tracker.events.UpdateEventService;
import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;
import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;
import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;
import static ca.uhnresearch.pughlab.tracker.domain.QView.views;

// Don't warn about the Law of Demeter here, as we use extensive method chaining in
// the Querydsl implementation. And I do mean extensive. 
@SuppressWarnings("PMD.LawOfDemeter")
public class StudyRepositoryImpl implements StudyRepository {
	
	private final Logger logger = LoggerFactory.getLogger(StudyRepositoryImpl.class);
	
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
		
	private UpdateEventService manager;

	private QueryDslJdbcTemplate template;
	
	private AuditLogRepository auditLogRepository;
	
	private CaseAttributePersistence cap = new CaseAttributePersistence();

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    public QueryDslJdbcTemplate getTemplate() {
        return template;
    }

	/**
     * Returns a list of studies
     * @param study
     * @return
     */
    public List<Study> getAllStudies() {
		logger.debug("Looking for all studies");

    	SQLQuery sqlQuery = template.newSqlQuery().from(studies);
    	List<Study> studyList = template.query(sqlQuery, studies);
    	logger.debug("Got some studies: {}", studyList.toString());

    	return studyList;
    }

    /**
     * Returns a named study
     * @param study
     * @return
     */
	public Study getStudy(String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(studies).where(studies.name.eq(name));
    	Study study = template.queryForObject(sqlQuery, studies);
    	
    	if (study != null) {
    		logger.debug("Got a study: {}", study.toString());
    	} else {
    		logger.debug("No study found");
    	}
    	
    	return study;
	}

    /**
     * Returns the list of views associated with a study
     * @param study
     * @return
     */
	public List<View> getStudyViews(Study study) {
		logger.debug("Looking for views for study: {}", study.getName());
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.studyId.eq(study.getId()));
    	List<View> viewList = template.query(sqlQuery, new ViewProjection(views));
    	logger.debug("Got some views: {}", viewList.toString());

		return viewList;
	}

	@Override
	public void setStudyViews(Study study, List<View> newViewsList) {
		logger.debug("Writing views for study: {}", study.getName());
				
		SQLQuery sqlQuery = template.newSqlQuery().from(views)
    	    .where(views.studyId.eq(study.getId()));

		List<View> oldViewsList = template.query(sqlQuery, new ViewProjection(views));		

		Map<Integer, View> newViews = new HashMap<Integer, View>();
		for(View v : newViewsList) {
			if (v.getId() != null) {
				newViews.put(v.getId(), v);
			} else {
				v.setStudyId(study.getId());
				logger.debug("Inserting view: {}", v);
				insertView(v);
			}
		}

		// Here, we should have existing attributes and old attributes to
		// handle.
		for(View v : oldViewsList) {
			View newView = newViews.get(v.getId());
			if (newView != null) {
				// We have both old and new -- this is an update!
				updateView(newView);
			} else {
				// Old but no new, delete the view, remembering to
				// delete from all attributes too.
				logger.debug("Deleting view: {}", v);
				deleteView(v);
			}
		}
	}

	/**
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public View getStudyView(Study study, String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.name.eq(name).and(views.studyId.eq(study.getId())));
    	View view = template.queryForObject(sqlQuery, new ViewProjection(views));

    	if (view != null) {
    		logger.debug("Got a view: {}", view.toString());
    	} else {
    		logger.debug("No study found");
    	}
    	
    	return view;
	}

	/**
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public void setStudyView(Study study, View view) throws RepositoryException {
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
	public List<Attributes> getStudyAttributes(Study study) {
		logger.debug("Looking for study attributes");
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());
		
		logger.debug("Executing query: {}", sqlQuery.toString());

    	List<Attributes> attributeList = template.query(sqlQuery, new AttributeProjection(attributes));
    	return attributeList;
	}

	private void insertView(final View v) {
		template.insert(views, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.populate(v, new ViewMapper()).execute();
			};
		});
	}

	private void updateView(final View v) {
		template.update(views, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(views.id.eq(v.getId())).populate(v, new ViewMapper()).execute();
			};
		});
	}

	private void deleteView(final View v) {
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
				return sqlInsertClause.populate(a, new AttributeMapper()).execute();
			};
		});
	}

	private void updateAttribute(final Attributes a) {
		template.update(attributes, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(attributes.id.eq(a.getId())).populate(a, new AttributeMapper()).execute();
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
	
	private void insertViewAttribute(final View v, final ViewAttributes va) {
		template.insert(viewAttributes, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.populate(va, new ViewAttributeMapper(v)).execute();
			};
		});
	}

	private void deleteViewAttribute(final View v, final ViewAttributes va) {
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.viewId.eq(v.getId()).and(viewAttributes.attributeId.eq(va.getId()))).execute();
			};
		});
	}

	private void updateViewAttribute(final View v, final ViewAttributes va) throws RepositoryException {
		template.update(viewAttributes, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(viewAttributes.viewId.eq(v.getId()).and(viewAttributes.attributeId.eq(va.getId()))).populate(va, new ViewAttributeMapper(v)).execute();
			};
		});
	}


	@Override
	public void setStudyAttributes(Study study, List<Attributes> atts) {
		logger.debug("Updating study attributes");
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());

		List<Attributes> attributeList = template.query(sqlQuery, new AttributeProjection(attributes));
		
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
				logger.debug("Inserting attribute: {}", a);
				insertAttribute(a);
			}
		}
		
		// Here, we should have existing attributes and old attributes to
		// handle.
		for(Attributes a : attributeList) {
			Attributes newAttribute = newAttributes.get(a.getId());
			if (newAttribute != null) {
				// We have both old and new -- this is an update!
				updateAttribute(newAttribute);
			} else {
				// Old but no new, delete the attribute, remembering to
				// delete from all views too.
				logger.debug("Deleting attribute: {}", a);
				deleteAttribute(a);
			}
		}
	}

	@Override
	public void setViewAttributes(Study study, View view, List<ViewAttributes> newAttributes) throws RepositoryException {
		// First, we need the list of all available attributes in the study.
		List<Attributes> studyAttributes = getStudyAttributes(study);
		Map<Integer, Attributes> studyAttributesTable = new HashMap<Integer, Attributes>();
		for (Attributes a : studyAttributes) {
			studyAttributesTable.put(a.getId(), a);
		}
		
		// Next, we need a list of the current view attributes/
		List<ViewAttributes> oldAttributes = getViewAttributes(study, view);
		
		// Next, build a table of the identifiers
		Map<Integer, Attributes> oldAttributesTable = new HashMap<Integer, Attributes>();
		for(Attributes a : oldAttributes) {
			oldAttributesTable.put(a.getId(), a);
		}
		
		// For each new/existing attribute, we can update the options in the view attribute
		// data. Unmatched old ones will be left in the table and deleted later.
		Integer rank = 0;
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
				va.setId(studyAttribute.getId());
				va.setRank(rank);
				va.setViewOptions(a.getOptions());
				insertViewAttribute(view, va);
				
			} else {
				// We do have an old attribute as well as a new one, so this is basically
				// an update on the view attribute options. But first we need to locate
				// the existing ViewAttributes.
				
		    	SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
		    		.innerJoin(viewAttributes)
		    		.on(attributes.id.eq(viewAttributes.attributeId))
		    		.where(viewAttributes.viewId.eq(view.getId()).and(viewAttributes.attributeId.eq(old.getId())));
		    	ViewAttributes va = template.queryForObject(sqlQuery, new ViewAttributeProjection(attributes, viewAttributes));
				va.setViewOptions(a.getOptions());
				va.setRank(rank);
				updateViewAttribute(view, va);
				
				// Mark the old one as seen, so we can delete any left over.
				oldAttributesTable.remove(a.getId());
			}
			rank++;
		}
		
		// Right, now we can simply remove old attributes
		for (Attributes a : oldAttributesTable.values()) {
			ViewAttributes va = new ViewAttributes();
			va.setId(a.getId());
			deleteViewAttribute(view, va);
		}
	}

    /**
     * Returns a list of all the attributes for a given view. 
     * @param study
     * @param view
     * @return
     */
	public List<ViewAttributes> getViewAttributes(Study study, View view) {
		
		logger.debug("Looking for view attributes");
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId()).and(views.id.eq(view.getId())))
    	    .orderBy(viewAttributes.rank.asc());
		
    	List<ViewAttributes> attributeList = template.query(sqlQuery, new ViewAttributeProjection(attributes, viewAttributes));

		return attributeList;
	}

	/**
	 * Generates an SQLQuery on cases from a CaseQuery object. This can then be incorporated
	 * into the queries that are used to access data.
	 * @param query
	 * @return
	 */
	private ListSubQuery<Integer> getStudySubQueryCaseQuery(Study study, CaseQuery query) {
		
		SQLSubQuery sq = new SQLSubQuery().from(cases).where(cases.studyId.eq(study.getId()));
		
		// If we have an ordering, use a left join to get the attribute, and order it later
		if (query.getOrderField() != null) {
			QCaseAttributeStrings c = new QCaseAttributeStrings("c");
			sq = sq.leftJoin(c).on(c.caseId.eq(cases.id).and(c.attribute.eq(query.getOrderField())));
			OrderSpecifier<?> ordering = c.getValueOrderSpecifier(query.getOrderDirection() == CaseQuery.OrderDirection.ASC);
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
	public List<ObjectNode> getData(Study study, View view, List<ViewAttributes> attributes, CaseQuery query) {
		// This method retrieves the attributes we needed. In most implementations, we've done 
		// this as a UNION in SQL and accepted dynamic types. We probably can't assume this, and
		// since UNIONs generally aren't indexable, we are probably genuinely better off running
		// separate queries for each primitive attribute type, and then assembling them in this
		// method. This hugely reduces the complexity of the DSL here too. 
		
		ListSubQuery<Integer> caseQuery = getStudySubQueryCaseQuery(study, query);
		return cap.getJsonData(template, caseQuery);
	}

	/**
	 * Returns the record count for a study).
	 */
	@Override
	public Long getRecordCount(Study study, View view) {
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
	private ListSubQuery<Integer> getStudyCaseSubQuery(Study study, Integer caseId) {
		SQLSubQuery sq = new SQLSubQuery().from(cases).where(cases.studyId.eq(study.getId()).and(cases.id.eq(caseId)));
		return sq.list(cases.id);
	}
	

	/**
	 * Returns the case data for a single study entity, in JSON format.
	 */
	@Override
	public ObjectNode getCaseData(Study study, View view, Cases caseValue) {
		ListSubQuery<Integer> caseQuery = getStudyCaseSubQuery(study, caseValue.getId());
		List<ObjectNode> listData = cap.getJsonData(template, caseQuery);
		if (listData.size() == 1) {
			return listData.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public Cases getStudyCase(Study study, View view, Integer caseId) {
		logger.debug("Looking for case by identifier: {}", caseId);
    	SQLQuery sqlQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()).and(cases.id.eq(caseId)));
    	Cases caseValue = template.queryForObject(sqlQuery, cases);
    	
    	if (caseValue != null) {
    		logger.debug("Got a case: {}", caseValue.toString());
    	} else {
    		logger.debug("No case found");
    	}
    	
    	return caseValue;
	}

	/**
	 * Retrieves a single value from a case attribute. There is a subtle but important issue here: what is the return value for a
	 * missing attribute. There is a Java null (when it isn't there) and a JSON null, which is there, but is null. Both can happen,
	 * and are to some extent equivalent. One interpretation us that a set to null is actually a deletion. 
	 */
	@Override
	public JsonNode getCaseAttributeValue(Study study, View view, Cases caseValue, String attribute) {
		JsonNode caseData = getCaseData(study, view, caseValue);
		return caseData.get(attribute);
	}
	
	
	private JsonNode getJsonValue(Object value) {
		if (value == null) {
			return jsonNodeFactory.nullNode();
		} else if (value instanceof JsonNode) {
			return (JsonNode) value;
		} else if (value instanceof String) {
			return jsonNodeFactory.textNode((String) value);
		} else if (value instanceof Boolean) {
			return jsonNodeFactory.booleanNode((Boolean) value);
		} else if (value instanceof java.sql.Date) {
			return jsonNodeFactory.textNode((String) value.toString());
		} else if (value instanceof Number) {
			return jsonNodeFactory.numberNode(((Number) value).doubleValue());
		} else {
			throw new RuntimeException("Invalid attribute type: " + value.getClass().getCanonicalName());
		}
	}
	
	
	protected void writeAuditLogRecord(final Study study, final View view, final Cases caseValue, final String attribute, final String userName, JsonNode oldValue, JsonNode value) {
		
    	final ObjectNode auditLogValues = jsonNodeFactory.objectNode();
    	auditLogValues.replace("old", oldValue);
		auditLogValues.replace("value", value);

		AuditLogRecord record = new AuditLogRecord();
		record.setStudyId(study.getId());
		record.setCaseId(caseValue.getId());
		record.setAttribute(attribute);
		record.setEventType("set_value");
		record.setEventUser(userName);
		record.setEventArgs(auditLogValues.toString());
		auditLogRepository.writeAuditLogRecord(record);
	}
	
	
	@Override
	public void setCaseAttributeValue(final Study study, final View view, final Cases caseValue, final String attribute, final String userName, JsonNode value) throws RepositoryException {
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId())
    	    .and(views.id.eq(view.getId()))
    	    .and(attributes.name.eq(attribute)));
    	ViewAttributes a = template.queryForObject(sqlQuery, new ViewAttributeProjection(attributes, viewAttributes));
    	
    	// If there isn't an attribute, we should probably throw an error.
    	if (a == null) {
    		throw new NotFoundException("Can't find attribute: " + attribute);
    	}
    	
    	ValueValidator validator = AttributeMapper.getAttributeValidator(a.getType());
    	WritableValue writable = validator.validate(a, value);
    	Object oldValue = cap.getOldCaseAttributeValue(template, caseValue, attribute, writable.getValueClass());
    	
    	writeAuditLogRecord(study, view, caseValue, attribute, userName, getJsonValue(oldValue), value);
    	cap.writeCaseAttributeValue(template, caseValue, attribute, writable);
    	
    	// Assuming we got here OK, it's reasonable to generate an update event. We only need to do 
    	// this if we have an UpdateEventService set. 
    	
    	sendUpdateEvent(study, view, caseValue, attribute, userName);
	}
	
	
	private void sendUpdateEvent(final Study study, final View view, final Cases caseValue, final String attribute, final String userName) {
    	UpdateEventService manager = getUpdateEventService();
    	if (manager != null) {
    		UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_SET_FIELD);
    		event.getData().setScope(study.getName());
    		event.getData().setUser(userName);
    		
    		final JsonNodeFactory factory = JsonNodeFactory.instance;
    		ObjectNode parameters = factory.objectNode();
    		parameters.put("field", attribute);
    		parameters.put("case", caseValue.getId());
    		
    		event.getData().setParameters(parameters);

    		manager.sendMessage(event);
    	}
	}
	
	/**
	 * Getter for an update event manager. 
	 */
	public UpdateEventService getUpdateEventService() {
		return manager;
	}

	/**
	 * Setter for an update event manager, allowing events to be triggered from the repository.
	 * @param manager
	 */
	public void setUpdateEventService(UpdateEventService manager) {
		this.manager = manager;
	}

	/**
	 * Creates and returns a case object for a new case. The only fields set will be the
	 * study identifier and the case identifier, but these are enough for finding and 
	 * working with this case. 
	 * @return the new case
	 */
	@Override
	public Cases newStudyCase(final Study study, final View view, final String userName) throws RepositoryException {
		Integer caseId = template.insertWithKey(cases, new SqlInsertWithKeyCallback<Integer>() { 
			public Integer doInSqlInsertWithKeyClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(cases.studyId).values(study.getId()).executeWithKey(cases.id);
			};
		});
		
		if (caseId == null) {
			throw new InvalidValueException("Can't create new case");
		}
		
		Cases newCase = new Cases();
		newCase.setStudyId(study.getId());
		newCase.setId(caseId);

    	UpdateEventService manager = getUpdateEventService();
    	if (manager != null) {
    		UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_NEW_RECORD);
    		event.getData().setScope(study.getName());
    		event.getData().setUser(userName);
    		
    		final JsonNodeFactory factory = JsonNodeFactory.instance;
    		ObjectNode parameters = factory.objectNode();
    		parameters.put("case", newCase.getId());
    		
    		event.getData().setParameters(parameters);

    		manager.sendMessage(event);
    	}
    	
		return newCase;
	}

	@Override
	public void setAuditLogRepository(AuditLogRepository repository) {
		auditLogRepository = repository;
	}
}

