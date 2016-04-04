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
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudySchemaResponse;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class StudySchemaResource extends StudyRepositoryResource<StudySchemaResponse> {
	
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(StudySchemaResource.class);
	
	private JacksonConverter converter = new JacksonConverter();

	/**
	 * Returns a StudySchemaResponseDTO for the study schema. 
	 * @return the schema
	 */
    @Get("json")
    public Representation getResource()  {
    	
    	StudySchemaResponse response = new StudySchemaResponse();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<StudySchemaResponse>(response);    	
    }
    
    /**
     * Writes a new study schema. A copy of the schema is generated and 
     * returned after it is written, which ensures the front-end is consistent
     * with the data store.
     * @param input the new schema
     * @return a representation
     */
    @Put("json")
    public Representation putResource(Representation input) {
    	logger.debug("Called putResource() in EntityFieldResource", input);
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	Study study = RequestAttributes.getRequestStudy(getRequest());

    	boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	if (! adminUser) {
			String message = MessageFormat.format("No administrator access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	try {
			StudySchemaResponse schema = converter.toObject(input, StudySchemaResponse.class, this);
			logger.debug("Got a new schema {}", schema);
			
			List<Attributes> attributes = new ArrayList<Attributes>();
			for(Attributes a : schema.getAttributes()) {
				attributes.add(a);
			}
			
			List<View> views = new ArrayList<View>();
			for(View v : schema.getViews()) {
				views.add(v);
			}

			getRepository().setStudyAttributes(study, attributes);
			getRepository().setStudyViews(study, views);
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getLocalizedMessage());
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}
    	
    	return getResource();
    }
    
    /**
     * Builds a StudySchemaResponseDTO from the request and the repository
     * information.  
     */
	@Override
	public void buildResponseDTO(StudySchemaResponse dto) throws ResourceException {
		super.buildResponseDTO(dto);
		
    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	if (! adminUser) {
			String message = MessageFormat.format("No administrator access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}

    	// Query the database for views
    	List<View> viewList = getRepository().getStudyViews(study);
    	
    	// Now translate into DTOs
    	for(View v : viewList) {
    		dto.getViews().add(v);
    	}
    	
    	// And get the attributes for the study
    	List<Attributes> attributes = getRepository().getStudyAttributes(study);
    	for(Attributes a : attributes) {
    		dto.getAttributes().add(a);
		}

    	dto.setStudy(study);
	}
}
