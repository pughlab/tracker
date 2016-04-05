package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QRole.roles;
import static ca.uhnresearch.pughlab.tracker.domain.QUserRole.userRoles;
import static ca.uhnresearch.pughlab.tracker.domain.QRolePermission.rolePermissions;
import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;
import static ca.uhnresearch.pughlab.tracker.domain.QUser.users;

import java.text.MessageFormat;
import java.util.List;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlInsertWithKeyCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.DataIntegrityException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.User;
import ca.uhnresearch.pughlab.tracker.security.JdbcAuthorizingRealm;

public class AuthorizationRepositoryImpl implements AuthorizationRepository {

	private final Logger logger = LoggerFactory.getLogger(AuthorizationRepositoryImpl.class);

	private QueryDslJdbcTemplate template;
	
	private JdbcAuthorizingRealm authorizationRealm;

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    public void setAuthorizingRealm(JdbcAuthorizingRealm authorizationRealm) {
        this.authorizationRealm = authorizationRealm;
    }

    public QueryDslJdbcTemplate getTemplate() {
        return template;
    }


	@Override
	public Long getStudyRoleCount(Study study, CasePager query) {
		SQLQuery sqlQuery = template.newSqlQuery().from(roles).where(roles.studyId.eq(study.getId()));
		return template.count(sqlQuery);
	}
	
	private SQLQuery buildRolesQuery(Study study, CasePager query) throws RepositoryException {
    	SQLQuery sqlQuery = template.newSqlQuery()
				.from(roles)
				.leftJoin(studies)
				.on(roles.studyId.eq(studies.id));
    	
    	if (study != null) {
    		sqlQuery = sqlQuery.where(roles.studyId.eq(study.getId()));
    	}
    	
    	sqlQuery = sqlQuery.orderBy(roles.name.asc());

		if (query.getOffset() != null) {
			sqlQuery = sqlQuery.offset(query.getOffset());
		}
		if (query.getLimit() != null) {
			sqlQuery = sqlQuery.limit(query.getLimit());
		}

		return sqlQuery;
	}
	
    /**
     * Returns a list of roles for a given study
     */
	@Override
	public List<Role> getStudyRoles(Study study, CasePager query) throws RepositoryException {
    	SQLQuery sqlQuery = buildRolesQuery(study, query);
    	List<Role> roleList = template.query(sqlQuery, new RoleStudyProjection(roles, studies));
    	for(Role role : roleList) {
        	role.setUsers(getRoleUsers(role));
        	role.setPermissions(getRolePermissions(role));
    	}
		return roleList;
	}
	
