package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AbstractResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

public class AuditLogRepositoryResource<T extends AbstractResponse> extends ServerResource {

	private AuditLogRepository repository;

	@Required
    public void setRepository(AuditLogRepository repository) {
        this.repository = repository;
    }
	
	public AuditLogRepository getRepository() {
		return repository;
	}

	public void buildResponseDTO(T dto) {
		final Subject currentUser = SecurityUtils.getSubject();
		final User user = new User(currentUser);
		final URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
	};

}
