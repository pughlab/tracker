package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public interface StudyRepository {

	/**
	 * Retrieves all the studies from the repository
	 * @return list of studies
	 */
	List<Study> getAllStudies();
	
	/**
	 * Retrieves a single specified study from the repository
	 * @return a study
	 */
	Study getStudy(String name);
	
	/**
	 * Writes or updates a study in the repository
	 * @return a study
	 */
	Study saveStudy(Study name) throws RepositoryException;
	
	/**
	 * Retrieves all the views for a study from the repository
	 * @return list of views
	 */
	List<View> getStudyViews(Study study);

	/**
	 * Writes the views for a study into the repository
	 */
	void setStudyViews(Study study, List<View> views) throws RepositoryException;

	/**
	 * Retrieves all the attributes for a study from the repository
	 * @return list of attributes
	 */
	List<Attributes> getStudyAttributes(Study study);

	/**
	 * Retrieves a single named attribute for a study from the repository
	 * @return the attribute
	 */
	Attributes getStudyAttribute(Study study, String name);

	/**
	 * Writes the attributes for a study to the repository
	 */
	void setStudyAttributes(Study study, List<Attributes> attributes) throws RepositoryException;

	/**
	 * Retrieves a single specified view for a study from the repository
	 * @return a view
	 */
	View getStudyView(Study study, String name);

	/**
	 * Updates a single view for the study
	 * @param study
	 * @param view
	 * @throws RepositoryException
	 */
	void setStudyView(Study study, View view) throws RepositoryException;
	
	/**
	 * Retrieves all the attributes for a view and study from the repository
	 * @return list of attributes
	 */
	List<ViewAttributes> getViewAttributes(Study study, View view);
	
	/**
	 * Writes the attributes for a view and study to the repository
	 */
	void setViewAttributes(Study study, View view, List<ViewAttributes> attributes) throws RepositoryException;
	
	/**
	 * Retrieves the record count for a specified study/view from the repository
	 * @return number of records
	 */
	Long getRecordCount(Study study, View view);
	
	/**
	 * Retrieves the record-level data for a view and study from the repository
	 * @return list of JSON nodes
	 */
	List<ObjectNode> getData(Study study, View view, List<? extends Attributes> attributes, CaseQuery query);
	
	/**
	 * Retrieves a single specified case for a study and view from the repository
	 * @return a case
	 */
	Cases getStudyCase(Study study, View view, Integer caseId);
	
	/**
	 * /**
	 * Changes a case state. This is a something that's easy to listen for, and can be set 
	 * simply by a listener. States are often mapped to display classes for row-level 
	 * highlighting. States are also handy for modelling workflows, as they can be
	 * triggered by other changes, and generate notifications. 
	 * 
	 * @param study
	 * @param view
	 * @param cases
	 * @param state
	 */
	void setStudyCaseState(Study study, View view, Cases cases, String userName, String state);
	
	/**
	 * Makes a new, empty, case. If an afterCase is passed, the new case will be added after that case
	 * and the others reordered. If afterCase is null or not passed, the new case will be added at the
	 * beginning of the case list, which is very probably not what you want.
	 * 
	 * @return the case identifier
	 */
	Cases newStudyCase(Study study, View view, String userName, Cases afterCase) throws RepositoryException;
	Cases newStudyCase(Study study, View view, String userName) throws RepositoryException;

	/**
	 * Retrieves the record-level data for a given case, view and study from the repository
	 * @return JSON object
	 */
	ObjectNode getCaseData(Study study, View view, Cases caseValue);
	ObjectNode getCaseData(Study study, View view, List<Attributes> attributes, Cases caseValue);
	
	/**
	 * Retrieves the attribute value for a given case, view, study, and attribute from the repository
	 * @return JSON node
	 */
	JsonNode getCaseAttributeValue(Study study, View view, Cases caseValue, Attributes attribute);

	/**
	 * Writes the attribute value for a given case, view, study, and attribute to the repository
	 */
	void setCaseAttributeValue(Study study, View view, Cases caseValue, Attributes attribute, String userName, JsonNode value) throws RepositoryException;

	void setEventHandler(EventHandler manager);
}