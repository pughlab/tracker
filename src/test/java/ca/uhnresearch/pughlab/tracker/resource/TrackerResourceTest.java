package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.io.ReaderInputStream;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
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
		studiesResource.setResponse(new Response(request));
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
        
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);

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
        
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);

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
		assertEquals(false, studies.get(0).getAsJsonObject().get("access").getAsJsonObject().get("admin").getAsBoolean());
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
        
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);

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
	
	/**
	 * Check that a new study can be created.
	 */
	@Test
	public void createTest() throws IOException {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));

        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);

        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        String s = "{\"name\":\"TEST\",\"description\":\"A test study\"}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);

		studiesResource.postResource(ir);
		
		assertEquals( Status.REDIRECTION_SEE_OTHER, studiesResource.getResponse().getStatus());
		assertEquals( "http://localhost:9998/services/api/studies/TEST", studiesResource.getResponse().getLocationRef().toString());		
	}

	/**
	 * Check that a new study can't be created without admin rights
	 */
	@Test
	public void createTestForbidden() throws IOException {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));

        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(false);

        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        String s = "{\"name\":\"TEST\",\"description\":\"A test study\"}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		studiesResource.postResource(ir);
	}
	
	/**
	 * Checks admin resource access to the tracker, to make sure we get a create action
	 * URL. 
	 * @throws IOException
	 */
	@Test
	public void adminTest() throws IOException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("OTHER:view")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Representation result = studiesResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonArray studies = data.get("studies").getAsJsonArray();
		assertEquals( 1, studies.size() );
		assertEquals( "DEMO", studies.get(0).getAsJsonObject().get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", studies.get(0).getAsJsonObject().get("description").getAsString());
		
		JsonObject actions = data.getAsJsonObject("actions");
		assertNotNull(actions.get("create"));
		assertEquals("http://localhost:9998/services/api/studies", actions.get("create").getAsString());
	}

	/**
	 * Check that a new study can be created.
	 * @throws RepositoryException 
	 */
	@Test
	public void createTestException() throws IOException, RepositoryException {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);

        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        StudyRepository mock = createMock(StudyRepository.class);
        mock.saveStudy(anyObject(Study.class), anyObject(String.class));
        expectLastCall().andStubThrow(new InvalidValueException("Error"));
        replay(mock);
        
        studiesResource.setRepository(mock);

        String s = "{\"name\":\"TEST\",\"description\":\"A test study\"}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		studiesResource.postResource(ir);
	}

	/**
	 * Check that a new study can be created.
	 * @throws RepositoryException 
	 */
	@Test
	public void createTestIOException() throws IOException, RepositoryException {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("admin")).andStubReturn(true);

        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        StudyRepository mock = createMock(StudyRepository.class);
        mock.saveStudy(anyObject(Study.class), anyObject(String.class));
        expectLastCall().andStubThrow(new InvalidValueException("Error"));
        replay(mock);
        
        studiesResource.setRepository(mock);

		FileRepresentation ir = new FileRepresentation("/dev/null", MediaType.APPLICATION_JSON);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		studiesResource.postResource(ir);
	}
}
