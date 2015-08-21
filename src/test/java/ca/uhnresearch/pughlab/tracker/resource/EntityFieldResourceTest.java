package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.restlet.data.MediaType.APPLICATION_JSON;

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
import org.restlet.data.CharacterSet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.io.ReaderInputStream;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class EntityFieldResourceTest extends AbstractShiroTest {

	private EntityFieldResource entityFieldResource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		entityFieldResource = new EntityFieldResource();
		entityFieldResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		entityFieldResource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }

	/**
	 * Tests reading from a entity field. 
	 * @throws IOException
	 */
	@Test
	public void resourceTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		entityFieldResource.getRequest().getAttributes().put("study", testStudy);
		entityFieldResource.getRequest().getAttributes().put("view", testView);
		entityFieldResource.getRequest().getAttributes().put("entity", testCase);
		entityFieldResource.getRequest().getAttributes().put("entityField", "patientId");

		Representation result = entityFieldResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("value").isJsonPrimitive() );
		
		assertEquals( "DEMO-03", data.get("value").getAsString() );
	}

	/**
	 * Tests writing to a entity field. 
	 * @throws IOException
	 */
	@Test
	public void resourcePutTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:write")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		entityFieldResource.getRequest().getAttributes().put("study", testStudy);
		entityFieldResource.getRequest().getAttributes().put("view", testView);
		entityFieldResource.getRequest().getAttributes().put("entity", testCase);
		entityFieldResource.getRequest().getAttributes().put("entityField", "patientId");
		
		// This time, we need an entity value to put
		String s = "{\"value\":\"DEMO-XX\"}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);
		
		Representation result = entityFieldResource.putResource(ir);
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("value").isJsonPrimitive() );
		
		// We're currently using a mocked repo here, so we can't really assume we get back
		// the right value.
		//assertEquals( "DEMO-XX", data.get("value").getAsString() );
	}

	/**
	 * Tests writing to a entity field. 
	 * @throws IOException
	 */
	@Test
	public void resourcePutTestNotAvailable() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:write")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		entityFieldResource.getRequest().getAttributes().put("study", testStudy);
		entityFieldResource.getRequest().getAttributes().put("view", testView);
		entityFieldResource.getRequest().getAttributes().put("entity", testCase);
		entityFieldResource.getRequest().getAttributes().put("entityField", "patientId");
		
		// This time, we need an entity value to put
		String s = "{\"$notAvailable\":true}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);
		
		Representation result = entityFieldResource.putResource(ir);
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("value").isJsonPrimitive() );
		
		// We're currently using a mocked repo here, so we can't really assume we get back
		// the right value.
		//assertEquals( "DEMO-XX", data.get("value").getAsString() );
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 */
	@Test
	public void resourcePutTestForbidden() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:write")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		entityFieldResource.getRequest().getAttributes().put("study", testStudy);
		entityFieldResource.getRequest().getAttributes().put("view", testView);
		entityFieldResource.getRequest().getAttributes().put("entity", testCase);
		entityFieldResource.getRequest().getAttributes().put("entityField", "patientId");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		entityFieldResource.putResource(new StringRepresentation("", APPLICATION_JSON));
		return;
	}
}
