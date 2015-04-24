package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

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
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class ViewExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private ViewExtractor extractor;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new ViewExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}

	/**
	 * Tests an extractor can pull out a given authorized view from a study and 
	 * a view name.
	 */
	@Test
	public void testBasicExtraction() {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		Study study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		request.getAttributes().put("viewName", "complete");
		extractor.handle(request, response);
		
		View view = (View) request.getAttributes().get("view");
		assertNotNull(view);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * Tests an extractor refuses a given unauthorized view from a study and 
	 * a view name.
	 */
	@Test
	public void testBasicRefusal() throws ResourceException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("view:read:DEMO-complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("view:write:DEMO-complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("view:download:DEMO-complete")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		Study study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		request.getAttributes().put("viewName", "complete");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));
		
		extractor.handle(request, response);
	}

	/**
	 * Tests an extractor refuses a given unauthorized view from a study and 
	 * a view name.
	 */
	@Test
	public void testMissing() throws ResourceException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("view:read:DEMO-complete")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		Study study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		request.getAttributes().put("viewName", "missing");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Not Found"));
		
		extractor.handle(request, response);
	}

	/**
	 * Tests an extractor refuses a given unauthorized view from a study and 
	 * a view name.
	 */
	@Test
	public void testNonAdminAcceptance() throws ResourceException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("study:admin:DEMO")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("view:read:DEMO-complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("view:write:DEMO-complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("view:download:DEMO-complete")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		Study study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		request.getAttributes().put("viewName", "complete");
		
		extractor.handle(request, response);
		
		View view = (View) request.getAttributes().get("view");
		assertNotNull(view);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());

	}

}
