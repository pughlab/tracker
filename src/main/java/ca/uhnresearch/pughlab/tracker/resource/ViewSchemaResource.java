package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewSchemaResponse;

/**
 * Restlet resource for the view schema endpoint.
 */
public class ViewSchemaResource extends StudyRepositoryResource<ViewSchemaResponse> {
	
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(ViewSchemaResource.class);

	/**
	 * A converter for Jackson rendering to/from JSON.
	 */
	private JacksonConverter converter = new JacksonConverter();

	/**
	 * Handles GET requests on a view schema. 
	 * @return a representation
	 */
    @Get("json")
    public Representation getResource()  {
    	
    	final ViewSchemaResponse response = new ViewSchemaResponse();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<ViewSchemaResponse>(response);    	
    }

    /**
     * Handles PUT requests on a view schema. A copy of the schema is generated and 
     * returned after it is written, which ensures the front-end is consistent
     * with the data store.
     * @param input the new schema
     * @return a representation
     */
    @Put("json")
    public Representation putResource(Representation input) {
    	logger.debug("Called putResource() in ViewSchemaResource", input);
    	
    	final Subject currentUser = SecurityUtils.getSubject();

    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	final View view = RequestAttributes.getRequestView(getRequest());

    	final boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	if (! adminUser) {
			String message = MessageFormat.format("No administrator access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}

    	// And now to grab the new attributes and render back.
    	try {
    		final ViewSchemaResponse schema = converter.toObject(input, ViewSchemaResponse.class, this);
			logger.debug("Got a new schema {}", schema);
			
			final List<ViewAttributes> attributes = new ArrayList<ViewAttributes>();
			for(ViewAttributes a : schema.getAttributes()) {
				attributes.add(a);
			}

			// Write the view attributes
			getRepository().setViewAttributes(study, view, attributes);
			
			// And write the view options if we should
			view.setOptions(schema.getView().getOptions());
			view.setBody(schema.getView().getBody());
			getRepository().setStudyView(study, view);
			
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}
    	
    	return getResource();
    }
    
    /**
     * Builds a response DTO for a view schema request
     * @param dto the response DTO to build
     */
	@Override
	public void buildResponseDTO(ViewSchemaResponse dto) throws ResourceException {
		super.buildResponseDTO(dto);
		
		final Study study = RequestAttributes.getRequestStudy(getRequest());
		final View view = RequestAttributes.getRequestView(getRequest());

		final Subject currentUser = SecurityUtils.getSubject();

    	// Only administrators can get the schema
		final boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	if (! adminUser) {
			String message = MessageFormat.format("No administrator access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	
    	final List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
    	for (ViewAttributes a : attributes) {
    		dto.getAttributes().add(a);
    	}
   	}
}
