package ca.uhnresearch.pughlab.tracker.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleResponse extends AbstractResponse {

	private Role role;

	private List<String> users;

	private List<String> permissions;

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

	/**
	 * @return the users
	 */
	@JsonProperty
	public List<String> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(List<String> users) {
		this.users = users;
	}

	/**
	 * @return the permissions
	 */
	@JsonProperty
	public List<String> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