	/**
	 * Finds and returns a role by name for a given study
	 */
	@Override
	public Role getStudyRole(Study study, String name) throws RepositoryException {
		logger.debug("Looking for study role by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery()
    			.from(roles)
    			.join(studies)
    			.on(roles.studyId.eq(studies.id))
    			.where(roles.name.eq(name).and(roles.studyId.eq(study.getId())));
    	Role role = template.queryForObject(sqlQuery, new RoleStudyProjection(roles, studies));
    	if (role != null) {
    		role.setUsers(getRoleUsers(role));
    		role.setPermissions(getRolePermissions(role));
    	}
    	return role;
	}


	@Override
	public Role getStudyRoleById(Study study, Integer id) throws RepositoryException {
		logger.debug("Looking for study role by id: {}", id);
    	SQLQuery sqlQuery = template.newSqlQuery()
    			.from(roles)
    			.join(studies)
    			.on(roles.studyId.eq(studies.id))
    			.where(roles.id.eq(id).and(roles.studyId.eq(study.getId())));
    	Role role = template.queryForObject(sqlQuery, new RoleStudyProjection(roles, studies));
    	if (role != null) {
    		role.setUsers(getRoleUsers(role));
    		role.setPermissions(getRolePermissions(role));
    	}
    	return role;
	}

	
	/**
	 * Deletes a role. Also deletes all the associations between the given role and 
	 * all related users and associated permissions, so not to be done lightly.
	 */
	@Override
	public void deleteStudyRole(Study study, final Role role) throws RepositoryException {
		
		// Before we delete, clear authorization for existing users
		clearRoleAuthorizationCache(role);
		
		template.delete(rolePermissions, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(rolePermissions.roleId.eq(role.getId())).execute();
			};
		});
		template.delete(userRoles, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(userRoles.roleId.eq(role.getId())).execute();
			};
		});
		template.delete(roles, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(roles.id.eq(role.getId())).execute();
			};
		});
	}

	/**
	 * Saves (and possibly creates) a role
	 */
	@Override
	public void saveStudyRole(Study study, final Role role) throws RepositoryException {
		
		try {
			// Make sure the study id is correct
			role.setStudyId(study.getId());
			
			if (role.getId() != null) {
	
				clearRoleAuthorizationCache(role);
				
				// We have an identifier, so we're updating the role -- basically this is a rename
				template.update(roles, new SqlUpdateCallback() { 
					public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
						return sqlUpdateClause.where(roles.id.eq(role.getId())).populate(role).execute();
					};
				});
				
			} else {
				
				Integer roleId = template.insertWithKey(roles, new SqlInsertWithKeyCallback<Integer>() { 
					public Integer doInSqlInsertWithKeyClause(SQLInsertClause sqlInsertClause) {
						return sqlInsertClause.populate(role).executeWithKey(roles.id);
					};
				});
				
				role.setId(roleId);
			}
			
			saveRoleUsers(role);
			saveRolePermissions(role);
	
			clearRoleAuthorizationCache(role);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException(e.getMessage());
		}
	}
	
	private void clearRoleAuthorizationCache(final Role role) throws RepositoryException {
		if (authorizationRealm == null) {
			return;
		}
		List<String> users = getRoleUsers(role);
		String realmName = authorizationRealm.getName();
		for(String user: users) {
			PrincipalCollection principal = new SimplePrincipalCollection(user, realmName);
			authorizationRealm.clearCachedAuthorizationInfo(principal);
		}
	}
	
	/**
	 * Returns a list of users associated with a role
	 */
	private List<String> getRoleUsers(Role role) throws RepositoryException {
    	SQLQuery sqlQuery = template.newSqlQuery().from(userRoles).where(userRoles.roleId.eq(role.getId()));
    	List<String> userList = template.query(sqlQuery, userRoles.username);
		return userList;
	}

	private List<String> getRolePermissions(Role role) {
    	SQLQuery sqlQuery = template.newSqlQuery().from(rolePermissions).where(rolePermissions.roleId.eq(role.getId()));
    	List<String> permissionList = template.query(sqlQuery, rolePermissions.permission);
		return permissionList;
	}

	private void saveRoleUsers(final Role role) throws RepositoryException {
		
		if (role.getId() == null) {
			throw new NotFoundException("Can't find role");
		}
		
		// First of all, let's remove the current list of users.
		template.delete(userRoles, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(userRoles.roleId.eq(role.getId())).execute();
			};
		});

		// Now we can add all the users back in again.
		template.insert(userRoles, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				for(String s : role.getUsers()) {
					sqlInsertClause.set(userRoles.roleId, role.getId()).set(userRoles.username, s).addBatch();
				}
				if (sqlInsertClause.isEmpty()) {
					return 0;
				} else {
					return sqlInsertClause.execute();
				}
			};
		});
	}

	private void saveRolePermissions(final Role role) throws RepositoryException {
		
		if (role.getId() == null) {
			throw new NotFoundException("Can't find role");
		}

		// First of all, let's remove the current list of permissions.
		template.delete(rolePermissions, new SqlDeleteCallback() { 
			public long doInSqlDeleteClause(SQLDeleteClause sqlDeleteClause) {
				return sqlDeleteClause.where(rolePermissions.roleId.eq(role.getId())).execute();
			};
		});

		// Now we can add all the users back in again.
		template.insert(rolePermissions, new SqlInsertCallback() { 
			public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
				for(String s : role.getPermissions()) {
					sqlInsertClause.set(rolePermissions.roleId, role.getId()).set(rolePermissions.permission, s).addBatch();
				}
				if (sqlInsertClause.isEmpty()) {
					return 0;
				} else {
					return sqlInsertClause.execute();
				}
			};
		});
	}

	@Override
	public User getUserByUsername(String username) throws RepositoryException {
    	SQLQuery sqlQuery = template.newSqlQuery().from(users).where(users.username.eq(username));
    	User user = template.queryForObject(sqlQuery, users);
		return user;
	}

	@Override
	public void saveUser(final User user) throws RepositoryException {
		// If the user doesn't exist, we should create it. Otherwise we can update it.
		// Essentially this allows us to start building a populated user table on login. 
		
		try {
			long updateCount = template.update(users, new SqlUpdateCallback() { 
				public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
					return sqlUpdateClause.where(users.username.eq(user.getUsername())).populate(user).execute();
				};
			});
			
			if (updateCount >= 1) return;
			updateCount = template.insert(users, new SqlInsertCallback() { 
				public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
					return sqlInsertClause.populate(user).execute();
				};
			});
			
			if (updateCount == 1) return;
			String message = MessageFormat.format("Failed to create or update a user: {0}", user.getUsername());
			throw new DataIntegrityException(message);
			
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException(e.getMessage());
		}
	}

}
