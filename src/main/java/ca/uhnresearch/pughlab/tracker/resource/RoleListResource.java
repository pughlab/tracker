package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
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

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleListResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleListResource extends AuthorizationRepositoryResource<RoleListResponse>{
	
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(RoleListResource.class);

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource() {
    	final RoleListResponse response = new RoleListResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleListResponse>(response);
    }
    
    @Put("json")
    public Representation putResource(Representation input)  {
    	
    	final Subject currentUser = SecurityUtils.getSubject();
    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	if (! isPermitted(currentUser, study)) {
    		final String message = MessageFormat.format("No access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}

    	try {
    		final RoleListResponse data = converter.toObject(input, RoleListResponse.class, this);
    		
    		if (data == null) {
    			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid data for request");
    		}
			logger.debug("Got a new role response {}", data);
			
			// OK, we're attempting to save roles, so let's be safe
			// about this. First of all, though, we find the current roles, 
			// indexed by identifier, so we know which ones we have been asked 
			// to delete. 
			
			final CasePager query = new CasePager();
			final List<Role> oldRoles = getRepository().getStudyRoles(study, query);
			final Map<Integer, Role> roleTable = new HashMap<Integer, Role>();
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
    		logger.error("IOException: " + e.getLocalizedMessage());
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (RepositoryException e) {
    		logger.error("RepositoryException: " + e.getLocalizedMessage());
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
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
		final Subject currentUser = SecurityUtils.getSubject();
		final User user = new User(currentUser);
		final URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
    	
    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	if (! isPermitted(currentUser, study)) {
			String message = MessageFormat.format("No access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	final CasePager query = RequestAttributes.getRequestCasePager(getRequest());
    	
    	final Long roleCount = getRepository().getStudyRoleCount(study, query);
    	dto.getCounts().setTotal(roleCount);
    	
    	// Query the database for views
		try {
			final List<Role> roles = getRepository().getStudyRoles(study, query);
	    	dto.setRoles(roles);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	};
}
