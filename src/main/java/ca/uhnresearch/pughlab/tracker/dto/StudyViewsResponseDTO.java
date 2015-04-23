package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyViewsResponseDTO extends StudyResponseDTO {
	
	List<ViewDTO> views = new ArrayList<ViewDTO>();

	public StudyViewsResponseDTO() {
		super();
	}
	
	public StudyViewsResponseDTO(URL url, UserDTO user, Studies s) {
		super(url, user, s);
	}

	@JsonProperty
	public List<ViewDTO> getViews() {
		return views;
	}

	public void setViews(List<ViewDTO> views) {
		this.views = views;
	}
}
