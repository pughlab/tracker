package ca.uhnresearch.pughlab.tracker.extractor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
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
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class StudyExtractor extends Extractor {
	
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

	protected int beforeHandle(Request request, Response response) {
		
		String value = (String) request.getAttributes().get("studyName");
		
		// Now we can extract the study and write it as a new attribute
		Study s = repository.getStudy(value);
		
		// If we don't find a value, we can fail at this stage.
		if (s == null) {
			logger.warn("Can't find study: {}", value);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		// Permissions checking might also be a sensible idea
    	Subject currentUser = SecurityUtils.getSubject();
		PrincipalCollection principals = currentUser.getPrincipals();
		String user = principals.getPrimaryPrincipal().toString();
    	logger.debug("Authenticated as: {}", user);

    	// Explicitly block access when there's not at least study read access
    	String permission = s.getName() + ":view";
    	logger.debug("Checking permission for: {}", permission);
    	
    	if (! currentUser.isPermitted(permission)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	logger.debug("OK, continuing with the study: {}", s.getName());
    	RequestAttributes.setRequestStudy(request, s);
		
		return CONTINUE;
	}
}
