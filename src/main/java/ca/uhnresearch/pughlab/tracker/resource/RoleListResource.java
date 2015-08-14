package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleListResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleListResource extends AuthorizationRepositoryResource<RoleListResponse>{
	
    @Get("json")
    public Representation getResource() {
    	RoleListResponse response = new RoleListResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleListResponse>(response);
    }

	public void buildResponseDTO(RoleListResponse dto) {
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = new User(currentUser);
    	URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
    	
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	
    	Long roleCount = getRepository().getRoleCount(query);
    	dto.getCounts().setTotal(roleCount);
    	
    	// Query the database for views
    	List<Role> roles;
		try {
			roles = getRepository().getRoles(query);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

    	dto.setRoles(roles);
	};
}