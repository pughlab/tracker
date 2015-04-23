package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.AbstractResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

public abstract class StudyRepositoryResource<T extends AbstractResponseDTO> extends ServerResource {

	private StudyRepository repository;

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	public StudyRepository getRepository() {
		return repository;
	}

	public void buildResponseDTO(T dto) {
    	Subject currentUser = SecurityUtils.getSubject();
    	UserDTO user = new UserDTO(currentUser);
    	URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
	};
}