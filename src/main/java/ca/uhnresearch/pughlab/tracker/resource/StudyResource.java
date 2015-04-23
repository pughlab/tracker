package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyViewsResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;

public class StudyResource extends StudyRepositoryResource<StudyViewsResponseDTO> {
	
    @Get("json")
    public Representation getResource()  {
    	StudyViewsResponseDTO response = new StudyViewsResponseDTO();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyViewsResponseDTO>(response);
    }

    
	@Override
	public void buildResponseDTO(StudyViewsResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	dto.setStudy(new StudyDTO(study));
    	
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
	}

}
