package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
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
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;
import static org.restlet.data.MediaType.APPLICATION_JSON;

public class RoleListResourceTest extends AbstractShiroTest {

	private RoleListResource resource;

	private Gson gson = new Gson();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		resource = new RoleListResource();
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

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		replay(mock);

        resource.getRequest().getAttributes().put("query", new CaseQuery());
        resource.getRequest().getAttributes().put("study", study);
        resource.setRepository(mock);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("roles").isJsonArray() );
		assertEquals( 1, data.get("roles").getAsJsonArray().size() );
		
		JsonObject jsonRole = data.get("roles").getAsJsonArray().get(0).getAsJsonObject();
		assertEquals( 1234, jsonRole.get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", jsonRole.get("name").getAsString() );
		
		assertTrue( data.get("counts").isJsonObject() );
		assertTrue( data.get("counts").getAsJsonObject().get("total").isJsonPrimitive() );
		assertEquals( 1, data.get("counts").getAsJsonObject().get("total").getAsInt() );
	}

	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceForbiddenTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		replay(mock);

        resource.getRequest().getAttributes().put("query", new CaseQuery());
        resource.getRequest().getAttributes().put("study", study);
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
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		replay(mock);
		
		resource.getRequest().getAttributes().put("study", study);
        resource.getRequest().getAttributes().put("query", new CaseQuery());
        resource.setRepository(mock);
        
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("roles").isJsonArray() );
		assertEquals( 1, data.get("roles").getAsJsonArray().size() );
		
		JsonObject jsonRole = data.get("roles").getAsJsonArray().get(0).getAsJsonObject();
		assertEquals( 1234, jsonRole.get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", jsonRole.get("name").getAsString() );
		
		assertTrue( data.get("counts").isJsonObject() );
		assertTrue( data.get("counts").getAsJsonObject().get("total").isJsonPrimitive() );
		assertEquals( 1, data.get("counts").getAsJsonObject().get("total").getAsInt() );
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceStudyForbiddenTest() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		replay(mock);

		resource.getRequest().getAttributes().put("study", study);
        resource.getRequest().getAttributes().put("query", new CaseQuery());
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
	public void resourcPutTest() throws IOException, RepositoryException {

		Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		expect(study.getId()).andStubReturn(5);
		replay(study);
		
		Capture<Role> capturedArgument = EasyMock.newCapture(CaptureType.FIRST);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setStudyId(5);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		mock.saveStudyRole(eq(study), capture(capturedArgument));
		expectLastCall();
		replay(mock);

        resource.setRepository(mock);

		resource.getRequest().getAttributes().put("study", study);

		Representation readResult = resource.getResource();
		
		// Make the old data set something we can muck about with
		Gson gson = new Gson();
		JsonObject readData = gson.fromJson(readResult.getText(), JsonObject.class);
		
		JsonObject writeData = readData;
		Representation writeRepresentation = new StringRepresentation(writeData.toString(), APPLICATION_JSON);   

		Representation writeResult = resource.putResource(writeRepresentation);
		assertEquals("application/json", writeResult.getMediaType().toString());		
		JsonObject data = gson.fromJson(writeResult.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		Assert.assertEquals("ROLE_CAT_HERDER", capturedArgument.getValue().getName());

	}


	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourcPutTestForbidden() throws IOException, RepositoryException {
		
		Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		expect(study.getId()).andStubReturn(5);
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		
        resource.setRepository(mock);
		resource.getRequest().getAttributes().put("study", study);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		Representation writeRepresentation = new StringRepresentation("[]", APPLICATION_JSON);
		resource.putResource(writeRepresentation);
	}

	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourcPutTestNull() throws IOException, RepositoryException {
		
		Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		expect(study.getId()).andStubReturn(5);
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		
        resource.setRepository(mock);
		resource.getRequest().getAttributes().put("study", study);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		resource.putResource(null);
	}
	
	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourcPutTestDelete() throws IOException, RepositoryException {

		Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getName()).andStubReturn("DEMO");
		expect(study.getId()).andStubReturn(5);
		replay(study);
		
		Capture<Role> capturedArgument = EasyMock.newCapture(CaptureType.FIRST);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		role.setStudyId(5);
		roles.add(role);
		expect(mock.getStudyRoles(eq(study), anyObject(CaseQuery.class))).andStubReturn(roles);
		expect(mock.getStudyRoleCount(eq(study), anyObject(CaseQuery.class))).andStubReturn(new Long(1));
		mock.deleteStudyRole(eq(study), capture(capturedArgument));
		expectLastCall();
		replay(mock);

        resource.setRepository(mock);

		resource.getRequest().getAttributes().put("study", study);

		Representation writeRepresentation = new StringRepresentation("{\"roles\":[]}", APPLICATION_JSON);   
		Representation writeResult = resource.putResource(writeRepresentation);
		assertEquals("application/json", writeResult.getMediaType().toString());		
		JsonObject data = gson.fromJson(writeResult.getText(), JsonObject.class);
		
		Assert.assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		Assert.assertEquals("ROLE_CAT_HERDER", capturedArgument.getValue().getName());
	}
}
