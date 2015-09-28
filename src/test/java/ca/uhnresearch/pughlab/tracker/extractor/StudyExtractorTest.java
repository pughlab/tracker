package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class StudyExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private StudyExtractor extractor;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new StudyExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}

	@Test
	public void testBasicExtraction() {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		request.getAttributes().put("studyName", "DEMO");
		extractor.handle(request, response);
		
		Study study = RequestAttributes.getRequestStudy(request);
		assertNotNull(study);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testPermissions() throws ResourceException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("DEMO:view")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		request.getAttributes().put("studyName", "DEMO");

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));
		
		extractor.handle(request, response);
	}

	@Test
	public void testMissing() throws ResourceException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		request.getAttributes().put("studyName", "XXXX");

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Not Found"));

		extractor.handle(request, response);
	}
}
