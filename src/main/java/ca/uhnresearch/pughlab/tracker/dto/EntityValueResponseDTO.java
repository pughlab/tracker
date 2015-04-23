package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityValueResponseDTO extends ViewResponseDTO {
	
	public EntityValueResponseDTO() { }

	public EntityValueResponseDTO(URL url, UserDTO user, Studies s, Views v, JsonNode val) {
		super(url, user, s, v);
		this.value = val;
	}

	JsonNode value;
	
	/**
	 * @return the value
	 */
	@JsonProperty
	public JsonNode getValue() {
		return value;
	}

	/**
	 * @param entity the value to set
	 */
	public void setValue(JsonNode value) {
		this.value = value;
	}

}
