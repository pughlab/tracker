package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributesResponse;

public class ViewAttributesResource extends StudyRepositoryResource<ViewAttributesResponse> {
	
    @Get("json")
    public Representation getResource()  {
    	ViewAttributesResponse response = new ViewAttributesResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<ViewAttributesResponse>(response);
    }

	@Override
	public void buildResponseDTO(ViewAttributesResponse dto) {
		super.buildResponseDTO(dto);
		    	
    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");
    	List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);

    	assert study != null;
    	assert view != null;
    	assert attributes != null;
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setAttributes(attributes);

    	dto.getPermissions().setReadAllowed((Boolean) getRequest().getAttributes().get("viewReadAllowed")); 
    	dto.getPermissions().setWriteAllowed((Boolean) getRequest().getAttributes().get("viewWriteAllowed")); 
    	dto.getPermissions().setDownloadAllowed((Boolean) getRequest().getAttributes().get("viewDownloadAllowed")); 
	}
}
