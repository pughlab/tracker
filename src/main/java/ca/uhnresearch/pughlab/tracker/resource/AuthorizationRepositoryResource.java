package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dto.AbstractResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class AuthorizationRepositoryResource<T extends AbstractResponse> extends ServerResource {

	private AuthorizationRepository repository;

	@Required
    public void setRepository(AuthorizationRepository repository) {
        this.repository = repository;
    }
	
	public AuthorizationRepository getRepository() {
		return repository;
	}

	public void buildResponseDTO(T dto) {
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = new User(currentUser);
    	URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
	};
}