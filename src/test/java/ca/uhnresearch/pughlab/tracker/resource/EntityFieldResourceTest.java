package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;

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
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
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
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("study:read:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("study:read:OTHER")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Studies testStudy = repository.getStudy("DEMO");		
		Views testView = repository.getStudyView(testStudy, "complete");
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
}
