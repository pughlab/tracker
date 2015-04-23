package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public interface StudyRepository {

	/**
	 * Retrieves all the studies from the repository
	 * @return list of studies
	 */
	List<Studies> getAllStudies();
	
	/**
	 * Retrieves a single specified study from the repository
	 * @return a study
	 */
	Studies getStudy(String name);
	
	/**
	 * Retrieves all the views for a study from the repository
	 * @return list of views
	 */
	List<Views> getStudyViews(Studies study);

	/**
	 * Retrieves all the attributes for a view and study from the repository
	 * @return list of attributes
	 */
	List<Attributes> getStudyAttributes(Studies study);

	/**
	 * Retrieves a single specified view for a study from the repository
	 * @return a view
	 */
	Views getStudyView(Studies study, String name);

	/**
	 * Retrieves all the attributes for a view and study from the repository
	 * @return list of attributes
	 */
	List<Attributes> getViewAttributes(Studies study, Views view);
	
	/**
	 * Retrieves the record count for a specified study/view from the repository
	 * @return number of records
	 */
	Long getRecordCount(Studies study, Views view);
	
	/**
	 * Retrieves the record-level data for a view and study from the repository
	 * @return list of JSON nodes
	 */
	List<JsonNode> getData(Studies study, Views view, List<Attributes> attributes, CaseQuery query);
	
	/**
	 * Retrieves a single specified case for a study and view from the repository
	 * @return a case
	 */
	Cases getStudyCase(Studies study, Views view, Integer caseId);

	/**
	 * Retrieves the record-level data for a given case, view and study from the repository
	 * @return JSON object
	 */
	JsonNode getCaseData(Studies study, Views view, Cases caseValue);
	
	/**
	 * Retrieves the attribute value for a given case, view, study, and attribute from the repository
	 * @return JSON node
	 */
	JsonNode getCaseAttributeValue(Studies study, Views view, Cases caseValue, String attribute);

	/**
	 * Writes the attribute value for a given case, view, study, and attribute to the repository
	 */
	void setCaseAttributeValue(Studies study, Views view, Cases caseValue, String attribute, String userName, JsonNode value) throws RepositoryException;

	/**
	 * Retrieves the audit log data. This is formatted as a set of JSON nodes, as there is 
	 * some reformatting of identifiers to match the tagging within the repository itself. 
	 * @return list of JSON nodes
	 */
	List<JsonNode> getAuditData(Studies study, CaseQuery query);
}