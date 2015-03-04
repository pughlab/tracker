package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyListResponseDTO extends AbstractResponseDTO {

	List<StudyDTO> studies = new ArrayList<StudyDTO>();

	public StudyListResponseDTO(URL url) {
		super(url);
	}

	@JsonProperty
	public List<StudyDTO> getStudies() {
		return studies;
	}

	public void setStudies(List<StudyDTO> studies) {
		this.studies = studies;
	}

}
