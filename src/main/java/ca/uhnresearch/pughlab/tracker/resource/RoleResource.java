package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

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

import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.RoleResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class RoleResource extends AuthorizationRepositoryResource<RoleResponse> {

	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(RoleResource.class);

	private JacksonConverter converter = new JacksonConverter();
	
    @Get("json")
    public Representation getResource()  {
    	final RoleResponse response = new RoleResponse();
    	buildResponseDTO(response);
       	return new JacksonRepresentation<RoleResponse>(response);
    }
    
    @Put("json")
    public Representation putResource(Representation input)  {
    	logger.debug("Got an update", input);
    	
    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	try {
    		final RoleResponse data = converter.toObject(input, RoleResponse.class, this);
			logger.debug("Got a new role response {}", data);
			
			Role role = data.getRole();
			
			getRepository().saveStudyRole(study, role);
			
			// Search for the role again because we might have renamed it
			// This ensures we have an identifier. See #14
			role = getRepository().getStudyRole(study, role.getName());
						
			RequestAttributes.setRequestRole(getRequest(), role);
			
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getLocalizedMessage());
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
    	
    	if (study == null) {
    		return false;
    	}
    	
    	final String studyName = study.getName();
    	if (currentUser.isPermitted(studyName + ":admin")) {
    		return true;
    	}
    	
    	return false;
    }

	public void buildResponseDTO(RoleResponse dto) {
		final Subject currentUser = SecurityUtils.getSubject();
		final Study study = RequestAttributes.getRequestStudy(getRequest());
    	
    	if (! isPermitted(currentUser, study)) {
    		final String message = MessageFormat.format("No access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	final User user = new User(currentUser);
    	final URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
    	
    	Role role = RequestAttributes.getRequestRole(getRequest());
    	dto.setRole(role);
	};
}
