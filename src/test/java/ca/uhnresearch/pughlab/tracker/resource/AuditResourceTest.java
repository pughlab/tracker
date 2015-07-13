package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.apache.shiro.subject.SimplePrincipalCollection;
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

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class AuditResourceTest extends AbstractShiroTest{

	private AuditResource resource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		resource = new AuditResource();
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
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "mock"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
        resource.getRequest().getAttributes().put("study", testStudy);
		
		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 0, data.get("log").getAsJsonArray().size());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * Checks that a non-admin user is denied read access
	 * @throws IOException
	 */
	@Test
	public void resourceTestForbidden() throws ResourceException {

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "mock"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		resource.getRequest().getAttributes().put("study", testStudy);
		resource.getRequest().getAttributes().put("view", testView);
				
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
		return;
	}
}
