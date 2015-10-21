package ca.uhnresearch.pughlab.tracker.extractor;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Extractor;

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

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
		
		CasePager pager = new CasePager();
		
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey("offset")) {
			pager.setOffset(new Integer((String) attributes.get("offset")));
		}
		if (attributes.containsKey("limit")) {
			pager.setLimit(new Integer((String) attributes.get("limit")));
		}

		RequestAttributes.setRequestCasePager(request, pager);

		return CONTINUE;
	}

}
