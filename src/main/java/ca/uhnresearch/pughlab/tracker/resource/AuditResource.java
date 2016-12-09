package ca.uhnresearch.pughlab.tracker.resource;

import java.text.MessageFormat;
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
    	final AuditLogResponse response = new AuditLogResponse();
    	buildResponseDTO(response);
    	return new JacksonRepresentation<AuditLogResponse>(response);
    }

	@Override
	public void buildResponseDTO(AuditLogResponse dto) {
		super.buildResponseDTO(dto);
		
    	// Query the database for studies
		final Study study = RequestAttributes.getRequestStudy(getRequest());
    	
		final Subject currentUser = SecurityUtils.getSubject();
		final boolean adminUser = currentUser.isPermitted(study.getName() + ":admin");
    	
    	// Only administrators can get the audit log
    	if (! adminUser) {
    		final String message = MessageFormat.format("No administrator access to study: {0}", study.getName());
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
    	}
 
    	// Get the data from the repository
    	final CasePager query = RequestAttributes.getRequestCasePager(getRequest());
    	final List<JsonNode> auditData = getRepository().getAuditData(study, query);

    	// Build the response
    	dto.setStudy(study);
    	dto.setLog(auditData);
	}
}
