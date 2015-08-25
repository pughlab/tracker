package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleListResponse;
import ca.uhnresearch.pughlab.tracker.dto.RoleResponse;
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
    
    @Post("json")
    public void postResource(Representation input)  {
    	try {
    		RoleResponse data = converter.toObject(input, RoleResponse.class, this);
			logger.debug("Got a new role response {}", data);

			Role role = data.getRole();
			getRepository().saveRole(role);
			
			role = getRepository().getRole(role.getName());
			
			getRequest().getAttributes().put("role", role);

			Reference target = new Reference(getRequest().getOriginalRef(), "./" + Reference.encode(role.getName()));
	    	getResponse().redirectSeeOther(target);
	    	getResponse().setStatus(Status.SUCCESS_CREATED);

    	} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
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
    	
    	if (study == null) {
    		return false;
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
    	
    	Long roleCount = getRepository().getRoleCount(query);
    	dto.getCounts().setTotal(roleCount);
    	
    	// Query the database for views
    	List<Role> roles;
		try {
			if (study == null) {
				roles = getRepository().getRoles(query);
			} else {
				roles = getRepository().getStudyRoles(study, query);
			}
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

    	dto.setRoles(roles);
	};
}
