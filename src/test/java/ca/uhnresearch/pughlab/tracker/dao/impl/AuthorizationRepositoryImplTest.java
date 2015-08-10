package ca.uhnresearch.pughlab.tracker.dao.impl;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/testContextDatabase.xml" })
public class AuthorizationRepositoryImplTest {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private AuthorizationRepositoryImpl authorizationRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Checks that the number of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleCount() {
		CaseQuery query = new CaseQuery();

		Long count = authorizationRepository.getRoleCount(query);
		Assert.assertEquals(4, count.longValue());
	}

	/**
	 * Checks that the number of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleCountWithPattern() {
		CaseQuery query = new CaseQuery();
		query.setPattern("DEMO");

		Long count = authorizationRepository.getRoleCount(query);
		Assert.assertEquals(3, count.longValue());
	}

	/**
	 * Checks that a list of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoles() throws RepositoryException {
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);

		List<Role> list = authorizationRepository.getRoles(query);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
	}

	/**
	 * Checks that a list of roles is returned correctly with a case query and offset.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRolesQuery() throws RepositoryException {
		CaseQuery query = new CaseQuery();
		query.setOffset(2);

		List<Role> list = authorizationRepository.getRoles(query);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}

	/**
	 * Checks that a list of roles is returned correctly with a case query and offset.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRolesQueryPattern() throws RepositoryException {
		CaseQuery query = new CaseQuery();
		query.setPattern("DEMO");

		List<Role> list = authorizationRepository.getRoles(query);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
	}

	/**
	 * Checks that a role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleByName() throws RepositoryException {
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
	public void testGetRoleByNameMissing() throws RepositoryException {
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getRole("ROLE_CAST_HERDER");
		Assert.assertNull(role);
	}

	/**
	 * Checks that a list of users for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleUsers() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		List<String> list = authorizationRepository.getRoleUsers(role);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("admin", list.get(0));
	}

	/**
	 * Checks that a list of permissions for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRolePermissions() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_DEMO_TRACK");
		List<String> list = authorizationRepository.getRolePermissions(role);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());		
		Assert.assertEquals("study:read:DEMO", list.get(0));
		Assert.assertEquals("view:read:DEMO-track", list.get(1));
		Assert.assertEquals("view:write:DEMO-track", list.get(2));
	}

	/**
	 * Checks that a role can be deleted successfully.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteRole() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		Assert.assertNotNull(role);

		authorizationRepository.deleteRole(role);
		
		Role search = authorizationRepository.getRole("ROLE_ADMIN");
		Assert.assertNull(search);
	}
	
	/**
	 * Checks that a role can be renamed successfully.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testRenameRole() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		Assert.assertNotNull(role);

		role.setName("ROLE_CAT_HERDER");
		authorizationRepository.saveRole(role);
		
		Role search = authorizationRepository.getRole("ROLE_CAT_HERDER");
		Assert.assertNotNull(search);
		Assert.assertEquals(role.getId(), search.getId());
	}

	/**
	 * Checks that a role can be created successfully.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testCreateRole() throws RepositoryException {
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		authorizationRepository.saveRole(role);
		
		Role search = authorizationRepository.getRole("ROLE_CAT_HERDER");
		Assert.assertNotNull(search);
		Assert.assertNotNull(search.getId());
	}

	/**
	 * Checks that a role that already exists will throw something.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testCreateExistingRole() throws RepositoryException {
		Role role = new Role();
		role.setName("ROLE_ADMIN");
		
		thrown.expect(RuntimeException.class);

		authorizationRepository.saveRole(role);
	}
	
	/**
	 * Checks that a list of users for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetRoleUsers() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		List<String> users = new ArrayList<String>();
		users.add("morag");
		users.add("mungo");
		users.add("misty");
		authorizationRepository.setRoleUsers(role, users);
		
		List<String> list = authorizationRepository.getRoleUsers(role);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("morag", list.get(0));
		Assert.assertEquals("mungo", list.get(1));
		Assert.assertEquals("misty", list.get(2));
	}

	/**
	 * Checks that a list of users for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetRolePermissions() throws RepositoryException {
		Role role = authorizationRepository.getRole("ROLE_ADMIN");
		List<String> permissions = new ArrayList<String>();
		permissions.add("study:X:read");
		permissions.add("study:X:write");
		authorizationRepository.setRolePermissions(role, permissions);
		
		List<String> list = authorizationRepository.getRolePermissions(role);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("study:X:read", list.get(0));
		Assert.assertEquals("study:X:write", list.get(1));
	}
}
