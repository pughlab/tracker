package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewEntityRequestBody extends EntityResponse {
	
    private Integer beforeId;

	/**
	 * @return the beforeId
	 */
	@JsonProperty
	public Integer getBeforeId() {
		return beforeId;
	}

	/**
	 * @param beforeId the beforeId to set
	 */
	public void setBeforeId(Integer beforeId) {
		this.beforeId = beforeId;
	}

}
