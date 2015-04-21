package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
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
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;

public class AuditResource extends StudyRepositoryResource {

    @Get("json")
    public Representation getResource()  {

    	// Query the database for studies
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	
    	Subject currentUser = SecurityUtils.getSubject();
    	boolean adminUser = currentUser.isPermitted("study:admin:" + study.getName());
    	
    	// Only administrators can get the audit log
    	if (! adminUser) {
    		throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    	}
 
    	URL url = getRequest().getRootRef().toUrl();
    	UserDTO user = new UserDTO(currentUser);

    	// Get the data from the repository
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	List<JsonNode> auditData = getRepository().getAuditData(study, query);

    	// Build the response
    	AuditLogResponseDTO response = new AuditLogResponseDTO(url, user, study);
    	response.setLog(auditData);
    	
    	// And render back the data
    	return new JacksonRepresentation<AuditLogResponseDTO>(response);
    }
}
