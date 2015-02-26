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

public class StudyExtractor extends Extractor {
	
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(StudyExtractor.class);

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		extractFromQuery("studyName", "studyName", true);
		
		String value = (String) request.getAttributes().get("studyName");
		logger.info("Called StudyExtractor beforeHandle: {}", value);
		
		// Now we can extract the study and write it as a new attribute
		Studies s = repository.getStudy(value);
		
		// If we don't find a value, we can fail at this stage.
		if (s == null) {
			logger.info("Can't find study: {}", value);

			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return STOP;
		}
		
		// Permissions checking might also be a sensible idea
		
		request.getAttributes().put("study", s);
		
		return CONTINUE;
	}
}
