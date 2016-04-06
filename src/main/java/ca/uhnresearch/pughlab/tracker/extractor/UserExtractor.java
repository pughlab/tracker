package ca.uhnresearch.pughlab.tracker.extractor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.User;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

/**
 * Extracts all the parameters for a limit and an offset, and any other case query 
 * values, and drops them into a new CasePager attribute that is added to the list
 * of attributes. 
 */
public class UserExtractor extends Extractor {
	
	private final Logger logger = LoggerFactory.getLogger(UserExtractor.class);

    private AuthorizationRepository repository;

	@Required
    public void setRepository(AuthorizationRepository repository) {
        this.repository = repository;
    }

    protected AuthorizationRepository getRepository() {
        return repository;
    }

	protected int beforeHandle(Request request, Response response) {
		
		Subject currentUser = SecurityUtils.getSubject();
		User user = new User(currentUser);
		
		try {
			getRepository().saveUser(user);
		} catch (RepositoryException e) {
			// Do nothing, but log the error
			logger.error(e.getLocalizedMessage());
		}
		
		RequestAttributes.setRequestUser(request, user);

		return CONTINUE;
	}

}
