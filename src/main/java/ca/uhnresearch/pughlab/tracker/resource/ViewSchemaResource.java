package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewSchemaResponseDTO;

public class ViewSchemaResource extends StudyRepositoryResource<ViewSchemaResponseDTO> {
	
    @Get("json")
    public Representation getResource()  {
    	
    	ViewSchemaResponseDTO response = new ViewSchemaResponseDTO();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<ViewSchemaResponseDTO>(response);    	
    }

	@Override
	public void buildResponseDTO(ViewSchemaResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");

    	Subject currentUser = SecurityUtils.getSubject();

    	// Only administrators can get the schema
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
    	
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));
    	
    	List<Attributes> attributes = getRepository().getViewAttributes(study, view);
    	for (Attributes a : attributes) {
    		dto.getAttributes().add(new AttributeDTO(a));
    	}
	}
}
