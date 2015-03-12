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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class TrackerResourceTest extends AbstractShiroTest {
	
	private TrackerResource studiesResource;
	private StudyRepository repository = new MockStudyRepository();
	
	@Before
	public void initialize() {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("study:read:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("study:read:OTHER")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        studiesResource = new TrackerResource();
		studiesResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		studiesResource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }
	
	@Test
	public void resourceTest() throws IOException {
		System.out.println(studiesResource);
		Representation result = studiesResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonArray studies = data.get("studies").getAsJsonArray();
		assertEquals( 2, studies.size() );
		assertEquals( "DEMO", studies.get(0).getAsJsonObject().get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", studies.get(0).getAsJsonObject().get("description").getAsString());
	}
}
