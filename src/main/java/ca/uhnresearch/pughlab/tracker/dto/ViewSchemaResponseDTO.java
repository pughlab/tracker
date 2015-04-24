package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewSchemaResponseDTO extends ViewResponseDTO {

	public ViewSchemaResponseDTO() {
		super();
	}

	public ViewSchemaResponseDTO(URL url, UserDTO user, Studies s, Views v) {
		super(url, user, s, v);
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
