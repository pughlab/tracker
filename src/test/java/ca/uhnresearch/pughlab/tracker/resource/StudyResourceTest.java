package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.assertEquals;

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

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.easymock.EasyMock.*;

public class StudyResourceTest extends AbstractShiroTest {

	private StudyResource studyResource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		studyResource = new StudyResource();
		studyResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		studyResource.setRequest(request);
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
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
		RequestAttributes.setRequestStudy(studyResource.getRequest(), testStudy);
		
		Representation result = studyResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 3, data.get("views").getAsJsonArray().size());
	}
	
	/**
	 * Checks that a non-admin user can access only the views that they are 
	 * authorised to.
	 * @throws IOException
	 */
	@Test
	public void permissionsTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:read:complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:read:track")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read:secondary")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:secondary")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
		RequestAttributes.setRequestStudy(studyResource.getRequest(), testStudy);
		
		Representation result = studyResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 1, data.get("views").getAsJsonArray().size());
		assertEquals( "track", data.getAsJsonArray("views").get(0).getAsJsonObject().get("name").getAsString());
	}

	/**
	 * Checks that write access by a non-admin user implies read access to the same view. 
	 * @throws IOException
	 */
	@Test
	public void permissionsWriteTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:read:track")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:track")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:read:complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read:secondary")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:secondary")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
		RequestAttributes.setRequestStudy(studyResource.getRequest(), testStudy);
		
		Representation result = studyResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 1, data.get("views").getAsJsonArray().size());
		assertEquals( "complete", data.getAsJsonArray("views").get(0).getAsJsonObject().get("name").getAsString());
	}
}
