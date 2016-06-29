package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.mysema.query.types.query.NumberSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseChangeInfo;
import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventSource;
import ca.uhnresearch.pughlab.tracker.events.RedactedJsonNode;
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
	
	private EventSource eventSource;

	private QueryDslJdbcTemplate template;
	
	private CaseAttributePersistence cap = new CaseAttributePersistence();

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    public QueryDslJdbcTemplate getTemplate() {
        return template;
    }

	/**
     * Returns a list of studies.
     * @return a list of studies
     */
    public List<Study> getAllStudies() {
		logger.debug("Looking for all studies");

		final SQLQuery sqlQuery = template.newSqlQuery().from(studies).orderBy(studies.name.asc());
		final List<Study> studyList = template.query(sqlQuery, new StudyProjection(studies));
    	logger.debug("Got some studies: {}", studyList.toString());

    	return studyList;
    }

    /**
     * Returns a named study.
     * @param name
     * @return a named study
     */
	public Study getStudy(String name) {
		logger.debug("Looking for study by name: {}", name);
		final SQLQuery sqlQuery = template.newSqlQuery().from(studies).where(studies.name.eq(name));
		final Study study = template.queryForObject(sqlQuery, new StudyProjection(studies));
    	
    	if (study != null) {
    		logger.debug("Got a study: {}", study.toString());
    	} else {
    		logger.debug("No study found");
    	}
    	
    	return study;
	}

	
	/**
	 * Writes or updates a study.
	 */
	@Override
	public Study saveStudy(final Study study, final String userName) {
		if (study.getId() == null) {
			logger.info("Saving new study: {}", study.getName());
			final Integer studyId = template.insertWithKey(studies, new SqlInsertWithKeyCallback<Integer>() { 
				public Integer doInSqlInsertWithKeyClause(SQLInsertClause sqlInsertClause) {
					return (int) sqlInsertClause.populate(study, new StudyMapper()).execute();
				};
			});
			study.setId(studyId);
		} else {
			logger.info("Updating existing study: {}", study.getName());
			template.update(studies, new SqlUpdateCallback() { 
				public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
					return sqlUpdateClause.where(studies.id.eq(study.getId())).populate(study, new StudyMapper()).execute();
				};
			});
		}
		
		// When we have saved a study, we can do an update on the study, the question is: how best to
		// do this. We can easily generate an event, but we need to be a wee bit careful that the 
		// event system can handle re-entrant. 
		
		final Event event = newEvent(study, userName, Event.EVENT_STUDY_CHANGE);
    	getEventSource().doEvent(event);
		
		return study;
	}
	
	
    /**
     * Returns the list of views associated with a study.
     * @param study
     * @return a list of views
     */
	public List<View> getStudyViews(Study study) {
		logger.debug("Looking for views for study: {}", study.getName());
		final SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.studyId.eq(study.getId())).orderBy(views.id.asc());
		final List<View> viewList = template.query(sqlQuery, new ViewProjection(views));
    	logger.debug("Got some views: {}", viewList.toString());

		return viewList;
	}

	@Override
	public void setStudyViews(Study study, List<View> newViewsList) {
		logger.debug("Writing views for study: {}", study.getName());
				
		final SQLQuery sqlQuery = template.newSqlQuery().from(views)
    	    .where(views.studyId.eq(study.getId()));

		final List<View> oldViewsList = template.query(sqlQuery, new ViewProjection(views));		

		final Map<Integer, View> newViews = new HashMap<Integer, View>();
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
			final View newView = newViews.get(v.getId());
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
     * @return a view
     */
	public View getStudyView(Study study, String name) {
		logger.debug("Looking for study by name: {}", name);
		final SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.name.eq(name).and(views.studyId.eq(study.getId())));
		final View view = template.queryForObject(sqlQuery, new ViewProjection(views));

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
     * @param view
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
     * @return a list of attributes
     */
	public List<Attributes> getStudyAttributes(Study study) {
		logger.debug("Looking for study attributes");
		
		final SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());
		
		logger.debug("Executing query: {}", sqlQuery.toString());

		final List<Attributes> attributeList = template.query(sqlQuery, new AttributeProjection(attributes));
    	return attributeList;
	}

    /**
     * Returns a single named attributes for a given study. 
     * @param study
     * @param name
     * @return a named attribute
     */
	public Attributes getStudyAttribute(Study study, String name) {
		logger.debug("Looking for study attribute: {}", name);
		
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()).and(attributes.name.eq(name)));
		
		logger.debug("Executing query: {}", sqlQuery.toString());

    	return template.queryForObject(sqlQuery, new AttributeProjection(attributes));
	}
	
	/**
	 * Returns a single study case.
	 */
	@Override
	public Cases getStudyCase(Study study, Integer caseId) {
		logger.debug("Looking for case by identifier: {}", caseId);
		final SQLQuery sqlQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()).and(cases.id.eq(caseId)));
		final Cases caseValue = template.queryForObject(sqlQuery, cases);
    	
    	if (caseValue != null) {
    		logger.debug("Got a case: {}", caseValue.toString());
    	} else {
    		logger.debug("No case found");
    	}
    	
    	return caseValue;
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
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.viewId.eq(v.getId())).execute();
			};
		});
		template.delete(views, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(views.id.eq(v.getId())).execute();
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
		
		final SQLQuery sqlQuery = template.newSqlQuery().from(attributes).where(attributes.id.eq(a.getId()));
		final String oldType = template.queryForObject(sqlQuery, attributes.type);
		if (oldType != null && !oldType.equals(a.getType())) {
			cap.deleteAllAttributes(template, a);
		}
		
		template.update(attributes, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(attributes.id.eq(a.getId())).populate(a, new AttributeMapper()).execute();
			};
		});
		
		// If an attribute type has changed, we should make sure that all existing
		// values that are active for that attribute are no longer marked active. 
		// Same is true for deletion, actually. Deletion is a bit easier, though, as
		// we can do it in all cases, and oh look: we do.
	}

	private void deleteAttribute(final Attributes a) {
		cap.deleteAllAttributes(template, a);
		template.delete(viewAttributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(viewAttributes.attributeId.eq(a.getId())).execute();
			};
		});
		template.delete(attributes, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(attributes.id.eq(a.getId())).execute();
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
		
		final SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .where(attributes.studyId.eq(study.getId()))
    	    .orderBy(attributes.rank.asc());

		final List<Attributes> attributeList = template.query(sqlQuery, new AttributeProjection(attributes));
		
		// Now we can go through both lists, checking to see which attributes
		// are to be deleted, which updated, and so on.
		final Map<Integer, Attributes> newAttributes = new HashMap<Integer, Attributes>();
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
			final Attributes newAttribute = newAttributes.get(a.getId());
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
		final List<Attributes> studyAttributes = getStudyAttributes(study);
		final Map<Integer, Attributes> studyAttributesTable = new HashMap<Integer, Attributes>();
		for (Attributes a : studyAttributes) {
			studyAttributesTable.put(a.getId(), a);
		}
		
		// Next, we need a list of the current view attributes/
		final List<ViewAttributes> oldAttributes = getViewAttributes(study, view);
		
		// Next, build a table of the identifiers
		final Map<Integer, Attributes> oldAttributesTable = new HashMap<Integer, Attributes>();
		for(Attributes a : oldAttributes) {
			oldAttributesTable.put(a.getId(), a);
		}
		
		// For each new/existing attribute, we can update the options in the view attribute
		// data. Unmatched old ones will be left in the table and deleted later.
		Integer rank = 0;
		for(Attributes a : newAttributes) {
			final Attributes old = oldAttributesTable.get(a.getId());
			if (old == null) {
				// No old attribute, this is a new one. So first check it exists within
				// the study. If it doesn't, then we can throw an exception.
				final Attributes studyAttribute = studyAttributesTable.get(a.getId());
				if (studyAttribute == null) {
					throw new NotFoundException("Missing attribute: " + a.getName());
				}
				
				final ViewAttributes va = new ViewAttributes();
				va.setId(studyAttribute.getId());
				va.setRank(rank);
				va.setViewOptions(a.getOptions());
				insertViewAttribute(view, va);
				
			} else {
				// We do have an old attribute as well as a new one, so this is basically
				// an update on the view attribute options. But first we need to locate
				// the existing ViewAttributes.
				
				final SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
		    		.innerJoin(viewAttributes)
		    		.on(attributes.id.eq(viewAttributes.attributeId))
		    		.where(viewAttributes.viewId.eq(view.getId()).and(viewAttributes.attributeId.eq(old.getId())));
				final ViewAttributes va = template.queryForObject(sqlQuery, new ViewAttributeProjection(attributes, viewAttributes));
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
			final ViewAttributes va = new ViewAttributes();
			va.setId(a.getId());
			deleteViewAttribute(view, va);
		}
	}

    /**
     * Returns a list of all the attributes for a given view. 
     * @param study
     * @param view
     * @return a list of view attributes
     */
	public List<ViewAttributes> getViewAttributes(Study study, View view) {
		
		logger.debug("Looking for view attributes");
		
		final SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId()).and(views.id.eq(view.getId())))
    	    .orderBy(viewAttributes.rank.asc());
		
		final List<ViewAttributes> attributeList = template.query(sqlQuery, new ViewAttributeProjection(attributes, viewAttributes));

		return attributeList;
	}

	/**
	 * Returns the record count for a study).
	 */
	@Override
	public Long getRecordCount(Study study, View view) {
		final SQLQuery recordQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()));
		return template.count(recordQuery);
	}

	/**
	 * Changes a case state. This is a something that's easy to listen for, and can be set 
	 * simply by a listener. States are often mapped to display classes for row-level 
	 * highlighting. States are also handy for modelling workflows, as they can be
	 * triggered by other changes, and generate notifications.
	 * 
	 * @param study
	 * @param c
	 * @param userName
	 * @param state
	 */
	@Override
	public void setStudyCaseState(final Study study, final Cases c, final String userName, final String state) {
		logger.debug("Updating a case: {}", c.getId());
		
		final String oldState = c.getState();
		
		template.update(cases, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(cases.id.eq(c.getId()).and(cases.studyId.eq(study.getId())))
						.set(cases.state, state)
						.execute();
			};
		});
		
		// This merits a new event, too, but it's not an attribute event, it's a state change
		// event. That way it gets audited and can be sent through a websocket connection
		// to a listening client. 
		
		final Event event = newEvent(study, userName, Event.EVENT_STATE);
		event.getData().getParameters().put("case_id", c.getId());
		event.getData().getParameters().put("old_state", oldState);
		event.getData().getParameters().put("state", state);

    	getEventSource().doEvent(event);
    	
    	c.setState(state);

    	return;
	}
	
	private void setQueryAttributesForField(final Study study, final String userName, final CaseChangeInfo caseChanges, final String field) {
		
		final CaseChangeInfo.Change change = caseChanges.getChange(field);
	
		final Event event = newEvent(study, userName, Event.EVENT_SET_FIELD);
		event.getData().getParameters().put("field", field);
		event.getData().getParameters().put("case_id", caseChanges.getCaseId());
		event.getData().getParameters().replace("old", new RedactedJsonNode(change.getOldValue()));
		event.getData().getParameters().replace("new", new RedactedJsonNode(change.getNewValue()));

    	getEventSource().doEvent(event);
	}

	/**
	 * Writing data into a StudyCaseQuery is a little more complex. We will end up with 
	 * a case selection, which we can then use to make the insert/updates that we need to do. 
	 * Much of the logic should be delegated to the CaseAttributePersistence layer.
	 */
	@Override
	public List<CaseChangeInfo> setQueryAttributes(StudyCaseQuery query, String userName, ObjectNode values) throws RepositoryException {
		
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}
		
		final List<CaseChangeInfo> changes = cap.setQueryAttributes(template, (QueryStudyCaseQuery) query, values);
		
		final Study study = query.getStudy();

		for(CaseChangeInfo caseChanges : changes) {
			for(String field : caseChanges.fields()) {
				setQueryAttributesForField(study, userName, caseChanges, field);
			}
		}
		
		return changes;
	}

	/**
	 * Getter for an update event manager. 
	 */
	public EventSource getEventSource() {
		return eventSource;
	}

	/**
	 * Setter for an update event manager, allowing events to be triggered from the repository.
	 * @param source the source
	 */
	public void setEventSource(EventSource source) {
		this.eventSource = source;
	}

	@Override
	public Cases newStudyCase(final Study study, final String userName) throws RepositoryException {
		return newStudyCase(study, userName, null);
	}	
	/**
	 * Creates and returns a case object for a new case. The only fields set will be the
	 * study identifier and the case identifier, but these are enough for finding and 
	 * working with this case. 
	 * @return the new case
	 */
	@Override
	public Cases newStudyCase(final Study study, final String userName, final Cases beforeCase) throws RepositoryException {
		
		Integer orderPoint = 1;
		if (beforeCase != null) {
			orderPoint = beforeCase.getOrder();
		} else {
			final SQLQuery sqlQuery = template.newSqlQuery().from(cases).where(cases.studyId.eq(study.getId()));
			orderPoint = template.queryForObject(sqlQuery, cases.order.max());
			if (orderPoint == null) {
				orderPoint = 0;
			}
			orderPoint = orderPoint + 1;
		}
		
		final Integer orderValue = orderPoint;
	
		// Now we can update everything > breakpoint.
		template.update(cases, new SqlUpdateCallback() { 
			public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
				return sqlUpdateClause.where(cases.studyId.eq(study.getId()).and(cases.order.goe(orderValue)))
						.set(cases.order, cases.order.add(1)).execute();
			};
		});

		// And now let's insert a new case, with the right break value
		Integer caseId = template.insertWithKey(cases, new SqlInsertWithKeyCallback<Integer>() { 
			public Integer doInSqlInsertWithKeyClause(SQLInsertClause sqlInsertClause) {
				return sqlInsertClause.columns(cases.studyId, cases.guid, cases.order).values(study.getId(), UUID.randomUUID().toString(), orderValue).executeWithKey(cases.id);
			};
		});
		
		if (caseId == null) {
			throw new InvalidValueException("Can't create new case");
		}
		
		final Cases newCase = new Cases();
		newCase.setStudyId(study.getId());
		newCase.setId(caseId);

		final Event event = new Event(Event.EVENT_NEW_RECORD, study.getName());
		event.getData().setUser(userName);
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode parameters = factory.objectNode();
		parameters.put("study_id", study.getId());
		parameters.put("case_id", newCase.getId());
		event.getData().setParameters(parameters);

    	getEventSource().doEvent(event);
    	
		return newCase;
	}

	@Override
	public List<ObjectNode> getCaseData(StudyCaseQuery query, List<? extends Attributes> attributes) {
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}

		return cap.getJsonData(template, (QueryStudyCaseQuery) query, attributes);
	}

	@Override
	public List<ObjectNode> getCaseData(StudyCaseQuery query, View view) {
		return getCaseData(query, getViewAttributes(query.getStudy(), view));
	}

	@Override
	public QueryStudyCaseQuery newStudyCaseQuery(Study study) {
		final SQLSubQuery sq = new SQLSubQuery().from(cases).where(cases.studyId.eq(study.getId()));
		return new QueryStudyCaseQuery(study, sq);
	}
	
	// TODO Add row filtering into view handling
	@Override
	public StudyCaseQuery addViewCaseMatcher(StudyCaseQuery query, View view) {		
		return query;
	}

	/**
	 * Applies a pager to the query inside a QueryStudyCaseQuery, which isn't intended to 
	 * be exposed externally. 
	 * @param query
	 * @param pager
	 * @return a new query 
	 */
	@Override
	public QueryStudyCaseQuery applyPager(StudyCaseQuery query, CasePager pager) {
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}

		final QueryStudyCaseQuery scq = (QueryStudyCaseQuery) query;
		
		SQLSubQuery sq = scq.getQuery();
		
		// If we have an ordering, use a left join to get the attribute, and order it later
		if (pager.getOrderField() != null) {
			final NumberSubQuery<Integer> attributeQuery = new SQLSubQuery()
					.from(attributes)
					.where(attributes.name.eq(pager.getOrderField()).and(attributes.studyId.eq(cases.studyId)))
					.unique(attributes.id);
			final QCaseAttributeStrings c = new QCaseAttributeStrings("c");
			sq = sq.leftJoin(c)
					.on(c.caseId.eq(cases.id).and(c.attributeId.eq(attributeQuery)));
			final OrderSpecifier<?> ordering = c.getValueOrderSpecifier(pager.getOrderDirection() == CasePager.OrderDirection.ASC);
			sq = sq.orderBy(ordering);
		} else {
			sq = sq.orderBy(cases.order.asc());
		}
		
		if (pager.hasOffset()) {
			sq = sq.offset(pager.getOffset().longValue());
		}
		if (pager.hasLimit()) {
			sq = sq.limit(pager.getLimit().longValue());
		}
		return new QueryStudyCaseQuery(scq.getStudy(), sq);
	}

	/**
	 * Only applies to string values (for now) this applies a filter into the case query
	 * matcher. 
	 */
	@Override
	public StudyCaseQuery addStudyCaseMatcher(StudyCaseQuery query, String attribute, String value) {
		throw new RuntimeException("Not yet implemented");
	}


	/** 
	 * Refines a query to a single case, which can be found by identifier. This can then be incorporated
	 * into the queries that are used to access data. 
	 * @param query
	 * @return a new query
	 */
	@Override
	public StudyCaseQuery addStudyCaseSelector(StudyCaseQuery query, Integer caseId) {
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}

		final QueryStudyCaseQuery scq = (QueryStudyCaseQuery) query;
		SQLSubQuery sq = scq.getQuery();
		sq = sq.where(cases.id.eq(caseId));
		return new QueryStudyCaseQuery(scq.getStudy(), sq);
	}

	@Override
	public StudyCaseQuery subcases(StudyCaseQuery query, String attribute) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public StudyCaseQuery addStudyCaseFilterSelector(StudyCaseQuery query, ObjectNode filter) {
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}

		final QueryStudyCaseQuery scq = (QueryStudyCaseQuery) query;
		final SQLSubQuery sq = cap.filterQuery(getTemplate(), scq, filter);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Using case filter: {}", sq.toString());
		}
		
		return new QueryStudyCaseQuery(scq.getStudy(), sq);
	}
	
	
	/**
	 * Makes a new event record for a given study, user, and event type.
	 * @param study
	 * @param userName
	 * @param eventType
	 * @return a new event
	 */
	private Event newEvent(final Study study, final String userName, final String eventType) {
		
		final Event event = new Event(eventType, study.getName());
		event.getData().setUser(userName);
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode parameters = factory.objectNode();
		parameters.put("study_id", study.getId());
		parameters.put("study", study.getName());
		event.getData().setParameters(parameters);
		
		return event;
	}
	
	
	/**
	 * Sends an event for deleting a case.
	 * @param study
	 * @param userName
	 * @param data
	 * @throws RepositoryException
	 */
	private void sendDeleteCaseEvent(final Study study, final String userName, final ObjectNode data) throws RepositoryException {
		
		// This merits a new event, too, but it's not an attribute event, it's a state change
		// event. That way it gets audited and can be sent through a websocket connection
		// to a listening client. 
		
		final Event event = newEvent(study, userName, Event.EVENT_DELETE_RECORD);
		event.getData().getParameters().put("case_id", data.get("id").asInt());
		event.getData().getParameters().replace("data", RedactedJsonNode.redactObjectNode(data));

    	getEventSource().doEvent(event);
	}
	
	/**
	 * Deletes a set of cases from the repository. 
	 * @param query a query to select the cases for deletion
	 * @param userName the user
	 */
	@Override
	public void deleteCases(final StudyCaseQuery query, final String userName) throws RepositoryException {
		if (! (query instanceof QueryStudyCaseQuery)) {
			throw new RuntimeException("Invalid type of StudyCaseQuery: " + query.getClass().getCanonicalName());
		}

		final QueryStudyCaseQuery scq = (QueryStudyCaseQuery) query;
		final Study study = scq.getStudy();
		final List<Attributes> attributes = getStudyAttributes(study);

		final List<ObjectNode> caseDatas = getCaseData(query, attributes);
		for(ObjectNode caseData : caseDatas) {
			sendDeleteCaseEvent(study, userName, caseData);
		}

		cap.deleteCases(template, scq);
	}
}
