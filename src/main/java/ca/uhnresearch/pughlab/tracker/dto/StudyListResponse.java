package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyListResponse extends AbstractResponse {

	List<Study> studies = new ArrayList<Study>();
	
	public StudyListResponse() { 
		super();
	}

	@JsonProperty
	public List<Study> getStudies() {
		return studies;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}

}
