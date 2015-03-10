package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.StudyResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;

public class StudyResource extends ServerResource {

	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(StudyResource.class);

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

    @Get("json")
    public Representation getResource()  {

    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());

    	// Query the database for views
    	List<Views> viewList = repository.getStudyViews(study);
    	
    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);
    	StudyResponseDTO response = new StudyResponseDTO(url, user, study);
    	for(Views v : viewList) {
    		
    		// Add the view if we have a read permission
    		String permission = "view:read:" + study.getName() + "-" + v.getName();
    		if (adminUser || currentUser.isPermitted(permission)) {
    			response.getViews().add(new ViewDTO(v));
    		}
    	}
    	
    	// And render back
       	return new JacksonRepresentation<StudyResponseDTO>(response);
    }

}
