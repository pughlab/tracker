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

    	// Explicitly block access when there's not at least study read access
    	String studyAdminPermission = "study:admin:" + study.getName();
    	String viewPermission = "view:read:" + study.getName() + "-" + v.getName();
    	if (! currentUser.isPermitted(studyAdminPermission) && ! currentUser.isPermitted(viewPermission)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	logger.info("OK, continuing with the view: {}", v.getName());
		request.getAttributes().put("view", v);
		
		// We set a few permissions to include in the response. This is more a convenience,
		// as it allows the front end to enable controls. Actual access is blocked independently
		// in the appropriate endpoints. 
		
		logger.info("Adding in permissions");
		request.getAttributes().put("viewReadAllowed", true);
		
		String viewWritePermission = "view:write:" + study.getName() + "-" + v.getName();
		request.getAttributes().put("viewWriteAllowed", currentUser.isPermitted(studyAdminPermission) && currentUser.isPermitted(viewWritePermission));
		
		String viewDownloadPermission = "view:download:" + study.getName() + "-" + v.getName();
		request.getAttributes().put("viewDownloadAllowed", currentUser.isPermitted(studyAdminPermission) && currentUser.isPermitted(viewDownloadPermission));
				
		return CONTINUE;
	}

}
