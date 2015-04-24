package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
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

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudySchemaResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;

public class StudySchemaResource extends StudyRepositoryResource<StudySchemaResponseDTO> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JacksonConverter converter = new JacksonConverter();

	/**
	 * Returns a StudySchemaResponseDTO for the study schema. 
	 * @return the schema
	 */
    @Get("json")
    public Representation getResource()  {
    	
    	StudySchemaResponseDTO response = new StudySchemaResponseDTO();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<StudySchemaResponseDTO>(response);    	
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
    	logger.info("Called putResource() in EntityFieldResource", input);
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	Studies study = (Studies) getRequest().getAttributes().get("study");

    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	try {
			StudySchemaResponseDTO schema = converter.toObject(input, StudySchemaResponseDTO.class, this);
			logger.info("Got a new schema {}", schema);
			
			List<Attributes> attributes = new ArrayList<Attributes>();
			for(AttributeDTO a : schema.getAttributes()) {
				attributes.add(a.getAttributes());
			}
			
			List<Views> views = new ArrayList<Views>();
			for(ViewDTO v : schema.getViews()) {
				views.add(v.getViews());
			}

			getRepository().setStudyAttributes(study, attributes);
			getRepository().setStudyViews(study, views);
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
    	
    	return getResource();
    }
    
    /**
     * Builds a StudySchemaResponseDTO from the request and the repository
     * information.  
     */
	@Override
	public void buildResponseDTO(StudySchemaResponseDTO dto) throws ResourceException {
		super.buildResponseDTO(dto);
		
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}

    	// Query the database for views
    	List<Views> viewList = getRepository().getStudyViews(study);
    	
    	// Now translate into DTOs
    	for(Views v : viewList) {
    		
    		// Add the view if we have a read permission
    		String permission = "view:read:" + study.getName() + "-" + v.getName();
    		if (adminUser || currentUser.isPermitted(permission)) {
    			dto.getViews().add(new ViewDTO(v));
    		}
    	}
    	
    	// And get the attributes for the study
    	List<Attributes> attributes = getRepository().getStudyAttributes(study);
    	for(Attributes a : attributes) {
    		dto.getAttributes().add(new AttributeDTO(a));
		}

    	dto.setStudy(new StudyDTO(study));
	}
}
