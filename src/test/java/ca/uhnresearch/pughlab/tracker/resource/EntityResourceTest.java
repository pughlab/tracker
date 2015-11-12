package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.easymock.EasyMock.*;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class EntityResourceTest extends AbstractShiroTest {

	private Gson gson = new Gson();
	private EntityResource resource;
	private StudyRepository repository;


	@Before
	public void initialize() {
		
		resource = new EntityResource();
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		resource.setRequest(request);
		
		repository = new MockStudyRepository();
		resource.setRepository(repository);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);

		List<Integer> cases = new ArrayList<Integer>();
		cases.add(3);
		
		RequestAttributes.setRequestCaseQuery(resource.getRequest(), new MockStudyCaseQuery(cases));

		Representation result = resource.getResource();
		Assert.assertEquals("application/json", result.getMediaType().toString());
		
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		Assert.assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		Assert.assertEquals( "complete", view.get("name").getAsString() );		
		
		Assert.assertTrue( data.get("entity").isJsonObject() );
		
		Assert.assertEquals( "DEMO-03", data.get("entity").getAsJsonObject().get("patientId").getAsString() );
	}

	@Test
	public void resourceDeleteTestForbidden() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:delete")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);

		List<Integer> cases = new ArrayList<Integer>();
		cases.add(3);
		
		RequestAttributes.setRequestCaseQuery(resource.getRequest(), new MockStudyCaseQuery(cases));

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.deleteResource();
	}

	@Test
	public void resourceDeleteTestSuccess() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:delete")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);

		List<Integer> cases = new ArrayList<Integer>();
		cases.add(3);
		
		RequestAttributes.setRequestCaseQuery(resource.getRequest(), new MockStudyCaseQuery(cases));

		Assert.assertEquals(10, repository.getRecordCount(testStudy, testView).longValue());

		resource.deleteResource();
		
		// And we should check it's gone, assuming the mocked repo mocks that
		Assert.assertEquals(9, repository.getRecordCount(testStudy, testView).longValue());
		
	}
}
