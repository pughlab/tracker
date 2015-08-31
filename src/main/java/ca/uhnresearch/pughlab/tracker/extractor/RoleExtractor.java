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

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;

public class RoleExtractor extends Extractor {

    private AuthorizationRepository repository;
    
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(AuthorizationRepository repository) {
        this.repository = repository;
    }

    protected AuthorizationRepository getRepository() {
        return repository;
    }

	protected int beforeHandle(Request request, Response response) {

		Study study = (Study) request.getAttributes().get("study");
		
		// No study => send a 400 error
		if (study == null) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		// We should allow access based on a read permission for the view, or 
		// any permission on the study
    	Subject currentUser = SecurityUtils.getSubject();

		String studyAdminPermissionString = study.getName() + ":admin";
		Boolean studyAdminPermission = currentUser.isPermitted(studyAdminPermissionString);
		
		if (! studyAdminPermission) {
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		}

		String name = (String) request.getAttributes().get("roleName");

		// Now we can extract the study and write it as a new attribute
		Role role = null;
		try {
			role = getRepository().getStudyRole(study, name);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		// If we don't find a value, we can fail at this stage.
		if (role == null) {
			logger.warn("Can't find role: {}", name);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		request.getAttributes().put("role", role);
		
		return CONTINUE;

	}
}
