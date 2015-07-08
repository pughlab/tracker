package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyViewsResponse;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class StudyResource extends StudyRepositoryResource<StudyViewsResponse> {
	
    @Get("json")
    public Representation getResource()  {
    	StudyViewsResponse response = new StudyViewsResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyViewsResponse>(response);
    }

    
	@Override
	public void buildResponseDTO(StudyViewsResponse dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
    	Study study = (Study) getRequest().getAttributes().get("study");
    	dto.setStudy(study);
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());

    	// Query the database for views
    	List<View> viewList = getRepository().getStudyViews(study);
    	
    	// Now translate into DTOs
    	for(View v : viewList) {
    		
    		// Add the view if we have a read permission
    		String permission = "view:read:" + study.getName() + "-" + v.getName();
    		if (adminUser || currentUser.isPermitted(permission)) {
    			dto.getViews().add(v);
    		}
    	}
	}

}
