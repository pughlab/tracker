package ca.uhnresearch.pughlab.tracker.extractor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;

public class CaseQueryExtractorTest {
	
	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private CaseQueryExtractor extractor;

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new CaseQueryExtractor();
		extractor.setNext(mock);
	}

	@Test
	public void testBasicExtraction() {
		
		Reference reference = new Reference();
		reference.addQueryParameter("offset", "5");
		reference.addQueryParameter("limit", "3");
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		extractor.handle(request, response);
		
		CaseQuery query = (CaseQuery) request.getAttributes().get("query");
		assertNotNull(query);
		
		assertNotNull(query.getLimit());
		assertEquals(3, query.getLimit().intValue());
		
		assertNotNull(query.getOffset());
		assertEquals(5, query.getOffset().intValue());
	}

}
