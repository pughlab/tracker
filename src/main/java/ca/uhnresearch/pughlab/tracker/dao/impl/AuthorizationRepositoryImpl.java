package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QRole.roles;
import static ca.uhnresearch.pughlab.tracker.domain.QUserRole.userRoles;
import static ca.uhnresearch.pughlab.tracker.domain.QView.views;
import static ca.uhnresearch.pughlab.tracker.domain.QRolePermission.rolePermissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlDeleteCallback;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

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
	public List<Role> getRoles(CaseQuery query) {
    	SQLQuery sqlQuery = template.newSqlQuery().from(roles);
    	List<Role> roleList = template.query(sqlQuery, roles);
		return roleList;
	}
	
	/**
	 * Finds and returns a role by name
	 */
	@Override
	public Role getRole(String name) {
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
	public void deleteRole(final Role role) {
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
	 * Returns a list of users associated with a role
	 */
	@Override
	public List<String> getRoleUsers(Role role) {
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
	public void setRoleUsers(Role role, List<String> users) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRolePermissions(Role role, List<String> permissions) {
		// TODO Auto-generated method stub

	}

}
