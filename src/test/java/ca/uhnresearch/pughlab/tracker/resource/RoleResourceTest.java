package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.eq;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RoleResourceTest extends AbstractShiroTest {

	private RoleResource resource;

	private Gson gson = new Gson();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		resource = new RoleResource();
		Request request = new Request(Method.GET, "http://localhost:9998/admin/roles");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		resource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }

	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setUsers(new ArrayList<String>());
		role.getUsers().add("user1");
		role.getUsers().add("user2");
		role.setPermissions(new ArrayList<String>());
		role.getPermissions().add("*:*");

		resource.getRequest().getAttributes().put("role", role);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		resource.setRepository(mock);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("role").isJsonObject() );
		assertEquals( 1234, data.get("role").getAsJsonObject().get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", data.get("role").getAsJsonObject().get("name").getAsString() );
		
		JsonObject roleData = data.get("role").getAsJsonObject();
		
		assertTrue( roleData.get("users").isJsonArray() );
		assertEquals( 2, roleData.get("users").getAsJsonArray().size() );
		assertEquals( "user1", roleData.get("users").getAsJsonArray().get(0).getAsString() );
		assertEquals( "user2", roleData.get("users").getAsJsonArray().get(1).getAsString() );
		
		assertTrue( roleData.get("permissions").isJsonArray() );
		assertEquals( 1, roleData.get("permissions").getAsJsonArray().size() );
		assertEquals( "*:*", roleData.get("permissions").getAsJsonArray().get(0).getAsString() );
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceForbiddenTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setUsers(new ArrayList<String>());
		role.getUsers().add("user1");
		role.getUsers().add("user2");
		role.setPermissions(new ArrayList<String>());
		role.getPermissions().add("*:*");
		resource.getRequest().getAttributes().put("role", role);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		resource.setRepository(mock);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceStudyTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setUsers(new ArrayList<String>());
		role.getUsers().add("user1");
		role.getUsers().add("user2");
		role.setPermissions(new ArrayList<String>());
		role.getPermissions().add("*:*");
		resource.getRequest().getAttributes().put("study", study);
		resource.getRequest().getAttributes().put("role", role);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		resource.setRepository(mock);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("role").isJsonObject() );
		assertEquals( 1234, data.get("role").getAsJsonObject().get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", data.get("role").getAsJsonObject().get("name").getAsString() );
		
		JsonObject roleData = data.get("role").getAsJsonObject();
		
		assertTrue( roleData.get("users").isJsonArray() );
		assertEquals( 2, roleData.get("users").getAsJsonArray().size() );
		assertEquals( "user1", roleData.get("users").getAsJsonArray().get(0).getAsString() );
		assertEquals( "user2", roleData.get("users").getAsJsonArray().get(1).getAsString() );
		
		assertTrue( roleData.get("permissions").isJsonArray() );
		assertEquals( 1, roleData.get("permissions").getAsJsonArray().size() );
		assertEquals( "*:*", roleData.get("permissions").getAsJsonArray().get(0).getAsString() );
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceStudyForbiddenTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setUsers(new ArrayList<String>());
		role.getUsers().add("user1");
		role.getUsers().add("user2");
		role.setPermissions(new ArrayList<String>());
		role.getPermissions().add("*:*");
		resource.getRequest().getAttributes().put("study", study);
		resource.getRequest().getAttributes().put("role", role);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		resource.setRepository(mock);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourcePutTest() throws IOException, RepositoryException {

		Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setUsers(new ArrayList<String>());
		role.getUsers().add("user1");
		role.getUsers().add("user2");
		role.setPermissions(new ArrayList<String>());
		role.getPermissions().add("*:*");
		
		resource.getRequest().getAttributes().put("study", study);
		resource.getRequest().getAttributes().put("role", role);

		Role renamed = new Role();
		renamed.setName("X");
		renamed.setId(1234);
		renamed.setUsers(new ArrayList<String>());
		renamed.getUsers().add("user1");
		renamed.getUsers().add("user2");
		renamed.setPermissions(new ArrayList<String>());
		renamed.getPermissions().add("*:*");
		
		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		mock.saveStudyRole(eq(study), anyObject(Role.class));
		expectLastCall();
		expect(mock.getStudyRole(eq(study), eq("X"))).andStubReturn(renamed);
		replay(mock);
		resource.setRepository(mock);
		
		Gson gson = new Gson();
		JsonObject writeData = gson.fromJson("{\"role\": {\"id\": 1234, \"name\": \"X\"}}", JsonObject.class);		
		Representation input = new StringRepresentation(writeData.toString(), APPLICATION_JSON);   

		Representation result = resource.putResource(input);
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);

		assertTrue( data.get("role").isJsonObject() );
		assertEquals( 1234, data.get("role").getAsJsonObject().get("id").getAsInt() );
		assertEquals( "X", data.get("role").getAsJsonObject().get("name").getAsString() );

	}
}
