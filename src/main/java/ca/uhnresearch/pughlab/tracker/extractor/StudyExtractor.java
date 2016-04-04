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

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class StudyExtractor extends RepositoryAwareExtractor {
	
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

    	// Explicitly block access when there's not at least study read access
    	String permission = s.getName() + ":view";
    	logger.debug("Checking permission for: {}", permission);
    	
    	if (! currentUser.isPermitted(permission)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	logger.debug("OK, continuing with the study: {}", s.getName());
    	RequestAttributes.setRequestStudy(request, s);
		
		// Now, as well as extracting the study, we can start to build a query
		// that can be used to pull out the data. Store this as a query
		// attribute. 
		
		StudyCaseQuery query = getRepository().newStudyCaseQuery(s);
		request.getAttributes().put("query", query);
				
		return CONTINUE;
	}
}
