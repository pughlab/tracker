package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityResponse extends ViewResponse {

	ObjectNode entity;
	
	public EntityResponse() {
		super();
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
