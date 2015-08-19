package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
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

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
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
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans.caseAttributeBooleans;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates.caseAttributeDates;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings.caseAttributeStrings;
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
				updateAttribute(a);
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
		
	private List<ObjectNode> getJsonData(ListSubQuery<Integer> query) {
		
		SQLQuery caseIdQuery = template.newSqlQuery().from(query.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);
		CaseObjectBuilder builder = new CaseObjectBuilder(caseIds);

		// Right. Now we can add in the attributes from a set of related queries, using the same basic
		// case query as a starting point. Yes, we're re-doing this query more times than I'd like, but
		// we can optimize later.
		
		// Sadly, we are returning all attributes rather than just the ones that are accessible. In
		// effect, we don't use attributes as a filter. 
				
		SQLQuery sqlQuery;
		List<Tuple> values;
		
		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeStrings).on(cases.id.eq(caseAttributeStrings.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeStrings.caseId, caseAttributeStrings.attribute, caseAttributeStrings.value, caseAttributeStrings.notAvailable, caseAttributeStrings.notes));
		builder.addTupleAttributes(values);

		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeDates).on(cases.id.eq(caseAttributeDates.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeDates.caseId, caseAttributeDates.attribute, caseAttributeDates.value, caseAttributeDates.notAvailable, caseAttributeDates.notes));
		builder.addTupleAttributes(values);

		sqlQuery = template.newSqlQuery().from(query.as(cases)).innerJoin(caseAttributeBooleans).on(cases.id.eq(caseAttributeBooleans.caseId));
		values = template.query(sqlQuery, new QTuple(caseAttributeBooleans.caseId, caseAttributeBooleans.attribute, caseAttributeBooleans.value, caseAttributeBooleans.notAvailable, caseAttributeBooleans.notes));
		builder.addTupleAttributes(values);

		return builder.getCaseObjects();
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
		return getJsonData(caseQuery);
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
		List<ObjectNode> listData = getJsonData(caseQuery);
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
	

	private JsonNode getNotAvailableValue() {
		ObjectNode marked = jsonNodeFactory.objectNode();
		marked.put("$notAvailable", Boolean.TRUE);
		return marked;
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
    	
    	// It is very possible there is no old value, so we ought to handle that appropriately.
    	Object oldRawValue = oldValue == null ? null : oldValue.get(0, Object.class);
		Boolean oldNotAvailable = oldValue == null ? false : oldValue.get(1, Boolean.class);
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
		
		AuditLogRecord record = new AuditLogRecord();
		record.setStudyId(study.getId());
		record.setCaseId(caseValue.getId());
		record.setAttribute(attribute);
		record.setEventType("set_value");
		record.setEventUser(userName);
		record.setEventArgs(auditLogValues.toString());
		auditLogRepository.writeAuditLogRecord(record);
    	
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
    		
    		if (a.getOptions() == null || ! a.getOptions().has("values") || ! a.getOptions().get("values").isArray()) {
    			throw new InvalidValueException("No option values specified: " + a.getName());
    		}
    		
    		if (valueNode != null) {
        		Boolean found = false;
        		Iterator<JsonNode> elements = a.getOptions().get("values").elements();
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
    	
    	// Assuming we got here OK, it's reasonable to generate an update event. We only need to do 
    	// this if we have an UpdateEventService set. 
    	
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
	
	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final String value) {
		final String writableValue = valueNotAvailable ? null : value;
		long updateCount = template.update(caseAttributeStrings, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeStrings.caseId.eq(caseValue.getId()).and(caseAttributeStrings.attribute.eq(attribute)))
					.set(caseAttributeStrings.notAvailable, valueNotAvailable)
					.set(caseAttributeStrings.value, writableValue)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeStrings, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeStrings.caseId, caseAttributeStrings.attribute, caseAttributeStrings.value, caseAttributeStrings.notAvailable)
					.values(caseValue.getId(), attribute, writableValue, valueNotAvailable)
					.execute();
			};
		});

	}

	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final Date value) {
		final Date writableValue = valueNotAvailable ? null : value;
		long updateCount = template.update(caseAttributeDates, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeDates.caseId.eq(caseValue.getId()).and(caseAttributeDates.attribute.eq(attribute)))
					.set(caseAttributeDates.notAvailable, valueNotAvailable)
					.set(caseAttributeDates.value, writableValue)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeDates, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeDates.caseId, caseAttributeDates.attribute, caseAttributeDates.value, caseAttributeDates.notAvailable)
					.values(caseValue.getId(), attribute, writableValue, valueNotAvailable)
					.execute();
			};
		});
	}

	private void writeCaseAttributeValue(final Cases caseValue, final String attribute, final Boolean valueNotAvailable, final Boolean value) {
		final Boolean writableValue = valueNotAvailable ? null : value;
		long updateCount = template.update(caseAttributeBooleans, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(caseAttributeBooleans.caseId.eq(caseValue.getId()).and(caseAttributeBooleans.attribute.eq(attribute)))
					.set(caseAttributeBooleans.notAvailable, valueNotAvailable)
					.set(caseAttributeBooleans.value, writableValue)
					.execute();
			};
		});
		if (updateCount == 1) return;
		template.insert(caseAttributeBooleans, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(caseAttributeBooleans.caseId, caseAttributeBooleans.attribute, caseAttributeBooleans.value, caseAttributeBooleans.notAvailable)
					.values(caseValue.getId(), attribute, writableValue, valueNotAvailable)
					.execute();
			};
		});
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

