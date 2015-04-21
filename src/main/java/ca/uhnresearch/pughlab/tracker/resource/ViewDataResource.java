package ca.uhnresearch.pughlab.tracker.resource;

import java.net.URL;
import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.UserDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributesResponseDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponseDTO;

public class ViewDataResource extends ViewAttributesResource {

	protected ViewAttributesResponseDTO newViewResponse(URL url, UserDTO user, Studies study, Views view) {
		return new ViewDataResponseDTO(url, user, study, view);
	}

	@Get("json")
    public Representation getResource()  {

		ViewDataResponseDTO response = (ViewDataResponseDTO) getViewResponse();

    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	assert query != null;
    	
    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
		@SuppressWarnings("unchecked")
		List<Attributes> attributes = (List<Attributes>) getRequest().getAttributes().get("attributes");

    	List<JsonNode> records = getRepository().getData(study, view, attributes, query);
    	response.setRecords(records);
    	response.getCounts().setTotal(getRepository().getRecordCount(study, view));
    	
    	// And render back
        return new JacksonRepresentation<ViewDataResponseDTO>(response);
    }
}
