package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class EntityFieldResourceTest extends AbstractShiroTest {

	private EntityFieldResource resource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		resource = new EntityFieldResource();
		resource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		resource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
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
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);

		Representation result = resource.getResource();
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
	 * Tests reading from a entity field. 
	 * @throws IOException
	 */
	@Test
	public void resourceTestForbidden() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:patientId")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
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
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("OTHER:read")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:patientId")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		// This time, we need an entity value to put
		String s = "{\"value\":\"DEMO-XX\"}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);
		
		Representation result = resource.putResource(ir);
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("value").isJsonPrimitive() );
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
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:patientId")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		// This time, we need an entity value to put
		String s = "{\"$notAvailable\":true}";
		Reader r=new StringReader(s);
		InputStream is=new ReaderInputStream(r);
		InputRepresentation ir =new InputRepresentation(is);
		ir.setCharacterSet(CharacterSet.ISO_8859_1);
		
		Representation result = resource.putResource(ir);
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("value").isJsonPrimitive() );
	}

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
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.putResource(new StringRepresentation("", APPLICATION_JSON));
	}

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 */
	@Test
	public void resourcePutTestForbiddenField() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.putResource(new StringRepresentation("", APPLICATION_JSON));
	}

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 */
	@Test
	public void resourcePutTestMissing() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Server Error"));

		resource.putResource(new StringRepresentation("", APPLICATION_JSON));
	}

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 * @throws RepositoryException 
	 */
	@Test
	public void resourcePutTestInvalidValueException() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		StudyRepository repository = createMock(StudyRepository.class);
		repository.setCaseAttributeValue(eq(testStudy), eq(testView), eq(testCase), eq(testAttribute), eq("stuart"), anyObject(JsonNode.class));
		expectLastCall().andThrow(new InvalidValueException("Invalid value"));
		replay(repository);
		
		resource.setRepository(repository);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		resource.putResource(new StringRepresentation("{\"value\":\"TST-001\"}", APPLICATION_JSON));
	}

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 * @throws RepositoryException 
	 */
	@Test
	public void resourcePutTestNotFoundException() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		StudyRepository repository = createMock(StudyRepository.class);
		repository.setCaseAttributeValue(eq(testStudy), eq(testView), eq(testCase), eq(testAttribute), eq("stuart"), anyObject(JsonNode.class));
		expectLastCall().andThrow(new NotFoundException("Not Found"));
		replay(repository);
		
		resource.setRepository(repository);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Not Found"));

		resource.putResource(new StringRepresentation("{\"value\":\"TST-001\"}", APPLICATION_JSON));
	}

	/**
	 * Tests that writing to a entity field with only read access permitted fails
	 * appropriately. 
	 * @throws IOException
	 * @throws RepositoryException 
	 */
	@Test
	public void resourcePutTestRepositoryException() throws IOException, RepositoryException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:write:patientId")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		Cases testCase = repository.getStudyCase(testStudy, testView, 3);
		Attributes testAttribute = repository.getStudyAttribute(testStudy, "patientId");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		RequestAttributes.setRequestEntity(resource.getRequest(), testCase);
		RequestAttributes.setRequestAttribute(resource.getRequest(), testAttribute);
		
		RepositoryException error = createMock(RepositoryException.class);
		replay(error);
		StudyRepository repository = createMock(StudyRepository.class);
		repository.setCaseAttributeValue(eq(testStudy), eq(testView), eq(testCase), eq(testAttribute), eq("stuart"), anyObject(JsonNode.class));
		expectLastCall().andThrow(error);
		replay(repository);
		
		resource.setRepository(repository);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Internal Server Error"));

		resource.putResource(new StringRepresentation("{\"value\":\"TST-001\"}", APPLICATION_JSON));
	}
}
