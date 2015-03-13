package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.EntityHistoryResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

import com.fasterxml.jackson.databind.JsonNode;

public class EntityFieldHistoryResource extends ServerResource {
	
	private StudyRepository repository;

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

    @Get("json")
    public Representation getResource()  {

    	Subject currentUser = SecurityUtils.getSubject();

    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	String attribute = (String) getRequest().getAttributes().get("entityField");
    	
    	JsonNode history = repository.getCaseAttributeHistory(study, view, caseValue, attribute);
    	EntityHistoryResponseDTO response = new EntityHistoryResponseDTO(url, user, study, view, history);

        return new JacksonRepresentation<EntityHistoryResponseDTO>(response);

    }

}
