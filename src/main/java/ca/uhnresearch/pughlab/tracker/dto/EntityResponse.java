package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class EntityResponse extends ViewResponse {

	JsonNode entity;
	
	public EntityResponse() {
		super();
	}
	
	public EntityResponse(URL url, User user, Study s, View v, JsonNode e) {
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
