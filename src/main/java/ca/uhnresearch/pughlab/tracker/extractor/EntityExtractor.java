package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class EntityExtractor extends Extractor {

	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		
		Studies study = (Studies) request.getAttributes().get("study");
		Views view = (Views) request.getAttributes().get("view");
		String idValue = (String) request.getAttributes().get("entityId");
		
		Integer caseId = Integer.parseInt(idValue);
		if (caseId == null) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
		JsonNode entityData = repository.getCaseData(study, view, caseId);
		if (entityData == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}

    	logger.info("OK, continuing with the entity: {}", entityData.toString());
		request.getAttributes().put("entity", entityData);
		
		return CONTINUE;

	}
}
