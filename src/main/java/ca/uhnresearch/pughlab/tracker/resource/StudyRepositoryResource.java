package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;

public class StudyRepositoryResource extends ServerResource {

	private StudyRepository repository;

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	public StudyRepository getRepository() {
		return repository;
	}

}