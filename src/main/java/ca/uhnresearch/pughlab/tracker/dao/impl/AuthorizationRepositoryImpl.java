package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QRole.roles;
import static ca.uhnresearch.pughlab.tracker.domain.QUserRole.userRoles;
import static ca.uhnresearch.pughlab.tracker.domain.QRolePermission.rolePermissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;
import org.springframework.data.jdbc.query.SqlInsertCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;

public class AuthorizationRepositoryImpl implements AuthorizationRepository {

	private final Logger logger = LoggerFactory.getLogger(AuthorizationRepositoryImpl.class);

	private QueryDslJdbcTemplate template;

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    public QueryDslJdbcTemplate getTemplate() {
        return template;
    }

    /**
     * Returns a list of roles
     */
	@Override
	public List<Role> getRoles(CaseQuery query) throws RepositoryException {
    	SQLQuery sqlQuery = template.newSqlQuery().from(roles).orderBy(roles.name.asc());
    	
		if (query.getOffset() != null) {
			sqlQuery = sqlQuery.offset(query.getOffset());
		}
		if (query.getLimit() != null) {
			sqlQuery = sqlQuery.limit(query.getLimit());
		}

    	List<Role> roleList = template.query(sqlQuery, roles);
		return roleList;
	}
	
	/**
	 * Finds and returns a role by name
	 */
	@Override
	public Role getRole(String name) throws RepositoryException {
		logger.debug("Looking for role by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(roles).where(roles.name.eq(name));
    	Role role = template.queryForObject(sqlQuery, roles);
    	
    	if (role != null) {
    		logger.debug("Got a role: {}", role.toString());
    	} else {
    		logger.debug("No role found");
    	}
    	
    	return role;
	}
	
	/**
	 * Deletes a role. Also deletes all the associations between the given role and 
	 * all related users and associated permissions, so not to be done lightly.
	 */
	@Override
	public void deleteRole(final Role role) throws RepositoryException {
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
	public void saveRole(final Role role) throws RepositoryException {
		if (role.getId() != null) {
			
			// We have an identifier, so we're updating the role -- basically this is a rename
			template.update(roles, new SqlUpdateCallback() { 
				public long doInSqlUpdateClause(SQLUpdateClause sqlUpdateClause) {
					return sqlUpdateClause.where(roles.id.eq(role.getId())).populate(role).execute();
				};
			});
			
		} else {
			
			// The identifier is null, let's create a new role
			template.insert(roles, new SqlInsertCallback() { 
				public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
					return sqlInsertClause.populate(role).execute();
				};
			});
		}
		
	}
	
	/**
	 * Returns a list of users associated with a role
	 */
	@Override
	public List<String> getRoleUsers(Role role) throws RepositoryException {
    	SQLQuery sqlQuery = template.newSqlQuery().from(userRoles).where(userRoles.roleId.eq(role.getId()));
    	List<String> userList = template.query(sqlQuery, userRoles.username);
    	logger.debug("Got some users: {}", userList.toString());

		return userList;
	}

	@Override
	public List<String> getRolePermissions(Role role) {
    	SQLQuery sqlQuery = template.newSqlQuery().from(rolePermissions).where(rolePermissions.roleId.eq(role.getId()));
    	List<String> permissionList = template.query(sqlQuery, rolePermissions.permission);
    	logger.debug("Got some permissions: {}", permissionList.toString());

		return permissionList;
	}

	@Override
	public void setRoleUsers(final Role role, final List<String> users) throws RepositoryException {
		
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
				for(String s : users) {
					sqlInsertClause.set(userRoles.roleId, role.getId()).set(userRoles.username, s).addBatch();
				}
				return sqlInsertClause.execute();
			};
		});
	}

	@Override
	public void setRolePermissions(final Role role, final List<String> permissions) throws RepositoryException {
		
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
				for(String s : permissions) {
					sqlInsertClause.set(rolePermissions.roleId, role.getId()).set(rolePermissions.permission, s).addBatch();
				}
				return sqlInsertClause.execute();
			};
		});
	}
}
