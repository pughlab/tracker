package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.StudyListResponse;
import ca.uhnresearch.pughlab.tracker.dto.StudyWithAccess;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;

public class TrackerResource extends StudyRepositoryResource<StudyListResponse> {
		
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(TrackerResource.class);

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource()  {
    	StudyListResponse response = new StudyListResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyListResponse>(response);
    }

    
    @Post("json")
    public Representation postResource(Representation input) {
    	
    	// Permissions -- admin permission is needed to create a new study
    	Subject currentUser = SecurityUtils.getSubject();
    	if (! currentUser.isPermitted("admin")) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
		PrincipalCollection principals = currentUser.getPrincipals();
		String userName = principals.getPrimaryPrincipal().toString();

    	Study savedStudy;
    	
    	try {
    		Study newStudy = converter.toObject(input, Study.class, this);
			logger.debug("Got new studyt data {}", newStudy.toString());

			savedStudy = getRepository().saveStudy(newStudy, userName);
	    	
    	} catch (IOException e) {
    		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
    	} catch (RepositoryException e) {
    		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
    	
    	Reference reference = getRequest().getRootRef().clone().addSegment("api").addSegment("studies").addSegment(savedStudy.getName());
    	redirectSeeOther(reference);
    	return new ReferenceList(Arrays.asList(reference)).getTextRepresentation();
    }
    
    
	@Override
	public void buildResponseDTO(StudyListResponse dto) {
		super.buildResponseDTO(dto);
		
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	if (currentUser.isPermitted("admin")) {
    		Reference reference = getRequest().getRootRef().clone().addSegment("api").addSegment("studies");
    		dto.getActions().put("create", reference.toUrl());
    	}
    	
    	// Query the database for studies
    	List<Study> studyList = getRepository().getAllStudies();
    	for(Study s : studyList) {
    		
    		String studyAdminPermissionString = s.getName() + ":admin";
    		Boolean studyAdminPermission = currentUser.isPermitted(studyAdminPermissionString);
    		Boolean studyViewPermission = studyAdminPermission;
    		Boolean studyAboutPermission = studyAdminPermission;
    		
    		if (studyAdminPermission) {
    			// Do nothing, as all permissions are already true
    		} else if (currentUser.isPermitted(s.getName() + ":view")) {
    			// Check for view permission
    			studyViewPermission = true;
    		}
    		
    		if (! studyViewPermission && s.getAbout() != null && ! s.getAbout().equals("")) {
    			// Or a public study option
    			JsonNode options = s.getOptions();
    			if (options != null && options.has("public") && options.get("public").asBoolean()) {
    				studyAboutPermission = true;
    			}
    		}

    		// For each study, we also ought to derive the precise nature of the
    		// allowed permissions, and embed them in a permissions DTO.
    		
    		if (studyViewPermission || studyAboutPermission) {
    			StudyWithAccess study = new StudyWithAccess();
    			study.setId(s.getId());
    			study.setName(s.getName());
    			study.setDescription(s.getDescription());
    			study.setOptions(s.getOptions());
    			study.setAbout(s.getAbout());
    			study.getAccess().setAdmin(studyAdminPermission);
    			study.getAccess().setView(studyViewPermission);
    			study.getAccess().setAbout(studyViewPermission || studyAboutPermission);
    			dto.getStudies().add(study);
    		}
    	}
	}
}
