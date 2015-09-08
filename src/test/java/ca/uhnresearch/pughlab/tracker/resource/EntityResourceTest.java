package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.*;
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
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class EntityResourceTest extends AbstractShiroTest {

	private Gson gson = new Gson();
	private EntityResource resource;
	private StudyRepository repository = new MockStudyRepository();


	@Before
	public void initialize() {
		
		resource = new EntityResource();
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
		resource.getRequest().getAttributes().put("study", testStudy);
		resource.getRequest().getAttributes().put("view", testView);
		resource.getRequest().getAttributes().put("entity", testCase);

		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );		
		
		assertTrue( data.get("entity").isJsonObject() );
		
		assertEquals( "DEMO-03", data.get("entity").getAsJsonObject().get("patientId").getAsString() );
	}

}
