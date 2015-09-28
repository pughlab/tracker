package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.restlet.data.Status;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class AttributeExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private AttributeExtractor extractor;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new AttributeExtractor();
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
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		Study study = repository.getStudy("DEMO");
		View view = repository.getStudyView(study, "complete");
		RequestAttributes.setRequestStudy(request, study);
		RequestAttributes.setRequestView(request, view);
		request.getAttributes().put("attributeName", "patientId");
		extractor.handle(request, response);
		
		Attributes attribute = RequestAttributes.getRequestAttribute(request);
		assertNotNull(attribute);
		
		assertEquals("patientId", attribute.getName());
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

}
