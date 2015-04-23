package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyListResponseDTO extends AbstractResponseDTO {

	List<StudyDTO> studies = new ArrayList<StudyDTO>();
	
	public StudyListResponseDTO() { 
		super();
	}

	public StudyListResponseDTO(URL url, UserDTO user) {
		super();
		this.setServiceUrl(url);
		this.setUser(user);
	}

	@JsonProperty
	public List<StudyDTO> getStudies() {
		return studies;
	}

	public void setStudies(List<StudyDTO> studies) {
		this.studies = studies;
	}

}
