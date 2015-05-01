package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class EntityExtractor extends Extractor {

	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		
		Study study = (Study) request.getAttributes().get("study");
		View view = (View) request.getAttributes().get("view");
		String idValue = (String) request.getAttributes().get("entityId");
		
		Integer caseId = null;
		try {
			caseId = Integer.parseInt(idValue);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
		Cases caseValue = repository.getStudyCase(study, view, caseId);
		if (caseValue == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}

    	logger.info("OK, continuing with the entity: {}", caseValue.toString());
		request.getAttributes().put("entity", caseValue);
		
		return CONTINUE;

	}
}
