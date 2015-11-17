package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyPermissions extends AdministratorPermissions {

	/**
	 * Retrieves the create permission.
	 * @return the create
	 */
	@JsonProperty
	public Boolean getCreate() {
		return create;
	}

	/**
	 * Sets the create permission.
	 * @param create the create to set
	 */
	public void setCreate(Boolean create) {
		this.create = create;
	}

	/**
	 * Retrieves the delete permission.
	 * @return the delete
	 */
	@JsonProperty
	public Boolean getDelete() {
		return delete;
	}

	/**
	 * Sets the delete permission.
	 * @param delete the delete to set
	 */
	public void setDelete(Boolean delete) {
		this.delete = delete;
	}

	private Boolean create = false;
	private Boolean delete = false;
}
