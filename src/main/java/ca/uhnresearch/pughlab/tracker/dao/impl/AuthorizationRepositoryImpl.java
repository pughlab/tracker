package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.User;

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

	@Override
	public List<User> getUsers(CaseQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Role> getRoles(CaseQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Role> getUserRoles(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserRoles(User user, List<Role> roles) {
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
