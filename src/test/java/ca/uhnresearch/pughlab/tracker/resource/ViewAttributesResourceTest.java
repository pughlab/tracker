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

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ViewAttributesResourceTest extends AbstractShiroTest {

	private ViewAttributesResource viewResource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		viewResource = new ViewAttributesResource();
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
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted(anyObject(String.class))).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(viewResource.getRequest(), testStudy);
		RequestAttributes.setRequestView(viewResource.getRequest(), testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
        RequestAttributes.setRequestCaseQuery(viewResource.getRequest(), query);

		Representation result = viewResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );
	}
}
