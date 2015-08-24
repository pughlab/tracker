package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;

/**
 * Interface for access to the authorization storage. This deliberately doesn't
 * represent user objects, as that's authentication. However, roles are managed
 * within the tracker and this manages role permissions in a way that is 
 * decoupled from the main tracker storage. 
 * 
 * @author stuartw
 */
public interface AuthorizationRepository {

	/**
	 * Retrieves the number of matching roles in the repository
	 * @return number of roles
	 */
	Long getRoleCount(CaseQuery query);

	/**
	 * Retrieves all the roles from the repository
	 */
	List<Role> getRoles(CaseQuery query) throws RepositoryException;
	
	/**
	 * Retrieves all the roles from the repository for a given study
	 */
	List<Role> getStudyRoles(Study study, CaseQuery query) throws RepositoryException;
	
	/**
	 * Retrieves a role by name
	 */
	Role getRole(String name) throws RepositoryException;
	
	/**
	 * Retrieves a role by name
	 */
	Role getStudyRole(Study study, String name) throws RepositoryException;

	/**
	 * Deletes a role
	 */
	void deleteRole(Role role) throws RepositoryException;

	/**
	 * Saves (and possibly creates) a role
	 */
	void saveRole(Role role) throws RepositoryException;

	/**
	 * Retrieves the list of users associated with a role
	 */
	List<String> getRoleUsers(Role role) throws RepositoryException;

	/**
	 * Updates the list of users associated with a role
	 */
	void setRoleUsers(Role role, List<String> users) throws RepositoryException;
	
	/**
	 * Retrieves the list of roles associated with a role
	 */
	List<String> getRolePermissions(Role role) throws RepositoryException;

	/**
	 * Updates the list of permissions associated with a role
	 */
	void setRolePermissions(Role role, List<String> permissions) throws RepositoryException;

}
