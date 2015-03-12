package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public interface StudyRepository {

	List<Studies> getAllStudies();
	
	Studies getStudy(String name);
	
	List<Views> getStudyViews(Studies study);

	Views getStudyView(Studies study, String name);

	List<Attributes> getViewAttributes(Studies study, Views view);
	
	Long getRecordCount(Studies study, Views view);
	
	List<JsonNode> getData(Studies study, Views view, List<Attributes> attributes, CaseQuery query);
}