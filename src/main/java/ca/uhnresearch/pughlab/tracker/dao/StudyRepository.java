package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.EventSource;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The general interface for a study repository.
 * 
 * @author stuartw
 */
public interface StudyRepository {

	/**
	 * Retrieves all the studies from the repository.
	 * @return list of studies
	 */
	List<Study> getAllStudies();
	
	/**
	 * Retrieves a single specified study from the repository.
	 * @param name the study name
	 * @return a study
	 */
	Study getStudy(String name);
	
	/**
	 * Writes or updates a study in the repository.
	 * @param name the study name
	 * @param userName the username
	 * @return a study
	 * @throws RepositoryException if there's an exception
	 */
	Study saveStudy(Study name, String userName) throws RepositoryException;
	
	/**
	 * Retrieves all the views for a study from the repository.
	 * @param study the study
	 * @return list of views
	 */
	List<View> getStudyViews(Study study);

	/**
	 * Writes the views for a study into the repository.
	 * @param study the study
	 * @param views a list of views
	 * @throws RepositoryException if there's an exception
	 */
	void setStudyViews(Study study, List<View> views) throws RepositoryException;

	/**
	 * Retrieves all the attributes for a study from the repository.
	 * @param study the study
	 * @return list of attributes
	 */
	List<Attributes> getStudyAttributes(Study study);

	/**
	 * Retrieves a single named attribute for a study from the repository.
	 * @param study the study
	 * @param name the attribute name
	 * @return the attribute
	 */
	Attributes getStudyAttribute(Study study, String name);

	/**
	 * Writes the attributes for a study to the repository.
	 * @param study the study
	 * @param attributes a list of attributes
	 * @throws RepositoryException if there's an exception
	 */
	void setStudyAttributes(Study study, List<Attributes> attributes) throws RepositoryException;

	/**
	 * Retrieves a single specified view for a study from the repository.
	 * @param study the study
	 * @param name the view name
	 * @return a view
	 */
	View getStudyView(Study study, String name);

	/**
	 * Updates a single view for the study.
	 * @param study the study
	 * @param view the view
	 * @throws RepositoryException if there's an exception
	 */
	void setStudyView(Study study, View view) throws RepositoryException;
	
	/**
	 * Retrieves all the attributes for a view and study from the repository.
	 * @param study the study
	 * @param view the view
	 * @return list of attributes
	 */
	List<ViewAttributes> getViewAttributes(Study study, View view);
	
	/**
	 * Writes the attributes for a view and study to the repository.
	 * @param study the study
	 * @param view the view
	 * @param attributes the attributes
	 * @throws RepositoryException if there's an exception
	 */
	void setViewAttributes(Study study, View view, List<ViewAttributes> attributes) 
			throws RepositoryException;
	
	/**
	 * Retrieves the record count for a specified study/view from the repository.
	 * @param study the study
	 * @param view the view
	 * @return number of records
	 */
	Long getRecordCount(Study study, View view);
		
	/**
	 * Retrieves the record-level data for a view and study from the repository.
	 * @param view the view
	 * @param query the query to select cases
	 * @return list of JSON nodes
	 */
	List<ObjectNode> getCaseData(StudyCaseQuery query, View view);

	/**
	 * Retrieves the record-level data for a view and study from the repository,
	 * filtered by a list of allowed attributes.
	 * 
	 * @param query the query to select cases
	 * @param attributes the attributes
	 * @return list of JSON nodes
	 */
	List<ObjectNode> getCaseData(StudyCaseQuery query, List<? extends Attributes> attributes);
	
	/**
	 * Deletes a set of cases from the repository.
	 *
	 * @param query the query to select cases
	 * @param userName the username
	 * @throws RepositoryException if there's an exception
	 */
	void deleteCases(StudyCaseQuery query, String userName) throws RepositoryException;

	/**
	 * Builds a new study case query, which can be transformed into a set of cases.
	 * This is always initialized to a single study. 
	 * 
	 * @param study the study
	 * @return a new StudyCaseQuery
	 */
	StudyCaseQuery newStudyCaseQuery(Study study);

	/**
	 * Adds a view to the case matcher, which might filter out a bunch of data that should not
	 * be visible. These will typically be row filters, as cases are being selected by the
	 * case query, not attributes. 
	 * 
	 * @param query the query to select cases
	 * @param view the view
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery addViewCaseMatcher(StudyCaseQuery query, View view);

	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned
	 * and can be used later.
	 * 
	 * @param query the query to select cases
	 * @param attribute the attribute
	 * @param value the value
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery addStudyCaseMatcher(StudyCaseQuery query, String attribute, String value);
	
	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned.
	 * and can be used later.
	 * 
	 * @param query the query to select cases
	 * @param caseId the case identifier
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery addStudyCaseSelector(StudyCaseQuery query, Integer caseId);

	/**
	 * Adds a new matching element to a study case filter. Only string values 
	 * are typically allowed here, currently. A new study case query is returned
	 * and can be used later.
	 * 
	 * @param query the query to select cases
	 * @param filter the filter
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery addStudyCaseFilterSelector(StudyCaseQuery query, ObjectNode filter);

	/**
	 * Returns subcases for a StudyCaseQuery, and returns a new study case query in the
	 * process.
	 * 
	 * @param query the query to select cases
	 * @param attribute the attribute
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery subcases(StudyCaseQuery query, String attribute);
	
	/**
	 * Applies a CasePager to a query, returning a new query.
	 * @param query the query to select cases
	 * @param pager the pager
	 * @return a modified StudyCaseQuery
	 */
	StudyCaseQuery applyPager(StudyCaseQuery query, CasePager pager);

	/**
	 * Retrieves a single specified case for a study and view from the repository.
	 * @param study the study
	 * @param caseId a case id to locate
	 * @return a case
	 */
	Cases getStudyCase(Study study, Integer caseId);
	
	/**
	 * Changes a case state. This is a something that's easy to listen for, and can be set 
	 * simply by a listener. States are often mapped to display classes for row-level 
	 * highlighting. States are also handy for modelling workflows, as they can be
	 * triggered by other changes, and generate notifications. 
	 * 
	 * @param study the study
	 * @param cases the case
	 * @param userName the user name
	 * @param state the state
	 */
	void setStudyCaseState(Study study, Cases cases, String userName, String state);
	
	/**
	 * Makes a new, empty, case. The new case will be added before beforeCase
	 * and the others reordered. If afterCase is null or not passed, the new case 
	 * will be added at the beginning of the case list, which is only sometimes what 
	 * you want.
	 * 
	 * @param study the study
	 * @param userName the user name
	 * @param beforeCase if supplied, inserts the case before this specified case
	 * @return the case identifier
	 * @throws RepositoryException if there's an exception
	 */
	Cases newStudyCase(Study study, String userName, Cases beforeCase) throws RepositoryException;
	
	/**
	 * Makes a new, empty, case. The new case will be added at the
	 * beginning of the case list, which is only sometimes what you want.
	 * @param study the study
	 * @param userName the user name
	 * @return a new case
	 * @throws RepositoryException if there's an exception
	 */
	Cases newStudyCase(Study study, String userName) throws RepositoryException;
	
	/**
	 * Selects which attributes will be returned in a query. 
	 * @param query the current query
	 * @param userName the user name
	 * @param values the values
	 * @return a list of CaseChangeInfo records
	 * @throws RepositoryException
	 */
	List<CaseChangeInfo> setQueryAttributes(StudyCaseQuery query, String userName, 
			                                ObjectNode values) throws RepositoryException;
	
	/**
	 * Sets the event source.
	 * @param source the new event source
	 */
	void setEventSource(EventSource source);
}
