package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class StudyResponseDTO extends AbstractResponseDTO {

	StudyDTO study;
	
	public StudyResponseDTO() {
		super();
	}
	
	public StudyResponseDTO(URL url, UserDTO user, Studies s) {
		super();
		this.setServiceUrl(url);
		this.setUser(user);
		this.setStudy(new StudyDTO(s));
	}

	@JsonProperty
	public StudyDTO getStudy() {
		return study;
	}

	public void setStudy(StudyDTO study) {
		this.study = study;
	}
}
