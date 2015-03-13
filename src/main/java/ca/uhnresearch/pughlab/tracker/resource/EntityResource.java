package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

public class EntityResource extends ServerResource {

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
    	JsonNode entity = (JsonNode) getRequest().getAttributes().get("entity");

    	EntityResponseDTO response = new EntityResponseDTO(url, user, study, view, entity);
    	
        return new JacksonRepresentation<EntityResponseDTO>(response);

    }
}
