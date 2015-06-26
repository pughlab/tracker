package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QRole.roles;
import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;

import com.mysema.query.sql.SQLQuery;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;

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
		// TODO Auto-generated method stub
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

	@Override
	public List<String> getRoleUsers(Role role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRoleUsers(Role role, List<String> users) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getRolePermissions(Role role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRolePermissions(Role role, List<String> permissions) {
		// TODO Auto-generated method stub

	}

}
