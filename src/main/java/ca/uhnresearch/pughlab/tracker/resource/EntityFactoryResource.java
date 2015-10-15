package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class EntityFactoryResource extends StudyRepositoryResource<EntityResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

    @Post("json")
    public Representation postResource(Representation input) {
    	
    	// Permissions -- write permission is needed to create a new record
    	Subject currentUser = SecurityUtils.getSubject();

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	boolean createUser = currentUser.isPermitted(study.getName() + ":create");
    	if (! createUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	View view = RequestAttributes.getRequestView(getRequest());

    	// First of all, we should try to deserialize what we have as an input.
    	// The interesting part is the entity field, which should contain the 
    	// initial fields. 
    	
    	// And now to grab the new attributes and render back.
    	try {
    		EntityResponse caseData = converter.toObject(input, EntityResponse.class, this);
			logger.debug("Got new case data {}", caseData);
			
			PrincipalCollection principals = currentUser.getPrincipals();
			String user = principals.getPrimaryPrincipal().toString();
			Cases newCase = getRepository().newStudyCase(study, view, user);
			if (newCase == null) {
				throw new RuntimeException("Error creating new case");
			}
			
			// And now we should write any attributes we have into the new
			// case. 
			ObjectNode attributes = caseData.getEntity();
			Iterator<Map.Entry<String,JsonNode>> fieldIterator = attributes.fields();
			while(fieldIterator.hasNext()) {
				Map.Entry<String,JsonNode> field = fieldIterator.next();
				String attributeName = field.getKey();
				JsonNode attributeValue = field.getValue();
				Attributes attribute = getRepository().getStudyAttribute(study, attributeName);
				getRepository().setCaseAttributeValue(study, view, newCase, attribute, user, attributeValue);
			}
			
			RequestAttributes.setRequestEntity(getRequest(), newCase);

    	} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		};
		
		// Now we should locate the new entity and return it using the same response
		// type. This is a little problematic. 
    	
    	EntityResponse response = new EntityResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityResponse>(response);
    }


	@Override
	public void buildResponseDTO(EntityResponse dto) {
		super.buildResponseDTO(dto);
		
    	logger.debug("Called getResource() in EntityResource");

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	View view = RequestAttributes.getRequestView(getRequest());
    	Cases caseValue = RequestAttributes.getRequestEntity(getRequest());
    	
    	ObjectNode caseData = getRepository().getCaseData(study, view, caseValue);
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setEntity(caseData);
	}
}
