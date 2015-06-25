package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.User;

public interface AuthorizationRepository {

	/**
	 * Retrieves all the users from the repository
	 * @return list of users
	 */
	List<User> getUsers(CaseQuery query);

	/**
	 * Retrieves all the roles from the repository
	 */
	List<Role> getRoles(CaseQuery query);
	
	/**
	 * Retrieves the list of roles associated with a user
	 */
	List<Role> getUserRoles(User user);

	/**
	 * Updates the list of roles associated with a user
	 */
	void setUserRoles(User user, List<Role> roles);
	
	/**
	 * Retrieves the list of roles associated with a role
	 */
	List<String> getRolePermissions(Role role);

	/**
	 * Updates the list of permissions associated with a role
	 */
	void setRolePermissions(Role role, List<String> permissions);

}
