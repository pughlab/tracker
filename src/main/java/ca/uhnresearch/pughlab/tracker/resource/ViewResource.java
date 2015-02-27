package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.codehaus.jackson.JsonNode;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
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
    	CaseQuery query = new CaseQuery();
    	
    	List<Attributes> attributes = repository.getViewAttributes(study, view);
    	List<JsonNode> records = repository.getData(study, view, attributes, query);

    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	ViewResponseDTO response = new ViewResponseDTO(url, study, view);
    	
    	try {
    		for(Attributes a : attributes) {
    			response.getAttributes().add(new AttributeDTO(a));
    		}
    	} catch (Exception e) {
    		throw new WebServiceException(e);
    	}
    	
    	response.setRecords(records);
    	
    	// And render back
        return new JacksonRepresentation<ViewResponseDTO>(response);
    }
}