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

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogResponse;
import ca.uhnresearch.pughlab.tracker.dto.Study;

public class AuditResource extends AuditLogRepositoryResource<AuditLogResponse> {

    @Get("json")
    public Representation getResource()  {
    	AuditLogResponse response = new AuditLogResponse();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<AuditLogResponse>(response);
    }

	@Override
	public void buildResponseDTO(AuditLogResponse dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
    	Study study = (Study) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	
    	// Only administrators can get the audit log
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
 
    	// Get the data from the repository
    	CasePager query = (CasePager) getRequest().getAttributes().get("query");
    	List<JsonNode> auditData = getRepository().getAuditData(study, query);

    	// Build the response
    	dto.setStudy(study);
    	dto.setLog(auditData);
	}
}
