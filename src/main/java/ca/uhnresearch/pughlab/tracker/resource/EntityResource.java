package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

public class EntityResource extends StudyRepositoryResource<EntityResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Get("json")
    public Representation getResource() {
    	EntityResponse response = new EntityResponse();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityResponse>(response);
    }

	@Override
	public void buildResponseDTO(EntityResponse dto) {
		super.buildResponseDTO(dto);
		
    	logger.debug("Called getResource() in EntityResource");

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	View view = RequestAttributes.getRequestView(getRequest());
    	Cases caseValue = RequestAttributes.getRequestEntity(getRequest());
    	
    	ObjectNode caseData = getRepository().getCaseData(study, view, caseValue);
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setEntity(caseData);
	}
}
