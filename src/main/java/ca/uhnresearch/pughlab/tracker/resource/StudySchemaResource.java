package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
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
    	
    	JsonNode data;
    	
    	try {
			data = converter.toObject(input, JsonNode.class, this);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

    	Subject currentUser = SecurityUtils.getSubject();

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	return getResource();
    }
    
    /**
     * Builds a StudySchemaResponseDTO from the request and the repository
     * information.  
     */
	@Override
	public void buildResponseDTO(StudySchemaResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());

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
