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
