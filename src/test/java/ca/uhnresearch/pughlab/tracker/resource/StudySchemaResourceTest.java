package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.matchers.JUnitMatchers.containsString;

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
import org.restlet.resource.ResourceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class StudySchemaResourceTest extends AbstractShiroTest{

	private StudySchemaResource resource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		resource = new StudySchemaResource();
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

	/**
	 * Checks that an admin user can access the entire study, including all its 
	 * many views.
	 * @throws IOException
	 */
	@Test
	public void resourceTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Studies testStudy = repository.getStudy("DEMO");
        resource.getRequest().getAttributes().put("study", testStudy);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 3, data.get("views").getAsJsonArray().size());

		assertEquals( 5, data.get("attributes").getAsJsonArray().size());
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Checks that a non-admin user is denied read access
	 */
	@Test
	public void resourceTestForbidden() throws IOException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Studies testStudy = repository.getStudy("DEMO");
        resource.getRequest().getAttributes().put("study", testStudy);
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
		return;
	}
}
