package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
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
    	
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));
	}
}
