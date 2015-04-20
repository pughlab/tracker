package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributesResponseDTO;

public class ViewAttributesResource extends StudyRepositoryResource {

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

    	// Now translate into DTOs
    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);
    	ViewAttributesResponseDTO response = new ViewAttributesResponseDTO(url, user, study, view);
    	
		for(Attributes a : attributes) {
			response.getAttributes().add(new AttributeDTO(a));
		}
		
		response.getPermissions().setReadAllowed((Boolean) getRequest().getAttributes().get("viewReadAllowed")); 
		response.getPermissions().setWriteAllowed((Boolean) getRequest().getAttributes().get("viewWriteAllowed")); 
		response.getPermissions().setDownloadAllowed((Boolean) getRequest().getAttributes().get("viewDownloadAllowed")); 
    	    	
    	// And render back
        return new JacksonRepresentation<ViewAttributesResponseDTO>(response);
    }
}
