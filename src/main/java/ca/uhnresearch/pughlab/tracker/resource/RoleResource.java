package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;

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

import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleResource extends AuthorizationRepositoryResource<RoleResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Get("json")
    public Representation getResource()  {
    	RoleResponse response = new RoleResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleResponse>(response);
    }

    @Put("json")
    public Representation putResource(Representation input)  {
    	logger.debug("Got an update", input);
    	
    	try {
    		RoleResponse data = converter.toObject(input, RoleResponse.class, this);
			logger.debug("Got a new role response {}", data);
			
			Role role = data.getRole();
			
			getRepository().saveRole(role);
			getRepository().setRoleUsers(role, data.getUsers());
			getRepository().setRolePermissions(role, data.getPermissions());
			
			getRequest().getAttributes().put("role", role);
			
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
    	
    	return getResource();
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
