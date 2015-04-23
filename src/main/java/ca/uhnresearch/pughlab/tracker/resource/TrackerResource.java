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
import ca.uhnresearch.pughlab.tracker.dto.StudyWithAccessDTO;

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
    			StudyWithAccessDTO studyDTO = new StudyWithAccessDTO(s);
    			studyDTO.getAccess().setReadAllowed(studyReadPermission);
    			studyDTO.getAccess().setWriteAllowed(studyWritePermission);
    			studyDTO.getAccess().setDownloadAllowed(studyDownloadPermission);
    			dto.getStudies().add(studyDTO);
    		}
    	}
	}
}
