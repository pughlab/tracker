package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.AttributeDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributesResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;

public class ViewAttributesResource extends StudyRepositoryResource<ViewAttributesResponseDTO> {
	
    @Get("json")
    public Representation getResource()  {
    	ViewAttributesResponseDTO response = new ViewAttributesResponseDTO();
    	buildResponseDTO(response);
        return new JacksonRepresentation<ViewAttributesResponseDTO>(response);
    }

	@Override
	public void buildResponseDTO(ViewAttributesResponseDTO dto) {
		super.buildResponseDTO(dto);
		    	
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	List<Attributes> attributes = getRepository().getViewAttributes(study, view);
    	getRequest().getAttributes().put("attributes", attributes);

    	assert study != null;
    	assert view != null;
    	assert attributes != null;
    	
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));

    	for(Attributes a : attributes) {
    		dto.getAttributes().add(new AttributeDTO(a));
		}

    	dto.getPermissions().setReadAllowed((Boolean) getRequest().getAttributes().get("viewReadAllowed")); 
    	dto.getPermissions().setWriteAllowed((Boolean) getRequest().getAttributes().get("viewWriteAllowed")); 
    	dto.getPermissions().setDownloadAllowed((Boolean) getRequest().getAttributes().get("viewDownloadAllowed")); 
	}
}
