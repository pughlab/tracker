package ca.uhnresearch.pughlab.tracker.extractor;

import java.text.MessageFormat;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

/**
 * Extracts an attribute name and object data, and stores them in the request attributes.
 * @author stuartw
 */
public class AttributeExtractor extends RepositoryAwareExtractor {
	
	private final Logger logger = LoggerFactory.getLogger(AttributeExtractor.class);

	/**
	 * Called to populate the request during extraction. This retrieves the 
	 * attribute name, looks it up in the study using the repository, and stores both
	 * the name and the attribute description in the request object. 
	 */
	protected int beforeHandle(Request request, Response response) {
		
		final Study study = RequestAttributes.getRequestStudy(request);
		
		final String value = (String) request.getAttributes().get("attributeName");
		logger.debug("Called AttributeExtractor beforeHandle: {}", value);
		
		// Now we can extract the attribute
		final Attributes attribute = getRepository().getStudyAttribute(study, value);
		
		// If we don't find a value, we can fail at this stage.
		if (attribute == null) {
			final String message = MessageFormat.format("Can't find attribute: {} in study {}", value, study.getName());
			logger.warn(message);
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
		}
		
		logger.debug("OK, continuing with the attribute: {}", attribute.getName());
		RequestAttributes.setRequestAttribute(request, attribute);
		
		return CONTINUE;
	}

}
