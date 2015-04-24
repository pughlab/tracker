package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyResponse extends AbstractResponse {

	Study study;
	
	public StudyResponse() {
		super();
	}
	
	public StudyResponse(URL url, User user, Study s) {
		super();
		this.setServiceUrl(url);
		this.setUser(user);
		this.setStudy(s);
	}

	@JsonProperty
	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
}
