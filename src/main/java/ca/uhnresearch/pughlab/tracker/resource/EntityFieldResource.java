package ca.uhnresearch.pughlab.tracker.resource;

import java.io.IOException;
import java.net.URL;

import org.apache.shiro.SecurityUtils;
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

import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.EntityValueResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

import com.fasterxml.jackson.databind.JsonNode;

public class EntityFieldResource extends StudyRepositoryResource {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JacksonConverter converter = new JacksonConverter();
	
    @Put("json")
    public Representation putResource(Representation input) {
    	logger.info("Called putResource() in EntityFieldResource", input);
    	
    	JsonNode data;
    	
    	try {
			data = converter.toObject(input, JsonNode.class, this);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

    	Subject currentUser = SecurityUtils.getSubject();

    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	String attribute = (String) getRequest().getAttributes().get("entityField");
    	
    	// Write the value
    	getRepository().setCaseAttributeValue(study, view, caseValue, attribute, data);
    	
    	// Return the response, which is the same as a GET response
    	JsonNode val = getRepository().getCaseAttributeValue(study, view, caseValue, attribute);
    	EntityValueResponseDTO response = new EntityValueResponseDTO(url, user, study, view, val);

        return new JacksonRepresentation<EntityValueResponseDTO>(response);
    }


    @Get("json")
    public Representation getResource()  {
    	logger.info("Called getResource() in EntityFieldResource");

    	Subject currentUser = SecurityUtils.getSubject();

    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	String attribute = (String) getRequest().getAttributes().get("entityField");
    	
    	// Get the value and build an appropriate response
    	JsonNode val = getRepository().getCaseAttributeValue(study, view, caseValue, attribute);
    	EntityValueResponseDTO response = new EntityValueResponseDTO(url, user, study, view, val);

        return new JacksonRepresentation<EntityValueResponseDTO>(response);
    }
}
