package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class TrackerResourceTest extends AbstractShiroTest {
	
	private TrackerResource studiesResource;
	private StudyRepository repository = new MockStudyRepository();
	
	@Before
	public void initialize() {
        studiesResource = new TrackerResource();
		studiesResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		studiesResource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }
	
	/**
	 * Checks basic resource access to the tracker, while set up as a user who has
	 * admin and read access to both studies. 
	 * @throws IOException
	 */
	@Test
	public void resourceTest() throws IOException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:view")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Representation result = studiesResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonArray studies = data.get("studies").getAsJsonArray();
		assertEquals( 2, studies.size() );
		assertEquals( "DEMO", studies.get(0).getAsJsonObject().get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", studies.get(0).getAsJsonObject().get("description").getAsString());
	}

	/**
	 * Checks basic resource access to the tracker, while set up as a user who has
	 * admin and read access to only one study -- the other study should not be 
	 * accessible. 
	 * @throws IOException
	 */
	@Test
	public void permissionsTest() throws IOException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(true);
        
        expect(subjectUnderTest.isPermitted("OTHER:view")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("OTHER:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Representation result = studiesResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonArray studies = data.get("studies").getAsJsonArray();
		assertEquals( 1, studies.size() );
		assertEquals(false, studies.get(0).getAsJsonObject().get("access").getAsJsonObject().get("adminAllowed").getAsBoolean());
		assertEquals( "DEMO", studies.get(0).getAsJsonObject().get("name").getAsString() );
	}

	/**
	 * Checks unauthorized resource access to the tracker. 
	 * @throws IOException
	 */
	@Test
	public void unauthorizedTest() throws IOException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(false);

        expect(subjectUnderTest.isPermitted("OTHER:view")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("OTHER:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Representation result = studiesResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonArray studies = data.get("studies").getAsJsonArray();
		assertEquals( 0, studies.size() );
	}
	
}
