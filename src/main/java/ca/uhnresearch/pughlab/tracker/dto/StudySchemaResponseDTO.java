package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class StudySchemaResponseDTO extends StudyViewsResponseDTO {

	public StudySchemaResponseDTO() {
		super();
	}

	public StudySchemaResponseDTO(URL url, UserDTO user, Studies s) {
		super(url, user, s);
	}
	
	List<AttributeDTO> attributes = new ArrayList<AttributeDTO>();

	@JsonProperty
	public List<AttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDTO> attributes) {
		this.attributes = attributes;
	}
}
