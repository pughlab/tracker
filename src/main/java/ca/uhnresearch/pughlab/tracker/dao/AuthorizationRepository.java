package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.Role;

public interface AuthorizationRepository {

	/**
	 * Retrieves all the roles from the repository
	 */
	List<Role> getRoles(CaseQuery query);
	
	/**
	 * Retrieves a role by name
	 */
	Role getRole(String name);
	
	/**
	 * Deletes a role
	 */
	void deleteRole(Role role);

	/**
	 * Retrieves the list of users associated with a role
	 */
	List<String> getRoleUsers(Role role);

	/**
	 * Updates the list of users associated with a role
	 */
	void setRoleUsers(Role role, List<String> users);
	
	/**
	 * Retrieves the list of roles associated with a role
	 */
	List<String> getRolePermissions(Role role);

	/**
	 * Updates the list of permissions associated with a role
	 */
	void setRolePermissions(Role role, List<String> permissions);

}
