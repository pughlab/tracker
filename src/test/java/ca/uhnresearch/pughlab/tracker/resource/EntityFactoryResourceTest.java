package ca.uhnresearch.pughlab.tracker.resource;

import static org.easymock.EasyMock.*;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;

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

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityFactoryResourceTest extends AbstractShiroTest {

	private EntityFactoryResource resource;
	private StudyRepository repository = new MockStudyRepository();
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;


	@Before
	public void initialize() {
		
		resource = new EntityFactoryResource();
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
	 * Checks that an empty entity POST request fails
	 * @throws IOException
	 */
	@Test
	public void resourceTestWriteBadRequest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("study:write:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		resource.getRequest().getAttributes().put("study", testStudy);
		resource.getRequest().getAttributes().put("view", testView);
				
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		Representation writeRepresentation = new StringRepresentation("", APPLICATION_JSON);   
		resource.postResource(writeRepresentation);
		return;
	}

	/**
	 * Checks that an empty entity POST request fails
	 * @throws IOException
	 */
	@Test
	public void resourceTestWriteForbidden() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("study:write:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		resource.getRequest().getAttributes().put("study", testStudy);
		resource.getRequest().getAttributes().put("view", testView);
				
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		Representation writeRepresentation = new StringRepresentation("", APPLICATION_JSON);   
		resource.postResource(writeRepresentation);
		return;
	}

	/**
	 * Checks that an empty entity POST request fails
	 * @throws IOException
	 */
	@Test
	public void resourceTestWrite() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("study:write:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		resource.getRequest().getAttributes().put("study", testStudy);
		resource.getRequest().getAttributes().put("view", testView);
				
		ObjectNode body = jsonNodeFactory.objectNode();
		ObjectNode entity = jsonNodeFactory.objectNode();
		entity.put("patientId", "DEMO-XX");
		body.replace("entity", entity);
		
		String entityBody = body.toString();

		Representation writeRepresentation = new StringRepresentation(entityBody, APPLICATION_JSON);   
		resource.postResource(writeRepresentation);
		return;
	}
}
