package ca.uhnresearch.pughlab.tracker.dao.impl;

import junit.framework.Assert;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Role;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/applicationContextDatabase.xml" })
public class AuthorizationRepositoryImplTest {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private AuthorizationRepositoryImpl authorizationRepository;

	/**
	 * Checks that a list of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoles() {
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);

		List<Role> list = authorizationRepository.getRoles(query);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
	}

	/**
	 * Checks that a role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleByName() {
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		Assert.assertNotNull(role);
		Assert.assertEquals("ROLE_ADMIN", role.getName());
	}

	/**
	 * Checks that a role that doesn't exist is not found.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleByNameMissing() {
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getRole("ROLE_CAST_HERDER");
		Assert.assertNull(role);
	}
}
