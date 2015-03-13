package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityResponseDTO extends ViewResponseDTO {

	JsonNode entity;

	public EntityResponseDTO(URL url, UserDTO user, Studies s, Views v, JsonNode e) {
		super(url, user, s, v);
		this.entity = e;
	}

	/**
	 * @return the entity
	 */
	@JsonProperty
	public JsonNode getEntity() {
		return entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(JsonNode entity) {
		this.entity = entity;
	}

}
