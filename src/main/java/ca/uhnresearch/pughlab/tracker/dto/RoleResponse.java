package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleResponse extends AbstractResponse {

	private Role role;

	/**
	 * @return the role
	 */
	@JsonProperty
	public Role getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}
}
