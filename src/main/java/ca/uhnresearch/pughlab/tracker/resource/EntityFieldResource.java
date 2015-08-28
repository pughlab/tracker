package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
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
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.EntityValueResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityFieldResource extends StudyRepositoryResource<EntityValueResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JacksonConverter converter = new JacksonConverter();
	
    @Put("json")
    public Representation putResource(Representation input) {
    	
    	logger.debug("Called putResource() in EntityFieldResource", input);
    	Subject currentUser = SecurityUtils.getSubject();

    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");
    	
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	
    	String attribute = (String) getRequest().getAttributes().get("entityField");
    	
    	boolean writeUser = currentUser.isPermitted(study.getName() + ":write");
    	if (! writeUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	JsonNode data;
    	
    	try {
			data = converter.toObject(input, JsonNode.class, this);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

    	// Write the value, handling exceptions we might get, and converting them to
    	// appropriate server responses.
    	
		PrincipalCollection principals = currentUser.getPrincipals();
		String user = principals.getPrimaryPrincipal().toString();
    	
    	try {
			getRepository().setCaseAttributeValue(study, view, caseValue, attribute, user, data.get("value"));
		} catch (InvalidValueException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (RepositoryException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
    	
    	return getResource();
    }


    @Get("json")
    public Representation getResource()  {
    	EntityValueResponse response = new EntityValueResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityValueResponse>(response);
    }


	@Override
	public void buildResponseDTO(EntityValueResponse dto) {
		super.buildResponseDTO(dto);

    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");
    	StudyCaseQuery query = (StudyCaseQuery) getRequest().getAttributes().get("query");

    	List<ObjectNode> cases = getRepository().getCaseData(query, view);
    	if (cases.isEmpty()) {
    		throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    	}
    	
    	ObjectNode result = cases.get(0);
    	
    	String attribute = (String) getRequest().getAttributes().get("entityField");
    	
    	// Get the value and build an appropriate response
    	JsonNode val = result.get(attribute);
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setValue(val);
	}
}
