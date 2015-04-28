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
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewSchemaResponse;

public class ViewSchemaResource extends StudyRepositoryResource<ViewSchemaResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource()  {
    	
    	ViewSchemaResponse response = new ViewSchemaResponse();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<ViewSchemaResponse>(response);    	
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

    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");

    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}

    	// And now to grab the new attributes and render back.
    	try {
    		ViewSchemaResponse schema = converter.toObject(input, ViewSchemaResponse.class, this);
			logger.info("Got a new schema {}", schema);
			
			List<ViewAttributes> attributes = new ArrayList<ViewAttributes>();
			for(ViewAttributes a : schema.getAttributes()) {
				attributes.add(a);
			}

			// Write the view attributes
			getRepository().setViewAttributes(study, view, attributes);
			
			// And write the view options if we should
			view.setOptions(schema.getView().getOptions());
			getRepository().setStudyView(study, view);
			
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
	public void buildResponseDTO(ViewSchemaResponse dto) throws ResourceException {
		super.buildResponseDTO(dto);
		
    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");

    	Subject currentUser = SecurityUtils.getSubject();

    	// Only administrators can get the schema
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	
    	List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
    	for (ViewAttributes a : attributes) {
    		dto.getAttributes().add(a);
    	}
   	}
}
