package ca.uhnresearch.pughlab.tracker.extractor;

import java.text.MessageFormat;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyAboutResponse;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class StudyAboutExtractor extends RepositoryAwareExtractor {

	private final Logger logger = LoggerFactory.getLogger(StudyExtractor.class);

	protected int beforeHandle(Request request, Response response) {
		
		String value = (String) request.getAttributes().get("studyName");
		
		// Now we can extract the study and write it as a new attribute
		Study s = getRepository().getStudy(value);
		
		// If we don't find a value, we can fail at this stage.
		if (s == null) {
			String message = MessageFormat.format("Can't find study: {}", value);
			logger.warn(message);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
		}
		
		// Permissions checking might also be a sensible idea
    	Subject currentUser = SecurityUtils.getSubject();
		PrincipalCollection principals = currentUser.getPrincipals();
		String user = principals.getPrimaryPrincipal().toString();
    	logger.debug("Authenticated as: {}", user);
    	
    	Boolean viewPermitted = currentUser.isPermitted(s.getName() + ":view");
    	Boolean accessPermitted = viewPermitted;
    	
    	if (! accessPermitted) {
			JsonNode options = s.getOptions();
			if (options != null && options.has("public") && options.get("public").asBoolean()) {
				accessPermitted = true;
			}
    	}
    	
    	if (! accessPermitted) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	logger.debug("OK, continuing with the study about: {}", s.getName());
    	StudyAboutResponse about = new StudyAboutResponse();
    	about.setId(s.getId());
    	about.setName(s.getName());
    	about.setDescription(s.getDescription());
    	about.setAbout(s.getAbout());
    	about.getAccess().setView(viewPermitted);
    	about.getAccess().setAbout(true);
    	
    	RequestAttributes.setRequestStudyAbout(request, about);

		return CONTINUE;
	}

}
