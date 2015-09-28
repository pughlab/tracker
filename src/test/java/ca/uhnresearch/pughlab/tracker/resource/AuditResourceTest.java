package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class AuditResourceTest extends AbstractShiroTest{

	private AuditResource resource;
	private StudyRepository repository = new MockStudyRepository();
	private AuditLogRepository auditLogRepository;

	@Before
	public void initialize() {
		
		auditLogRepository = createMock(AuditLogRepository.class);
		
		resource = new AuditResource();
		resource.setRepository(auditLogRepository);
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
		
		List<JsonNode> auditRecords = new ArrayList<JsonNode>();
		
		expect(auditLogRepository.getAuditData(anyObject(Study.class), anyObject(CaseQuery.class))).andReturn(auditRecords);
		replay(auditLogRepository);
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		
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
		
		replay(auditLogRepository);

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
				
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		resource.getResource();
		return;
	}
}
