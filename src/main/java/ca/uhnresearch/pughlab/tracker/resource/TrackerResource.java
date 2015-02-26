package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyListResponseDTO;

public class TrackerResource extends ServerResource {
		
	private StudyRepository repository;

	private final Logger logger = LoggerFactory.getLogger(TrackerResource.class);

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }

    @Get("json")
    public Representation getResource()  {
    	logger.info("Called getResource");
    	
    	// Query the database for studies
    	List<Studies> studyList = repository.getAllStudies();
    	
    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	StudyListResponseDTO response = new StudyListResponseDTO(url);
    	for(Studies s : studyList) {
    		response.getStudies().add(new StudyDTO(s));
    	}
    	
    	// And render back
       	return new JacksonRepresentation<StudyListResponseDTO>(response);
    }
}
