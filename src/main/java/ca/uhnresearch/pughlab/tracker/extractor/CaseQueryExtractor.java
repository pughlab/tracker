package ca.uhnresearch.pughlab.tracker.extractor;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;

/**
 * Extracts all the parameters for a limit and an offset, and any other case query 
 * values, and drops them into a new CaseQuery attribute that is added to the list
 * of attributes. 
 */
public class CaseQueryExtractor extends Extractor {
	
	private final Logger logger = LoggerFactory.getLogger(CaseQueryExtractor.class);

	protected int beforeHandle(Request request, Response response) {
		extractFromQuery("offset", "offset", true);
		extractFromQuery("limit", "limit", true);
		
		CaseQuery query = new CaseQuery();
		
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey("offset")) {
			query.offset = new Integer((String) attributes.get("offset"));
		}
		if (attributes.containsKey("limit")) {
			query.limit = new Integer((String) attributes.get("limit"));
		}

		attributes.put("query", query);

		return CONTINUE;
	}

}
