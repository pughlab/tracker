package ca.uhnresearch.pughlab.tracker.extractor;

import java.text.MessageFormat;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class ViewExtractor extends RepositoryAwareExtractor {
	
	private final Logger logger = LoggerFactory.getLogger(ViewExtractor.class);

	/**
	 * Checks that we have appropriate permissions, and sets a set of permission attributes.
	 * @param request the request
	 * @param study the study
	 * @param view the view
	 * @param currentUser the current authorized user
	 * @throws ResourceException when there are insufficient permissions
	 */
	private void checkPermissions(Request request, Study study, View view, Subject currentUser) throws ResourceException {
		
		final String studyAdminPermissionString = study.getName() + ":admin";
		final Boolean studyAdminPermission = currentUser.isPermitted(studyAdminPermissionString);
		Boolean viewReadPermission = studyAdminPermission;
		Boolean viewWritePermission = studyAdminPermission;
		
		if (studyAdminPermission) {
			// Do nothing, as all permissions are already true
		} else {
			String viewReadPermissionString = study.getName() + ":read:" + view.getName();
			viewReadPermission = currentUser.isPermitted(viewReadPermissionString);
			
			String viewWritePermissionString = study.getName() + ":write:" + view.getName();
			viewWritePermission = currentUser.isPermitted(viewWritePermissionString);
		}
		
		// If we have permission to write, by default allow reading too
		if (viewWritePermission) {
			viewReadPermission = viewWritePermission;
		}
		
		// If we can't read, throw an error
		if (! viewReadPermission) {
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		}
	}

	protected int beforeHandle(Request request, Response response) {
		
		final Study study = RequestAttributes.getRequestStudy(request);
		final String value = (String) request.getAttributes().get("viewName");
		logger.debug("Called ViewExtractor beforeHandle: {}", value);
		
		// Now we can extract the study and write it as a new attribute
		final View v = getRepository().getStudyView(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (v == null) {
			final String message = MessageFormat.format("Can't find view: {} in study {}", value, study.getName());
			logger.warn(message);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
		}
		
		// We should allow access based on a read permission for the view, or 
		// any permission on the study
		final Subject currentUser = SecurityUtils.getSubject();

		// We set a few permissions to include in the response. This is more a convenience,
		// as it allows the front end to enable controls. Actual access is blocked independently
		// in the appropriate endpoints. 
		checkPermissions(request, study, v, currentUser);
		
		logger.debug("OK, continuing with the view: {}", v.getName());
		RequestAttributes.setRequestView(request, v);
		
		// And add the view into the StudyCaseQuery
		StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(request);
		query = getRepository().addViewCaseMatcher(query, v);
		RequestAttributes.setRequestCaseQuery(request, query);
		
		return CONTINUE;
	}
}
