package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleResource extends AuthorizationRepositoryResource<RoleResponse> {

    @Get("json")
    public Representation getResource()  {
    	RoleResponse response = new RoleResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleResponse>(response);
    }

    @Put("json")
    public Representation putResource()  {
    	RoleResponse response = new RoleResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleResponse>(response);
    }

	public void buildResponseDTO(RoleResponse dto) {
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = new User(currentUser);
    	URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
    	
    	Role role = (Role) getRequest().getAttributes().get("role");
    	dto.setRole(role);
    	
    	// Query the database for users
    	List<String> users = getRepository().getRoleUsers(role);
    	dto.setUsers(users);
    	
    	// And for permissions
    	List<String> permissions = getRepository().getRolePermissions(role);
    	dto.setPermissions(permissions);
	};

}
