package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;

public class AuditResource extends StudyRepositoryResource<AuditLogResponseDTO> {

    @Get("json")
    public Representation getResource()  {
    	AuditLogResponseDTO response = new AuditLogResponseDTO();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<AuditLogResponseDTO>(response);
    }

	@Override
	public void buildResponseDTO(AuditLogResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	
    	// Only administrators can get the audit log
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
 
    	// Get the data from the repository
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	List<JsonNode> auditData = getRepository().getAuditData(study, query);

    	// Build the response
    	dto.setStudy(new StudyDTO(study));
    	dto.setLog(auditData);
	}
}
