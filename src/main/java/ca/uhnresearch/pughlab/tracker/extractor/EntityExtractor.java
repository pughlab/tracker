package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class EntityExtractor extends RepositoryAwareExtractor {

	private final Logger logger = LoggerFactory.getLogger(EntityExtractor.class);

	protected int beforeHandle(Request request, Response response) {
		
		StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(request);
		
		String idValue = (String) request.getAttributes().get("entityId");
		if (logger.isDebugEnabled()) {
			logger.debug("Continuing with the entity: {}", idValue);
		}
		
		Integer caseId = null;
		try {
			caseId = Integer.parseInt(idValue);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}
		
		query = getRepository().addStudyCaseSelector(query, caseId);
		RequestAttributes.setRequestCaseQuery(request, query);
		
		return CONTINUE;
	}
}
