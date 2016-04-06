package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
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
import ca.uhnresearch.pughlab.tracker.dto.StudyViewsResponse;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class StudyResource extends StudyRepositoryResource<StudyViewsResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource()  {
    	StudyViewsResponse response = new StudyViewsResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyViewsResponse>(response);
    }
    
    @Put("json")
    public Representation putResource(Representation input) {
    	
    	logger.debug("Called putResource() in StudyResource", input);
    	Subject currentUser = SecurityUtils.getSubject();
    	
		PrincipalCollection principals = currentUser.getPrincipals();
		String userName = principals.getPrimaryPrincipal().toString();

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	// Only administrators can save the study data
    	if (! currentUser.isPermitted(study.getName() + ":admin")) {
			String message = MessageFormat.format("No administrator access to study: {}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}

    	try {
    		StudyViewsResponse data = converter.toObject(input, StudyViewsResponse.class, this);
    		
    		if (data == null) {
    			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing or invalid request body");
    		}
			logger.debug("Got a study views response {}", data);
			
			// Set the id from the request URL, so we know which study to update
			Study update = data.getStudy();
			update.setId(study.getId());
			getRepository().saveStudy(update, userName);
			RequestAttributes.setRequestStudy(getRequest(), update);
			
    	} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getLocalizedMessage());
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}

    	return getResource();
    }

    
	@Override
	public void buildResponseDTO(StudyViewsResponse dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	dto.setStudy(study);
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");

    	// Query the database for views
    	List<View> viewList = getRepository().getStudyViews(study);
    	
    	// Now translate into DTOs
    	for(View v : viewList) {
    		
    		// Add the view if we have a read permission, or a write permission. See #64
    		if (adminUser || 
    				currentUser.isPermitted(study.getName() + ":read:" + v.getName()) ||
    				currentUser.isPermitted(study.getName() + ":write:" + v.getName())) {
    			dto.getViews().add(v);
    		}
    	}
	}

}
