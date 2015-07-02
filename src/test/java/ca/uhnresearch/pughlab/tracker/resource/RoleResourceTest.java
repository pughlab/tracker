package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RoleResourceTest extends AbstractShiroTest {

	private RoleResource resource;

	private Gson gson = new Gson();

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
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		resource.getRequest().getAttributes().put("role", role);

		List<String> users = new ArrayList<String>();
		users.add("user1");
		users.add("user2");
		List<String> permissions = new ArrayList<String>();
		permissions.add("study:*:*");
		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		expect(mock.getRoleUsers(role)).andStubReturn(users);
		expect(mock.getRolePermissions(role)).andStubReturn(permissions);
		replay(mock);
		resource.setRepository(mock);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("role").isJsonObject() );
		assertEquals( 1234, data.get("role").getAsJsonObject().get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", data.get("role").getAsJsonObject().get("name").getAsString() );
		
		assertTrue( data.get("users").isJsonArray() );
		assertEquals( 2, data.get("users").getAsJsonArray().size() );
		assertEquals( "user1", data.get("users").getAsJsonArray().get(0).getAsString() );
		assertEquals( "user2", data.get("users").getAsJsonArray().get(1).getAsString() );
		
		assertTrue( data.get("permissions").isJsonArray() );
		assertEquals( 1, data.get("permissions").getAsJsonArray().size() );
		assertEquals( "study:*:*", data.get("permissions").getAsJsonArray().get(0).getAsString() );
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
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		resource.getRequest().getAttributes().put("role", role);

		Role renamed = new Role();
		renamed.setName("X");
		renamed.setId(1234);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		mock.saveRole(anyObject(Role.class));
		expectLastCall();
		expect(mock.getRole("X")).andStubReturn(renamed);
		mock.setRoleUsers(anyObject(Role.class), anyObject(ArrayList.class));
		expectLastCall();
		mock.setRolePermissions(anyObject(Role.class), anyObject(ArrayList.class));
		expectLastCall();
		expect(mock.getRoleUsers(renamed)).andStubReturn(new ArrayList<String>());
		expect(mock.getRolePermissions(renamed)).andStubReturn(new ArrayList<String>());
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
