package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dto.Role;

public class RoleExtractor extends Extractor {
	
	private AuthorizationRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(AuthorizationRepository repository) {
        this.repository = repository;
    }
	
	protected int beforeHandle(Request request, Response response) {
		
		String name = (String) request.getAttributes().get("roleName");
		
		// Now we can extract the study and write it as a new attribute
		Role role = repository.getRole(name);
		
		// If we don't find a value, we can fail at this stage.
		if (role == null) {
			logger.warn("Can't find role: {}", name);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		request.getAttributes().put("role", role);
		
		return CONTINUE;
	}
}
