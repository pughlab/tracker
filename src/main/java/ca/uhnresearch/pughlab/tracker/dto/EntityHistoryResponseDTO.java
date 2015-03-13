package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityHistoryResponseDTO extends ViewResponseDTO {

	JsonNode history;

	public EntityHistoryResponseDTO(URL url, UserDTO user, Studies s, Views v, JsonNode h) {
		super(url, user, s, v);
		this.history = h;
	}

	/**
	 * @return the entity
	 */
	@JsonProperty
	public JsonNode getHistory() {
		return history;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setHistory(JsonNode history) {
		this.history = history;
	}

}