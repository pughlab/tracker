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
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.ViewPermissionsDTO;

public class ViewExtractor extends Extractor {
	
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	/**
	 * Checks that we have appropriate permissions, and sets a set of permission attributes.
	 * @param request the request
	 * @param study the study
	 * @param view the view
	 * @param currentUser the current authorized user
	 * @throws ResourceException when there are insufficient permissions
	 */
	private void checkPermissions(Request request, Studies study, Views view, Subject currentUser) throws ResourceException {
		
		String studyAdminPermissionString = "study:admin:" + study.getName();
		Boolean studyAdminPermission = currentUser.isPermitted(studyAdminPermissionString);
		Boolean viewReadPermission = studyAdminPermission;
		Boolean viewWritePermission = studyAdminPermission;
		Boolean viewDownloadPermission = studyAdminPermission;
		
		if (studyAdminPermission) {
			// Do nothing, as all permissions are already true
		} else {
			String viewReadPermissionString = "view:read:" + study.getName() + "-" + view.getName();
			viewReadPermission = currentUser.isPermitted(viewReadPermissionString);
			
			String viewWritePermissionString = "view:write:" + study.getName() + "-" + view.getName();
			viewWritePermission = currentUser.isPermitted(viewWritePermissionString);

			String viewDownloadPermissionString = "view:download:" + study.getName() + "-" + view.getName();
			viewDownloadPermission = currentUser.isPermitted(viewDownloadPermissionString);
		}
		
		// If we have permission to write, by default allow reading too
		if (viewWritePermission) {
			viewReadPermission = viewWritePermission;
		}
		
		// If we can't read, throw an error
		if (! viewReadPermission) {
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		}
		
		request.getAttributes().put("viewReadAllowed", viewReadPermission);
		request.getAttributes().put("viewWriteAllowed", viewWritePermission);
		request.getAttributes().put("viewDownloadAllowed", viewDownloadPermission);
	}

	protected int beforeHandle(Request request, Response response) {
		
		Studies study = (Studies) request.getAttributes().get("study");
		String value = (String) request.getAttributes().get("viewName");
		logger.info("Called ViewExtractor beforeHandle: {}", value);
		
		// Now we can extract the study and write it as a new attribute
		Views v = repository.getStudyView(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (v == null) {
			logger.warn("Can't find view: {} in study {}", value, study.getName());
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		// We should allow access based on a read permission for the view, or 
		// any permission on the study
    	Subject currentUser = SecurityUtils.getSubject();

		// We set a few permissions to include in the response. This is more a convenience,
		// as it allows the front end to enable controls. Actual access is blocked independently
		// in the appropriate endpoints. 
		checkPermissions(request, study, v, currentUser);
		
		logger.info("OK, continuing with the view: {}", v.getName());
		request.getAttributes().put("view", v);
		
		return CONTINUE;
	}
}
