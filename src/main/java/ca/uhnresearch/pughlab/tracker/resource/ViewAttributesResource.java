package ca.uhnresearch.pughlab.tracker.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

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
		    	
		Subject currentUser = SecurityUtils.getSubject();

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	View view = RequestAttributes.getRequestView(getRequest());
    	List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
    	
    	// Security should also apply here, and this requires a wee bit of fiddling
    	List<ViewAttributes> readable = new ArrayList<ViewAttributes>();
    	for(ViewAttributes va : attributes) {
    		if (currentUser.isPermitted(study.getName() + ":attribute:read:" + va.getName())) {
    			readable.add(va);
    		}
    	}

    	dto.setStudy(study);
    	dto.setView(view);
    	dto.setAttributes(readable);
    	
    	Boolean createPermitted = currentUser.isPermitted(study.getName() + ":create");
    	Boolean deletePermitted = currentUser.isPermitted(study.getName() + ":delete");
    	dto.getPermissions().setCreate(createPermitted); 
    	dto.getPermissions().setDelete(deletePermitted); 

    	Boolean readPermitted = currentUser.isPermitted(study.getName() + ":read:" + view.getName());
    	Boolean writePermitted = currentUser.isPermitted(study.getName() + ":write:" + view.getName());
    	Boolean downloadPermitted = currentUser.isPermitted(study.getName() + ":download:" + view.getName());
    	readPermitted = readPermitted || writePermitted;
    	
    	dto.getPermissions().setRead(readPermitted); 
    	dto.getPermissions().setWrite(writePermitted); 
    	dto.getPermissions().setDownload(downloadPermitted); 
	}
}
