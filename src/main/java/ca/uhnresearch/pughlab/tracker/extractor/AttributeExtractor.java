package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class AttributeExtractor extends RepositoryAwareExtractor {
	
	private final Logger logger = LoggerFactory.getLogger(AttributeExtractor.class);

	protected int beforeHandle(Request request, Response response) {
		
		Study study = RequestAttributes.getRequestStudy(request);
		
		String value = (String) request.getAttributes().get("attributeName");
		logger.debug("Called AttributeExtractor beforeHandle: {}", value);
		
		// Now we can extract the attribute
		Attributes attribute = getRepository().getStudyAttribute(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (attribute == null) {
			logger.warn("Can't find attribute: {} in study {}", value, study.getName());
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		logger.debug("OK, continuing with the attribute: {}", attribute.getName());
		RequestAttributes.setRequestAttribute(request, attribute);
		
		return CONTINUE;
	}

}
