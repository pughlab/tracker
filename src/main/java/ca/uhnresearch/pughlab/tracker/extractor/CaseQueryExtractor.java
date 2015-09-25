package ca.uhnresearch.pughlab.tracker.extractor;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Extractor;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

/**
 * Extracts all the parameters for a limit and an offset, and any other case query 
 * values, and drops them into a new CaseQuery attribute that is added to the list
 * of attributes. 
 */
public class CaseQueryExtractor extends Extractor {
	
	protected int beforeHandle(Request request, Response response) {
		
		extractFromQuery("offset", "offset", true);
		extractFromQuery("limit", "limit", true);
		extractFromQuery("q", "q", true);
		super.beforeHandle(request, response);
		
		CaseQuery query = new CaseQuery();
		
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey("q")) {
			query.setPattern((String) attributes.get("q"));
		}
		if (attributes.containsKey("offset")) {
			query.setOffset(new Integer((String) attributes.get("offset")));
		}
		if (attributes.containsKey("limit")) {
			query.setLimit(new Integer((String) attributes.get("limit")));
		}

		RequestAttributes.setRequestCaseQuery(request, query);

		return CONTINUE;
	}

}
