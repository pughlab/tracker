package ca.uhnresearch.pughlab.tracker.extractor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
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
		
		String value = (String) request.getAttributes().get("studyName");
		
		// Now we can extract the study and write it as a new attribute
		Studies s = repository.getStudy(value);
		
		// If we don't find a value, we can fail at this stage.
		if (s == null) {
			logger.info("Can't find study: {}", value);

			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return STOP;
		}
		
		// Permissions checking might also be a sensible idea
    	Subject currentUser = SecurityUtils.getSubject();
    	logger.info("Authenticated as: {}", currentUser.getPrincipal().toString());

    	// Explicitly block access when there's not at least study read access
    	String permission = "study:read:" + s.getName();
    	logger.info("Checking permission for: {}", permission);
    	if (! currentUser.isPermitted(permission)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	logger.info("OK, continuing with the study: {}", s.getName());
		request.getAttributes().put("study", s);
		
		return CONTINUE;
	}
}
