package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyListResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

public class TrackerResource extends StudyRepositoryResource {
		
	private final Logger logger = LoggerFactory.getLogger(TrackerResource.class);

    @Get("json")
    public Representation getResource()  {
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	logger.debug("Authenticated as: {}", currentUser.getPrincipal().toString());
    	
    	// Query the database for studies
    	List<Studies> studyList = getRepository().getAllStudies();
    	
    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);
    	StudyListResponseDTO response = new StudyListResponseDTO(url, user);
    	for(Studies s : studyList) {
    		String permission = "study:read:"+s.getName();
    		if (currentUser.isPermitted(permission)) {
    			response.getStudies().add(new StudyDTO(s));
    		}
    	}
    	
    	// And render back
       	return new JacksonRepresentation<StudyListResponseDTO>(response);
    }
}
