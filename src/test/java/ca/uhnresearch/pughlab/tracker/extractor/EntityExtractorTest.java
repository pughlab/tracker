package ca.uhnresearch.pughlab.tracker.extractor;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

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

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class EntityExtractorTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private EntityExtractor extractor;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new EntityExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}

	@Test
	public void testBasicExtraction() {
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		Studies study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		Views view = repository.getStudyView(study, "complete");
		request.getAttributes().put("view", view);
		request.getAttributes().put("entityId", "5");

		extractor.handle(request, response);
		Cases caseValue = (Cases) request.getAttributes().get("entity");
		assertNotNull(caseValue);
		assertEquals(5, caseValue.getId().intValue());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testMissingExtraction() {
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		Studies study = repository.getStudy("DEMO");
		request.getAttributes().put("study", study);
		Views view = repository.getStudyView(study, "complete");
		request.getAttributes().put("view", view);
		request.getAttributes().put("entityId", "12");

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Not Found"));

		extractor.handle(request, response);
	}
}