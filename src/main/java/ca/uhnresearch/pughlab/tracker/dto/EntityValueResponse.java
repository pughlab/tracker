package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityValueResponse extends ViewResponse {
	
	public EntityValueResponse() { }

	public EntityValueResponse(URL url, User user, Study s, View v, JsonNode val) {
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
