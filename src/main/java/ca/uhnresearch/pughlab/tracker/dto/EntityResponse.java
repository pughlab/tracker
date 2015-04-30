package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityResponse extends ViewResponse {

	ObjectNode entity;
	
	public EntityResponse() {
		super();
	}
	
	public EntityResponse(URL url, User user, Study s, View v, ObjectNode e) {
		super(url, user, s, v);
		this.entity = e;
	}

	/**
	 * @return the entity
	 */
	@JsonProperty
	public ObjectNode getEntity() {
		return entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(ObjectNode entity) {
		this.entity = entity;
	}

}
