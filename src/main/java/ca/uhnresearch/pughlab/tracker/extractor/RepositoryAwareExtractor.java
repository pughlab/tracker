package ca.uhnresearch.pughlab.tracker.extractor;

import org.restlet.routing.Extractor;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;

public abstract class RepositoryAwareExtractor extends Extractor {

	private StudyRepository repository;

	/**
	 * Sets the repository
	 * @param repository
	 */
	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	/**
	 * @return the repository
	 */
	public StudyRepository getRepository() {
		return repository;
	}
}
