package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AdministratorPermissions {

	private Boolean admin = false;

	/**
	 * Retrieves the admin permission.
	 * @return the admin
	 */
	@JsonProperty
	public Boolean getAdmin() {
		return admin;
	}

	/**
	 * Sets the admin permission.
	 * @param admin the admin to set
	 */
	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
}
