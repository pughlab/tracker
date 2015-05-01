package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyResponse extends AbstractResponse {

	Study study;
	
	public StudyResponse() {
		super();
	}
	
	@JsonProperty
	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
}
