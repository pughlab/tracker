package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AdministratorPermissions {

	private Boolean adminAllowed = false;

	/**
	 * @return the adminAllowed
	 */
	@JsonProperty
	public Boolean getAdminAllowed() {
		return adminAllowed;
	}

	/**
	 * @param adminAllowed the adminAllowed to set
	 */
	public void setAdminAllowed(Boolean adminAllowed) {
		this.adminAllowed = adminAllowed;
	}


}
