package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyListResponse;
import ca.uhnresearch.pughlab.tracker.dto.StudyWithAccess;

public class TrackerResource extends StudyRepositoryResource<StudyListResponse> {
		
    @Get("json")
    public Representation getResource()  {
    	StudyListResponse response = new StudyListResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyListResponse>(response);
    }

	@Override
	public void buildResponseDTO(StudyListResponse dto) {
		super.buildResponseDTO(dto);
		
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	// Query the database for studies
    	List<Study> studyList = getRepository().getAllStudies();
    	for(Study s : studyList) {
    		
    		String studyAdminPermissionString = "study:admin:" + s.getName();
    		Boolean studyAdminPermission = currentUser.isPermitted(studyAdminPermissionString);
    		Boolean studyReadPermission = studyAdminPermission;
    		Boolean studyWritePermission = studyAdminPermission;
    		Boolean studyDownloadPermission = studyAdminPermission;
    		
    		if (studyAdminPermission) {
    			// Do nothing, as all permissions are already true
    		} else {
    			String studyReadPermissionString = "study:read:" + s.getName();
    			studyReadPermission = currentUser.isPermitted(studyReadPermissionString);
    			
    			String studyWritePermissionString = "study:write:" + s.getName();
    			studyWritePermission = currentUser.isPermitted(studyWritePermissionString);

    			String studyDownloadPermissionString = "study:download:" + s.getName();
    			studyDownloadPermission = currentUser.isPermitted(studyDownloadPermissionString);
    		}

    		if (studyWritePermission) {
    			studyReadPermission = studyWritePermission;
    		}
    		
  
    		// For each study, we also ought to derive the precise nature of the
    		// allowed permissions, and embed them in a permissions DTO.
    		
    		if (studyReadPermission) {
    			StudyWithAccess study = new StudyWithAccess();
    			study.setId(s.getId());
    			study.setName(s.getName());
    			study.setDescription(s.getDescription());
    			study.getAccess().setReadAllowed(studyReadPermission);
    			study.getAccess().setWriteAllowed(studyWritePermission);
    			study.getAccess().setDownloadAllowed(studyDownloadPermission);
    			dto.getStudies().add(study);
    		}
    	}
	}
}
