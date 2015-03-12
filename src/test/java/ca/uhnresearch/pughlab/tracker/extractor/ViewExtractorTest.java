package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;

import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
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
		Studies study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		request.getAttributes().put("viewName", "complete");
		extractor.handle(request, response);
		
		Views view = (Views) request.getAttributes().get("view");
		assertNotNull(view);
	}
}
