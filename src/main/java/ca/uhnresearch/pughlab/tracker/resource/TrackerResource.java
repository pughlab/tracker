package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyListResponseDTO;

public class TrackerResource extends StudyRepositoryResource<StudyListResponseDTO> {
		
    @Get("json")
    public Representation getResource()  {
    	StudyListResponseDTO response = new StudyListResponseDTO();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyListResponseDTO>(response);
    }

	@Override
	public void buildResponseDTO(StudyListResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	// Query the database for studies
    	List<Studies> studyList = getRepository().getAllStudies();
    	for(Studies s : studyList) {
    		String permission = "study:read:"+s.getName();
    		if (currentUser.isPermitted(permission)) {
    			dto.getStudies().add(new StudyDTO(s));
    		}
    	}
	}
}
