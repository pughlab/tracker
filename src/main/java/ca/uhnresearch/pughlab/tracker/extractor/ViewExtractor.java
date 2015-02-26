package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewExtractor extends Extractor {
	
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(ViewExtractor.class);

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		extractFromQuery("viewName", "viewName", true);
		
		Studies study = (Studies) request.getAttributes().get("study");
		String value = (String) request.getAttributes().get("viewName");
		logger.info("Called ViewExtractor beforeHandle: {}", value);
		
		// Now we can extract the study and write it as a new attribute
		Views v = repository.getStudyView(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (v == null) {
			logger.info("Can't find study: {}", value);

			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return STOP;
		}
		
		// Permissions checking might also be a sensible idea
		
		request.getAttributes().put("view", v);
		
		return CONTINUE;
	}

}
