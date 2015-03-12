package ca.uhnresearch.pughlab.tracker.resource;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.easymock.EasyMock.*;

public class StudyResourceTest extends AbstractShiroTest {

	private StudyResource studyResource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
		studyResource = new StudyResource();
		studyResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		studyResource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }
	
	@Test
	public void resourceTest() throws IOException {
		
		Studies testStudy = repository.getStudy("DEMO");
		studyResource.getRequest().getAttributes().put("study", testStudy);
		
		Representation result = studyResource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		JsonObject study = data.get("study").getAsJsonObject();
		
		assertEquals( "DEMO", study.get("name").getAsString() );
		assertEquals( "A demo clinical genomics study", study.get("description").getAsString() );

		assertEquals( 3, data.get("views").getAsJsonArray().size());
	}
}
