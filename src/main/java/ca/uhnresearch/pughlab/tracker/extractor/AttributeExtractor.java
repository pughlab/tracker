package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class AttributeExtractor extends Extractor {
	
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * @return the repository
	 */
	public StudyRepository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	@Required
	public void setRepository(StudyRepository repository) {
		this.repository = repository;
	}

	protected int beforeHandle(Request request, Response response) {
		
		Study study = RequestAttributes.getRequestStudy(request);
		
		String value = (String) request.getAttributes().get("attributeName");
		logger.debug("Called AttributeExtractor beforeHandle: {}", value);
		
		// Now we can extract the attribute
		Attributes attribute = repository.getStudyAttribute(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (attribute == null) {
			logger.warn("Can't find attribute: {} in study {}", value, study.getName());
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		// We should allow access based on a read permission for the view, or 
		// any permission on the study
//    	Subject currentUser = SecurityUtils.getSubject();

		// We set a few permissions to include in the response. This is more a convenience,
		// as it allows the front end to enable controls. Actual access is blocked independently
		// in the appropriate endpoints. 
//		checkPermissions(request, study, v, currentUser);
		
		logger.debug("OK, continuing with the attribute: {}", attribute.getName());
		RequestAttributes.setRequestAttribute(request, attribute);
		
		return CONTINUE;
	}

}
