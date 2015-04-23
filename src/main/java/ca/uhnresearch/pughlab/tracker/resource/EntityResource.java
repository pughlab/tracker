package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;

public class EntityResource extends StudyRepositoryResource<EntityResponseDTO> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Get("json")
    public Representation getResource() {
    	EntityResponseDTO response = new EntityResponseDTO();
    	buildResponseDTO(response);
        return new JacksonRepresentation<EntityResponseDTO>(response);
    }

	@Override
	public void buildResponseDTO(EntityResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	logger.info("Called getResource() in EntityResource");

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	Cases caseValue = (Cases) getRequest().getAttributes().get("entity");
    	
    	JsonNode caseData = getRepository().getCaseData(study, view, caseValue);
    	
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));
    	dto.setEntity(caseData);
	}
}
