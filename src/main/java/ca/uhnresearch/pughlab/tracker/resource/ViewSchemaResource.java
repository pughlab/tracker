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

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudySchemaResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewSchemaResponseDTO;

public class ViewSchemaResource extends StudyRepositoryResource<ViewSchemaResponseDTO> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource()  {
    	
    	ViewSchemaResponseDTO response = new ViewSchemaResponseDTO();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<ViewSchemaResponseDTO>(response);    	
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
    	logger.info("Called putResource() in ViewSchemaResource", input);
    	
    	Subject currentUser = SecurityUtils.getSubject();

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");

    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}

    	// And now to grab the new attributes and render back.
    	try {
    		ViewSchemaResponseDTO schema = converter.toObject(input, ViewSchemaResponseDTO.class, this);
			logger.info("Got a new schema {}", schema);
			
			List<Attributes> attributes = new ArrayList<Attributes>();
			for(AttributeDTO a : schema.getAttributes()) {
				attributes.add(a.getAttributes());
			}

			getRepository().setViewAttributes(study, view, attributes);

		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
    	
    	return getResource();
    }
    
	@Override
	public void buildResponseDTO(ViewSchemaResponseDTO dto) throws ResourceException {
		super.buildResponseDTO(dto);
		
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");

    	Subject currentUser = SecurityUtils.getSubject();

    	// Only administrators can get the schema
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));
    	
    	List<Attributes> attributes = getRepository().getViewAttributes(study, view);
    	for (Attributes a : attributes) {
    		dto.getAttributes().add(new AttributeDTO(a));
    	}
	}
}
