package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

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
	List<Attributes> getViewAttributes(Study study, View view);
	
	/**
	 * Writes the attributes for a view and study to the repository
	 */
	void setViewAttributes(Study study, View view, List<Attributes> attributes) throws RepositoryException;
	
	/**
	 * Retrieves the record count for a specified study/view from the repository
	 * @return number of records
	 */
	Long getRecordCount(Study study, View view);
	
	/**
	 * Retrieves the record-level data for a view and study from the repository
	 * @return list of JSON nodes
	 */
	List<JsonNode> getData(Study study, View view, List<Attributes> attributes, CaseQuery query);
	
	/**
	 * Retrieves a single specified case for a study and view from the repository
	 * @return a case
	 */
	Cases getStudyCase(Study study, View view, Integer caseId);

	/**
	 * Retrieves the record-level data for a given case, view and study from the repository
	 * @return JSON object
	 */
	JsonNode getCaseData(Study study, View view, Cases caseValue);
	
	/**
	 * Retrieves the attribute value for a given case, view, study, and attribute from the repository
	 * @return JSON node
	 */
	JsonNode getCaseAttributeValue(Study study, View view, Cases caseValue, String attribute);

	/**
	 * Writes the attribute value for a given case, view, study, and attribute to the repository
	 */
	void setCaseAttributeValue(Study study, View view, Cases caseValue, String attribute, String userName, JsonNode value) throws RepositoryException;

	/**
	 * Retrieves the audit log data. This is formatted as a set of JSON nodes, as there is 
	 * some reformatting of identifiers to match the tagging within the repository itself. 
	 * @return list of JSON nodes
	 */
	List<JsonNode> getAuditData(Study study, CaseQuery query);
}