package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponseDTO;

public class ViewDataResource extends StudyRepositoryResource {

    @Get("json")
    public Representation getResource()  {

    	Subject currentUser = SecurityUtils.getSubject();

    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	
    	assert study != null;
    	assert view != null;
    	assert query != null;
    	
    	List<Attributes> attributes = getRepository().getViewAttributes(study, view);
    	List<JsonNode> records = getRepository().getData(study, view, attributes, query);

    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);
    	ViewDataResponseDTO response = new ViewDataResponseDTO(url, user, study, view);
    	
		for(Attributes a : attributes) {
			response.getAttributes().add(new AttributeDTO(a));
		}
    	
    	response.setRecords(records);
    	response.getCounts().setTotal(getRepository().getRecordCount(study, view));
    	
    	// And render back
        return new JacksonRepresentation<ViewDataResponseDTO>(response);
    }
}
