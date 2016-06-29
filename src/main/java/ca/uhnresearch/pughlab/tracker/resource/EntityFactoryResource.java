package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
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

import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponse;
import ca.uhnresearch.pughlab.tracker.dto.NewEntityRequestBody;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityFactoryResource extends StudyRepositoryResource<EntityResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private JacksonConverter converter = new JacksonConverter();

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    @Post("json")
    public Representation postResource(Representation input) {
    	
    	// Permissions -- write permission is needed to create a new record
    	final Subject currentUser = SecurityUtils.getSubject();

    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	final boolean createUser = currentUser.isPermitted(study.getName() + ":create");
    	if (! createUser) {
    		final String message = MessageFormat.format("No create access for study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	final View view = RequestAttributes.getRequestView(getRequest());
    	StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(getRequest());

    	// First of all, we should try to deserialize what we have as an input.
    	// The interesting part is the entity field, which should contain the 
    	// initial fields. 
    	
    	// And now to grab the new attributes and render back.
    	try {
    		final NewEntityRequestBody caseData = converter.toObject(input, NewEntityRequestBody.class, this);
			logger.debug("Got new case data {}", caseData);
			
			ObjectNode attributes = caseData.getEntity();
			Iterator<Map.Entry<String,JsonNode>> fieldIterator = attributes.fields();

			// Check permissions before we create a new case
			while(fieldIterator.hasNext()) {
				final Map.Entry<String,JsonNode> field = fieldIterator.next();

				if (! currentUser.isPermitted(study.getName() + ":write:" + view.getName())) {
					String message = MessageFormat.format("Forbidden: no write access for study: {0}, view: {1}", study.getName(), view.getName());
					throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
				}

				final String attributeName = field.getKey();
				final Attributes attribute = getRepository().getStudyAttribute(study, attributeName);
				if (attribute == null) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Can't write attribute: " + attributeName);
				}
				
				if (! currentUser.isPermitted(study.getName() + ":attribute:write:" + attribute.getName())) {
					String message = MessageFormat.format("No write access for study: {0}, attribute: {1}", study.getName(), attribute.getName());
					throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
				}
			}
			
			Cases beforeCase = null;
			if (caseData.getBeforeId() != null) {
				beforeCase = getRepository().getStudyCase(study, caseData.getBeforeId());
			}
			
			final PrincipalCollection principals = currentUser.getPrincipals();
			final String user = principals.getPrimaryPrincipal().toString();
			final Cases newCase = getRepository().newStudyCase(study, user, beforeCase);
			if (newCase == null) {
				throw new RuntimeException("Error creating new case");
			}
			
			// Refine the query to select the new case alone.
			query = getRepository().addStudyCaseSelector(query, newCase.getId());
			
			// And now we should write any attributes we have into the new
			// case. This should probably be done as a single operation to the
			// repository.
			attributes = caseData.getEntity();
			fieldIterator = attributes.fields();
			while(fieldIterator.hasNext()) {
				Map.Entry<String,JsonNode> field = fieldIterator.next();
				ObjectNode values = jsonNodeFactory.objectNode();
				values.replace(field.getKey(), field.getValue());
				getRepository().setQueryAttributes(query, user, values);
			}
			
			RequestAttributes.setRequestEntity(getRequest(), newCase);

    	} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		};
		
		// Now we should locate the new entity and return it using the same response
		// type. This is a little problematic. 
    	
		final EntityResponse response = new EntityResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityResponse>(response);
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
