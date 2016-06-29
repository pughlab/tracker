package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.EntityValueResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityFieldResource extends StudyRepositoryResource<EntityValueResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JacksonConverter converter = new JacksonConverter();
	
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    @Put("json")
    public Representation putResource(Representation input) {
    	
    	logger.debug("Called putResource() in EntityFieldResource", input);
    	final Subject currentUser = SecurityUtils.getSubject();

    	final Study study = RequestAttributes.getRequestStudy(getRequest());
    	final View view = RequestAttributes.getRequestView(getRequest());
    	final Attributes attribute = RequestAttributes.getRequestAttribute(getRequest());
    	final StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(getRequest());
    	
    	if (study == null || view == null || attribute == null) {
    		throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Missing study, view, or attribute");
    	}
    	
    	if (! currentUser.isPermitted(study.getName() + ":write:" + view.getName()) ||
    		! currentUser.isPermitted(study.getName() + ":attribute:write:" + attribute.getName())) {
			String message = MessageFormat.format("No write access to attribute: {0}", attribute.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
    	
    	JsonNode data;
    	
    	try {
			data = converter.toObject(input, JsonNode.class, this);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}

    	// Write the value, handling exceptions we might get, and converting them to
    	// appropriate server responses.
    	
    	final PrincipalCollection principals = currentUser.getPrincipals();
    	final String user = principals.getPrimaryPrincipal().toString();
    	
    	try {
    		final ObjectNode values = jsonNodeFactory.objectNode();
    		values.replace(attribute.getName(), data.get("value"));
			getRepository().setQueryAttributes(query, user, values);
		} catch (InvalidValueException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getLocalizedMessage());
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal Server Error: " + e.getLocalizedMessage());
		}
    	
    	return getResource();
    }


    @Get("json")
    public Representation getResource()  {
    	final EntityValueResponse response = new EntityValueResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityValueResponse>(response);
    }


	@Override
	public void buildResponseDTO(EntityValueResponse dto) {
		super.buildResponseDTO(dto);

		final Subject currentUser = SecurityUtils.getSubject();

		final Study study = RequestAttributes.getRequestStudy(getRequest());
		final View view = RequestAttributes.getRequestView(getRequest());
		final Attributes attribute = RequestAttributes.getRequestAttribute(getRequest());
		final StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(getRequest());

    	// Add support for the permissions. It's very simple here
    	if (! currentUser.isPermitted(study.getName() + ":attribute:read:" + attribute.getName())) {
    		final String message = MessageFormat.format("No read access to attribute: {0}", attribute.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}

    	final List<ObjectNode> cases = getRepository().getCaseData(query, view);
    	if (cases.isEmpty()) {
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    	}
    	
    	final ObjectNode result = cases.get(0);
    	
    	// Get the value and build an appropriate response
    	final JsonNode val = result.get(attribute.getName());
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setValue(val);
	}
}
