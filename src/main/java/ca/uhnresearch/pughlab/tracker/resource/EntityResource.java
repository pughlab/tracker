package ca.uhnresearch.pughlab.tracker.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.EntityResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

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
		
		Subject currentUser = SecurityUtils.getSubject();
    	
    	logger.debug("Called getResource() in EntityResource");

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	View view = RequestAttributes.getRequestView(getRequest());
    	Cases caseValue = RequestAttributes.getRequestEntity(getRequest());
    	
    	// Security is now an issue here. We need to check read permission for the 
    	// view attributes. 
    	
    	List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
    	List<Attributes> readable = new ArrayList<Attributes>();
    	for(ViewAttributes va : attributes) {
    		if (currentUser.isPermitted(study.getName() + ":attribute:read:" + va.getName())) {
    			readable.add(va);
    		}
    	}

    	ObjectNode caseData = getRepository().getCaseData(study, view, readable, caseValue);
    	
    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setEntity(caseData);
	}
}
