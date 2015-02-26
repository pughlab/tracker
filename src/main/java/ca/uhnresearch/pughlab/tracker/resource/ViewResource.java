package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.ViewResponseDTO;

public class ViewResource extends ServerResource {

	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(ViewResource.class);

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

    @Get("json")
    public Representation getResource()  {
    	logger.info("Called getResource");
    	
    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");

    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	ViewResponseDTO response = new ViewResponseDTO(url, study, view);
    	
    	// And render back
       	return new JacksonRepresentation<ViewResponseDTO>(response);
    }

}
