package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ca.uhnresearch.pughlab.tracker.dto.StudyAboutResponse;

public class StudyAboutResource extends StudyRepositoryResource<StudyAboutResponse> {
	
    @Get("json")
    public Representation getResource()  {
    	StudyAboutResponse response = RequestAttributes.getRequestStudyAbout(getRequest());
    	buildResponseDTO(response);
       	return new JacksonRepresentation<StudyAboutResponse>(response);
    }
}
