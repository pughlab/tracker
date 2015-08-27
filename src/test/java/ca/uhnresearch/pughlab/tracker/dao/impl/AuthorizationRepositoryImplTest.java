package ca.uhnresearch.pughlab.tracker.dao.impl;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.eq;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.After;
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

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.security.JdbcAuthorizingRealm;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/testContextDatabase.xml" })
public class AuthorizationRepositoryImplTest {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private AuthorizationRepositoryImpl authorizationRepository;
	
	/**
	 * Clean up the AuthorizingRealm if we set one in testing.
	 */
	@After 
	public void tearDown() {
		authorizationRepository.setAuthorizingRealm(null);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * Checks that the number of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleCount() {
		CasePager query = new CasePager();
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		Long count = authorizationRepository.getStudyRoleCount(study, query);
		Assert.assertEquals(3, count.longValue());
	}

	/**
	 * Checks that a list of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoles() throws RepositoryException {
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		List<Role> list = authorizationRepository.getStudyRoles(study, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
	}

	/**
	 * Checks that the required roles co.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleStudyNames() throws RepositoryException {
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(2);
		replay(study);

		List<Role> list = authorizationRepository.getStudyRoles(study, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("SECOND", list.get(0).getStudyName());
		Assert.assertEquals("SECOND", list.get(1).getStudyName());
	}

	/**
	 * Checks that a list of roles is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyRoles() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);
		
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		List<Role> list = authorizationRepository.getStudyRoles(study, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());

		Assert.assertEquals("ROLE_DEMO_ADMIN", list.get(0).getName());
		Assert.assertEquals("ROLE_DEMO_READ", list.get(1).getName());
		Assert.assertEquals("ROLE_DEMO_TRACK", list.get(2).getName());

		Assert.assertEquals("DEMO", list.get(0).getStudyName());
		Assert.assertEquals("DEMO", list.get(1).getStudyName());
		Assert.assertEquals("DEMO", list.get(2).getStudyName());
	}

	/**
	 * Checks that a list of roles is returned correctly with a case query and offset.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRolesQuery() throws RepositoryException {
		CasePager query = new CasePager();
		query.setOffset(1);

		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);
		
		List<Role> list = authorizationRepository.getStudyRoles(study, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}

	/**
	 * Checks that a role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleByName() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(0);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_ADMIN");
		Assert.assertNotNull(role);
		Assert.assertEquals("ROLE_ADMIN", role.getName());
	}

	/**
	 * Checks that a role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleById() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRoleById(study, 3);
		Assert.assertNotNull(role);
		Assert.assertEquals("ROLE_DEMO_TRACK", role.getName());
		Assert.assertEquals("DEMO", role.getStudyName());
	}

	/**
	 * Checks that a study role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyRoleByNameForNonStudyRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_ADMIN");
		Assert.assertNull(role);
	}

	/**
	 * Checks that a study role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyRoleByNameForStudyRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_DEMO_TRACK");
		Assert.assertNotNull(role);
		Assert.assertEquals("ROLE_DEMO_TRACK", role.getName());
		Assert.assertEquals("DEMO", role.getStudyName());
	}

	/**
	 * Checks that a study role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyRoleByIdForNonStudyRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRoleById(study, 1);
		Assert.assertNull(role);
	}

	/**
	 * Checks that a study role is found by name correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyRoleByIdForStudyRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRoleById(study, 3);
		Assert.assertNotNull(role);
		Assert.assertEquals("ROLE_DEMO_TRACK", role.getName());
		Assert.assertEquals("DEMO", role.getStudyName());
	}

	/**
	 * Checks that a role that doesn't exist is not found.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleByNameMissing() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(0);
		replay(study);

		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(10);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_CAST_HERDER");
		Assert.assertNull(role);
	}

	/**
	 * Checks that a list of users for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRoleUsers() throws RepositoryException {

		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(0);
		replay(study);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_ADMIN");
		List<String> list = role.getUsers();
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		Assert.assertEquals("morungos@gmail.com", list.get(0));
	}

	/**
	 * Checks that a list of permissions for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetRolePermissions() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		replay(study);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_DEMO_TRACK");
		List<String> list = role.getPermissions();
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());		
		Assert.assertEquals("read:track", list.get(0));
		Assert.assertEquals("write:track", list.get(1));
	}

	/**
	 * Checks that a role can be deleted successfully.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);
		
		JdbcAuthorizingRealm realm = createMock(JdbcAuthorizingRealm.class);
		expect(realm.getName()).andStubReturn("mockRealm");
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("stuart", "mockRealm")));
		expectLastCall();
		replay(realm);
		
		authorizationRepository.setAuthorizingRealm(realm);
		
		Role role = authorizationRepository.getStudyRole(study, "ROLE_DEMO_ADMIN");
		Assert.assertNotNull(role);

		authorizationRepository.deleteStudyRole(study, role);
		
		Role search = authorizationRepository.getStudyRole(study, "ROLE_DEMO_ADMIN");
		Assert.assertNull(search);
		
		verify(realm);
	}
	
	/**
	 * Checks that a role can be renamed successfully.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testRenameRole() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_DEMO_ADMIN");
		Assert.assertNotNull(role);

		role.setName("ROLE_CAT_HERDER");
		authorizationRepository.saveStudyRole(study, role);
		
		Role search = authorizationRepository.getStudyRole(study, "ROLE_CAT_HERDER");
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
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		authorizationRepository.saveStudyRole(study, role);
		
		Role search = authorizationRepository.getStudyRole(study, "ROLE_CAT_HERDER");
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
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		Role role = new Role();
		role.setName("ROLE_DEMO_TRACK");
		
		thrown.expect(RuntimeException.class);

		authorizationRepository.saveStudyRole(study, role);
	}
	
	/**
	 * Checks that a list of users for a role is returned correctly.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetRoleUsers() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(1);
		replay(study);

		JdbcAuthorizingRealm realm = createMock(JdbcAuthorizingRealm.class);
		expect(realm.getName()).andStubReturn("mockRealm");
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("morungos@gmail.com", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("admin", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("stuartw@ads.uhnresearch.ca", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("oidcprofile#stuartw", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("morag", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("mungo", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("misty", "mockRealm")));
		expectLastCall();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("stuart", "mockRealm")));
		expectLastCall();
		replay(realm);
		
		authorizationRepository.setAuthorizingRealm(realm);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_DEMO_ADMIN");
		List<String> users = new ArrayList<String>();
		users.add("morag");
		users.add("mungo");
		users.add("misty");
		role.setUsers(users);
		authorizationRepository.saveStudyRole(study, role);
		
		Role loadedRole = authorizationRepository.getStudyRole(study, role.getName());
		
		List<String> list = loadedRole.getUsers();
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
		
		Study study = createMock(Study.class);
		expect(study.getId()).andStubReturn(0);
		replay(study);

		JdbcAuthorizingRealm realm = createMock(JdbcAuthorizingRealm.class);
		expect(realm.getName()).andStubReturn("mockRealm");
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("morungos@gmail.com", "mockRealm")));
		expectLastCall().anyTimes();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("admin", "mockRealm")));
		expectLastCall().anyTimes();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("stuartw@ads.uhnresearch.ca", "mockRealm")));
		expectLastCall().anyTimes();
		realm.clearCachedAuthorizationInfo(eq(new SimplePrincipalCollection("oidcprofile#stuartw", "mockRealm")));
		expectLastCall().anyTimes();
		replay(realm);
		
		authorizationRepository.setAuthorizingRealm(realm);

		Role role = authorizationRepository.getStudyRole(study, "ROLE_ADMIN");
		List<String> permissions = new ArrayList<String>();
		permissions.add("X:read");
		permissions.add("X:write");
		role.setPermissions(permissions);
		authorizationRepository.saveStudyRole(study, role);
		
		Role loadedRole = authorizationRepository.getStudyRole(study, role.getName());

		List<String> list = loadedRole.getPermissions();
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("X:read", list.get(0));
		Assert.assertEquals("X:write", list.get(1));
	}
}
