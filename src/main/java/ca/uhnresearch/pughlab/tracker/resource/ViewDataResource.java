package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponse> {

	@Get("json")
    public Representation getResource()  {
		ViewDataResponse response = new ViewDataResponse();
		buildResponseDTO(response);
        return new JacksonRepresentation<ViewDataResponse>(response);
    }
	
	@Override
	public void buildResponseDTO(ViewDataResponse dto) {
		super.buildResponseDTO(dto);
		
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	assert query != null;

    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");
    	dto.setStudy(study);
    	dto.setView(view);
    	
		@SuppressWarnings("unchecked")
		List<ViewAttributes> attributes = (List<ViewAttributes>) getRequest().getAttributes().get("attributes");

    	List<JsonNode> records = getRepository().getData(study, view, attributes, query);
    	dto.setRecords(records);
    	dto.getCounts().setTotal(getRepository().getRecordCount(study, view));

	}
}
