package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class StudyResponseDTO extends AbstractResponseDTO {

	StudyDTO study;
	List<ViewDTO> views = new ArrayList<ViewDTO>();
	
	public StudyResponseDTO(URL url, Studies s) {
		super(url);
		this.study = new StudyDTO(s);
	}

	@JsonProperty
	public List<ViewDTO> getViews() {
		return views;
	}

	public void setViews(List<ViewDTO> views) {
		this.views = views;
	}

	@JsonProperty
	public StudyDTO getStudy() {
		return study;
	}

	public void setStudy(StudyDTO study) {
		this.study = study;
	}
}
