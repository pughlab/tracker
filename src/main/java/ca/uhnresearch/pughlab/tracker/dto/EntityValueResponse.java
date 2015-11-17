package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityValueResponse extends ViewResponse {
	
	public EntityValueResponse() { }

	JsonNode value;
	
	/**
	 * @return the value
	 */
	@JsonProperty
	public JsonNode getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(JsonNode value) {
		this.value = value;
	}

}
