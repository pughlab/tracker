package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
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

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleListResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleListResource extends AuthorizationRepositoryResource<RoleListResponse>{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource() {
    	RoleListResponse response = new RoleListResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleListResponse>(response);
    }
    
    @Put("json")
    public Representation putResource(Representation input)  {
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	Study study = (Study) getRequest().getAttributes().get("study");
    	
    	if (! isPermitted(currentUser, study)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}

    	try {
    		RoleListResponse data = converter.toObject(input, RoleListResponse.class, this);
    		
    		if (data == null) {
    			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
    		}
			logger.debug("Got a new role response {}", data);
			
			// Special cases. If the study has id zero, it's the admin study, so 
			// exactly a single role is allowed.
			
			if (study.getId() == 0) {
				if (data.getRoles().size() != 1) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}
			
			// OK, we're attempting to save roles, so let's be safe
			// about this. First of all, though, we find the current roles, 
			// indexed by identifier, so we know which ones we have been asked 
			// to delete. 
			
			CaseQuery query = new CaseQuery();
			List<Role> oldRoles = getRepository().getStudyRoles(study, query);
			Map<Integer, Role> roleTable = new HashMap<Integer, Role>();
			for(Role oldRole : oldRoles) {
				roleTable.put(oldRole.getId(), oldRole);
			}
			
			for(Role roleToSave : data.getRoles()) {
				
				
				// If it's an existing role...
				if (roleToSave.getId() != null) {
					
					if (roleToSave.getStudyId() == study.getId()) {

						// Same study, so we're good to save it, and remove from the table
						roleTable.remove(roleToSave.getId());
						getRepository().saveStudyRole(study, roleToSave);
						continue;
					} else {
						
						// Different study, bad request as someone's trying to breach 
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
					}
					
				} else {
					
					roleToSave.setStudyId(study.getId());
					roleToSave.setStudyName(study.getName());
					getRepository().saveStudyRole(study, roleToSave);
				}
				
			}
			
			// Now remove any roles we've left in the table
			for(Role roleToDelete : roleTable.values()) {
				getRepository().deleteStudyRole(study, roleToDelete);
			}
			
    	} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
    	
    	return getResource();
    }
    
    /**
     * Checks permissions for the role list. The study might be null, if we're attempting a
     * non-study specific role list. 
     * @param currentUser
     * @param study
     * @return
     */
    private boolean isPermitted(Subject currentUser, Study study) {
    	if (currentUser.isPermitted("admin")) {
    		return true;
    	}
    	
    	String studyName = study.getName();
    	if (currentUser.isPermitted(studyName + ":admin")) {
    		return true;
    	}
    	
    	return false;
    }
    
	public void buildResponseDTO(RoleListResponse dto) {
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = new User(currentUser);
    	URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
    	
    	Study study = (Study) getRequest().getAttributes().get("study");
    	
    	if (! isPermitted(currentUser, study)) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	
    	Long roleCount = getRepository().getStudyRoleCount(study, query);
    	dto.getCounts().setTotal(roleCount);
    	
    	// Query the database for views
    	List<Role> roles;
		try {
			roles = getRepository().getStudyRoles(study, query);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

    	dto.setRoles(roles);
	};
}
