package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class EntityResource extends StudyRepositoryResource<EntityResponse> {
	
	/**
	 * A logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(EntityResource.class);
	
    @Get("json")
    public Representation getResource() {
    	final EntityResponse response = new EntityResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityResponse>(response);
    }

	@Delete()
	public void deleteResource()  {
		final Subject currentUser = SecurityUtils.getSubject();
		
		final Request request = getRequest();
		final Study study = RequestAttributes.getRequestStudy(request);
		final StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(request);

		final Boolean deletePermitted = currentUser.isPermitted(study.getName() + ":delete");
    	if (! deletePermitted) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "No delete permission for this resource");
    	}

    	final PrincipalCollection principals = currentUser.getPrincipals();
    	final String user = principals.getPrimaryPrincipal().toString();

    	try {
			getRepository().deleteCases(query, user);
    	} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		};
	}
	
	@Override
	public void buildResponseDTO(EntityResponse dto) {
		super.buildResponseDTO(dto);
		
    	logger.debug("Called getResource() in EntityResource");

    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	final View view = RequestAttributes.getRequestView(getRequest());
    	final StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(getRequest());
    	
    	final List<ObjectNode> cases = getRepository().getCaseData(query, view);
        if (cases.isEmpty()) {
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    	}

    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setEntity(cases.get(0));
	}
}
