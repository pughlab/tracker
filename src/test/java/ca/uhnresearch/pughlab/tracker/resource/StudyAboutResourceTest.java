package ca.uhnresearch.pughlab.tracker.resource;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyAboutResponse;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.easymock.EasyMock.*;

public class StudyAboutResourceTest extends AbstractShiroTest {

	private StudyAboutResource studyAboutResource;
	private StudyRepository repository = new MockStudyRepository();

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		studyAboutResource = new StudyAboutResource();
		studyAboutResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		studyAboutResource.setRequest(request);
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
        
        StudyAboutResponse testStudy = new StudyAboutResponse();
        testStudy.setName("DEMO");
        testStudy.setDescription("A demo clinical genomics study");
		RequestAttributes.setRequestStudyAbout(studyAboutResource.getRequest(), testStudy);
		
		Representation result = studyAboutResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		assertEquals( "DEMO", data.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", data.get("description").getAsString() );
	}
	
	/**
	 * Checks that a non-admin user can access only the views that they are 
	 * authorised to.
	 * @throws IOException
	 */
	@Ignore
	public void permissionsTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        StudyAboutResponse test = new StudyAboutResponse();
		RequestAttributes.setRequestStudyAbout(studyAboutResource.getRequest(), test);
        
		Representation result = studyAboutResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		assertEquals( "DEMO", data.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", data.get("description").getAsString() );
	}

	/**
	 * Checks that a public user can access only the views that they are 
	 * authorised to.
	 * @throws IOException
	 */
	@Ignore
	public void publicAccessTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
        ObjectNode options = jsonNodeFactory.objectNode();
        options.set("public", jsonNodeFactory.booleanNode(true));
        testStudy.setOptions(options);
		RequestAttributes.setRequestStudy(studyAboutResource.getRequest(), testStudy);
		
		Representation result = studyAboutResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );
	}

	/**
	 * Checks that a public user can access only the views that they are 
	 * authorised to.
	 * @throws IOException
	 */
	@Ignore
	public void publicAccessDeniedTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
        ObjectNode options = jsonNodeFactory.objectNode();
        options.set("public", jsonNodeFactory.booleanNode(false));
        testStudy.setOptions(options);
		RequestAttributes.setRequestStudy(studyAboutResource.getRequest(), testStudy);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));
		
		studyAboutResource.getResource();
	}
}
