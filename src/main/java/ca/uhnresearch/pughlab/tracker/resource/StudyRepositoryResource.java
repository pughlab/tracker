package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.AbstractResponse;
import ca.uhnresearch.pughlab.tracker.dto.User;

/**
 * A base class for all resources that depend on a repository, including setters so that the 
 * study repository can be injected. 
 * 
 * @author stuartw
 *
 * @param <T> the response type for this resource
 */
public abstract class StudyRepositoryResource<T extends AbstractResponse> extends ServerResource {
	
	/**
	 * The repository
	 */
	private StudyRepository repository;

	/**
	 * Sets the repository
	 * @param repository the repository
	 */
	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	/**
	 * Returns the repository
	 * @return the repository
	 */
	public StudyRepository getRepository() {
		return repository;
	}

	/**
	 * Builds a response DTO
	 * @param dto the DTO
	 */
	public void buildResponseDTO(T dto) {
		final Subject currentUser = SecurityUtils.getSubject();
		final User user = new User(currentUser);
		final URL url = getRequest().getRootRef().toUrl();

    	dto.setUser(user);
    	dto.setServiceUrl(url);
	};
}