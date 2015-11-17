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
	 * @param study study
	 * @param pager a pager
	 * @return number of roles
	 */
	Long getStudyRoleCount(Study study, CasePager pager);

	/**
	 * Retrieves all the roles from the repository for a given study
	 * @param study study
	 * @param pager a pager
	 * @return a list of roles
	 * @throws RepositoryException if a repository exception occurred
	 */
	List<Role> getStudyRoles(Study study, CasePager pager) throws RepositoryException;
	
	/**
	 * Retrieves a role by name.
	 * @param study study
	 * @param name the name of a role
	 * @return the role
	 * @throws RepositoryException if a repository exception occurred
	 */
	Role getStudyRole(Study study, String name) throws RepositoryException;

	/**
	 * Retrieves a study role by internal identifier.
	 * @param study study
	 * @param id the role identifier
	 * @return the role
	 * @throws RepositoryException if a repository exception occurred
	 */
	Role getStudyRoleById(Study study, Integer id) throws RepositoryException;

	/**
	 * Deletes a role.
	 * @param study study
	 * @param role the role
	 * @throws RepositoryException if a repository exception occurred
	 */
	void deleteStudyRole(Study study, Role role) throws RepositoryException;

	/**
	 * Saves (and possibly creates) a role.
	 * @param study study
	 * @param role the role
	 * @throws RepositoryException if a repository exception occurred
	 */
	void saveStudyRole(Study study, Role role) throws RepositoryException;
}
