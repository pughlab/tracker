package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;

public class EntityExtractor extends Extractor {

	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		
		StudyCaseQuery query = (StudyCaseQuery) request.getAttributes().get("query");
		
		String idValue = (String) request.getAttributes().get("entityId");
		if (logger.isDebugEnabled()) {
			logger.debug("Continuing with the entity: {}", idValue);
		}
		
		Integer caseId = null;
		try {
			caseId = Integer.parseInt(idValue);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
		query = repository.addStudyCaseSelector(query, caseId);
		request.getAttributes().put("query", query);
		
		return CONTINUE;

	}
}
