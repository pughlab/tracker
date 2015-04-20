package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;

import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ViewResourceTest extends AbstractShiroTest {

	private ViewDataResource viewResource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		viewResource = new ViewDataResource();
		viewResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		viewResource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }

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
		viewResource.getRequest().getAttributes().put("study", testStudy);
		viewResource.getRequest().getAttributes().put("view", testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
		viewResource.getRequest().getAttributes().put("query", query);

		Representation result = viewResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );
		
		assertTrue( data.get("records").isJsonArray() );
	}
}
