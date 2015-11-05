package ca.uhnresearch.pughlab.tracker.extractor;

import java.io.IOException;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FilterExtractor extends RepositoryAwareExtractor {
	
	private final Logger logger = LoggerFactory.getLogger(AttributeExtractor.class);

	private static ObjectMapper mapper = new ObjectMapper();

	protected int beforeHandle(Request request, Response response) {
		
		extractFromQuery("q", "q", true);
		super.beforeHandle(request, response);

		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey("q")) {
			String query = (String) attributes.get("q");
			try {
				handleQuery(request, query);
			} catch (JsonParseException e) {
				logger.error("Invalid query JSON: " + query);
			} catch (JsonMappingException e) {
				logger.error("Can't map JSON: " + query);
			} catch (IOException e) {
				logger.error("IOException when handling a query: " + e.getLocalizedMessage());
			}
		}
		
		return CONTINUE;

	}
	
	private void handleQuery(Request request, String query) throws JsonParseException, JsonMappingException, IOException  {
		ObjectNode queryObject = mapper.readValue(query, ObjectNode.class);
		
		// Now we ought to be able to add filters into the query that we generate
		// through the repository. Don't do this if we don't have a real filter
		// value that makes sense. 
		
		if (queryObject != null) { 
			StudyCaseQuery caseQuery = RequestAttributes.getRequestCaseQuery(request);
			StudyCaseQuery filteredCaseQuery = getRepository().addStudyCaseFilterSelector(caseQuery, queryObject);
			RequestAttributes.setRequestCaseQuery(request, filteredCaseQuery);
		}
		
		RequestAttributes.setRequestFilter(request, queryObject);
	}
}
