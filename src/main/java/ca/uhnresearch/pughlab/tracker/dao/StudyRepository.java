package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public interface StudyRepository {

	/**
	 * Retrieves all the studies from the repository.
	 * @return list of studies
	 */
	List<Study> getAllStudies();
	
	/**
	 * Retrieves a single specified study from the repository.
	 * @return a study
	 */
	Study getStudy(String name);
	
	/**
	 * Writes or updates a study in the repository.
	 * @return a study
	 */
	Study saveStudy(Study name) throws RepositoryException;
	
	/**
	 * Retrieves all the views for a study from the repository.
	 * @return list of views
	 */
	List<View> getStudyViews(Study study);

	/**
	 * Writes the views for a study into the repository.
	 * @throws RepositoryException
	 */
	void setStudyViews(Study study, List<View> views) throws RepositoryException;

	/**
	 * Retrieves all the attributes for a study from the repository.
	 * @return list of attributes
	 */
	List<Attributes> getStudyAttributes(Study study);

	/**
	 * Retrieves a single named attribute for a study from the repository.
	 * @return the attribute
	 */
	Attributes getStudyAttribute(Study study, String name);

	/**
	 * Writes the attributes for a study to the repository.
	 * @throws RepositoryException
	 */
	void setStudyAttributes(Study study, List<Attributes> attributes) throws RepositoryException;

	/**
	 * Retrieves a single specified view for a study from the repository.
	 * @return a view
	 */
	View getStudyView(Study study, String name);

	/**
	 * Updates a single view for the study.
	 * @param study
	 * @param view
	 * @throws RepositoryException
	 */
	void setStudyView(Study study, View view) throws RepositoryException;
	
	/**
	 * Retrieves all the attributes for a view and study from the repository.
	 * @return list of attributes
	 */
	List<ViewAttributes> getViewAttributes(Study study, View view);
	
	/**
	 * Writes the attributes for a view and study to the repository.
	 * @throws RepositoryException
	 */
	void setViewAttributes(Study study, View view, List<ViewAttributes> attributes) throws RepositoryException;
	
	/**
	 * Retrieves the record count for a specified study/view from the repository.
	 * @return number of records
	 */
	Long getRecordCount(Study study, View view);
		
	/**
	 * Retrieves the record-level data for a view and study from the repository.
	 * @return list of JSON nodes
	 */
	List<ObjectNode> getCaseData(StudyCaseQuery query, View view);

	/**
	 * Retrieves the record-level data for a view and study from the repository,
	 * filtered by a list of allowed attributes.
	 * 
	 * @return list of JSON nodes
	 */
	List<ObjectNode> getCaseData(StudyCaseQuery query, List<? extends Attributes> attributes);
	
	/**
	 * Deletes a set of cases from the repository.
	 */
	void deleteCases(StudyCaseQuery query, String userName) throws RepositoryException;

	/**
	 * Builds a new study case query, which can be transformed into a set of cases.
	 * This is always initialized to a single study. 
	 * @return
	 */
	StudyCaseQuery newStudyCaseQuery(Study study);

	/**
	 * Adds a view to the case matcher, which might filter out a bunch of data that should not
	 * be visible. These will typically be row filters, as cases are being selected by the
	 * case query, not attributes. 
	 * @return
	 */
	StudyCaseQuery addViewCaseMatcher(StudyCaseQuery query, View view);

	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned
	 * and can be used later.
	 * @param attribute
	 * @param value
	 * @return
	 */
	StudyCaseQuery addStudyCaseMatcher(StudyCaseQuery query, String attribute, String value);
	
	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned.
	 * and can be used later.
	 * @param query
	 * @param caseId
	 * @return
	 */
	StudyCaseQuery addStudyCaseSelector(StudyCaseQuery query, Integer caseId);

	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned
	 * and can be used later.
	 * @param query
	 * @param filter
	 * @return
	 */
	StudyCaseQuery addStudyCaseFilterSelector(StudyCaseQuery query, ObjectNode filter);

	/**
	 * Returns subcases for a StudyCaseQuery, and returns a new study case query in the
	 * process.
	 * @param query
	 * @param attribute
	 * @return
	 */
	StudyCaseQuery subcases(StudyCaseQuery query, String attribute);
	
	/**
	 * Applies a CasePager to a query, returning a new query.
	 * @param query
	 * @param pager
	 * @return
	 */
	StudyCaseQuery applyPager(StudyCaseQuery query, CasePager pager);

	/**
	 * Retrieves a single specified case for a study and view from the repository.
	 * @return a case
	 */
	Cases getStudyCase(Study study, Integer caseId);
	
	/**
	 * /**
	 * Changes a case state. This is a something that's easy to listen for, and can be set 
	 * simply by a listener. States are often mapped to display classes for row-level 
	 * highlighting. States are also handy for modelling workflows, as they can be
	 * triggered by other changes, and generate notifications. 
	 * 
	 * @param study
	 * @param cases
	 * @param userName
	 * @param state
	 */
	void setStudyCaseState(Study study, Cases cases, String userName, String state);
	
	/**
	 * Makes a new, empty, case. The new case will be added after afterCase
	 * and the others reordered. If afterCase is null or not passed, the new case will be added at the
	 * beginning of the case list, which is only sometimes what you want.
	 * @return the case identifier
	 * @throws RepositoryException
	 */
	Cases newStudyCase(Study study, String userName, Cases afterCase) throws RepositoryException;
	
	/**
	 * Makes a new, empty, case. The new case will be added at the
	 * beginning of the case list, which is only sometimes what you want.
	 * @param study
	 * @param userName
	 * @return
	 * @throws RepositoryException
	 */
	Cases newStudyCase(Study study, String userName) throws RepositoryException;
	
	/**
	 * Selects which attributes will be returned in a query. 
	 * @param query the current query
	 * @param userName the current user's username
	 * @param values
	 * @return a list of CaseChangeInfo records
	 * @throws RepositoryException
	 */
	List<CaseChangeInfo> setQueryAttributes(StudyCaseQuery query, String userName, ObjectNode values) throws RepositoryException;
	
	/**
	 * Sets the event handler.
	 * @param handler the new event handler
	 */
	void setEventHandler(EventHandler handler);
}