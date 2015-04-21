package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

public class EntityResource extends StudyRepositoryResource {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Get("json")
    public Representation getResource() {
    	
    	logger.info("Called getResource() in EntityResource");

    	Subject currentUser = SecurityUtils.getSubject();

    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	
    	JsonNode caseData = getRepository().getCaseData(study, view, caseValue);

    	EntityResponseDTO response = new EntityResponseDTO(url, user, study, view, caseData);
    	
        return new JacksonRepresentation<EntityResponseDTO>(response);

    }
}
