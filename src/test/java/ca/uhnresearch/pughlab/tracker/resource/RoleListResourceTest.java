package ca.uhnresearch.pughlab.tracker.resource;

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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.*;

public class RoleListResourceTest extends AbstractShiroTest {

	private RoleListResource resource;

	private Gson gson = new Gson();

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
	public void resourceTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		List<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		roles.add(role);
		expect(mock.getRoles(anyObject(CaseQuery.class))).andStubReturn(roles);
		replay(mock);
		resource.setRepository(mock);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertTrue( data.get("roles").isJsonArray() );
		assertEquals( 1, data.get("roles").getAsJsonArray().size() );
		
		JsonObject jsonRole = data.get("roles").getAsJsonArray().get(0).getAsJsonObject();
		assertEquals( 1234, jsonRole.get("id").getAsInt() );
		assertEquals( "ROLE_CAT_HERDER", jsonRole.get("name").getAsString() );
	}
}
