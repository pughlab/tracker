package ca.uhnresearch.pughlab.tracker.extractor;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Extractor;

import ca.uhnresearch.pughlab.tracker.dao.CasePager;

/**
 * Extracts all the parameters for a limit and an offset, and any other case query 
 * values, and drops them into a new CasePager attribute that is added to the list
 * of attributes. 
 */
public class CasePagerExtractor extends Extractor {
	
	protected int beforeHandle(Request request, Response response) {
		
		extractFromQuery("offset", "offset", true);
		extractFromQuery("limit", "limit", true);
		super.beforeHandle(request, response);
		
		CasePager query = new CasePager();
		
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey("offset")) {
			query.setOffset(new Integer((String) attributes.get("offset")));
		}
		if (attributes.containsKey("limit")) {
			query.setLimit(new Integer((String) attributes.get("limit")));
		}

		attributes.put("query", query);

		return CONTINUE;
	}

}
