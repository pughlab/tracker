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
	Long getStudyRoleCount(Study study, CaseQuery query);

	/**
	 * Retrieves all the roles from the repository for a given study
	 */
	List<Role> getStudyRoles(Study study, CaseQuery query) throws RepositoryException;
	
	/**
	 * Retrieves a role by name
	 */
	Role getStudyRole(Study study, String name) throws RepositoryException;

	/**
	 * Retrieves a study role by internal identifier
	 */
	Role getStudyRoleById(Study study, Integer id) throws RepositoryException;

	/**
	 * Deletes a role
	 */
	void deleteStudyRole(Study study, Role role) throws RepositoryException;

	/**
	 * Saves (and possibly creates) a role
	 */
	void saveStudyRole(Study study, Role role) throws RepositoryException;
}
