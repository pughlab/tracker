package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;
import ca.uhnresearch.pughlab.tracker.dto.StudyDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDTO;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponseDTO;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponseDTO> {

	@Get("json")
    public Representation getResource()  {
		ViewDataResponseDTO response = new ViewDataResponseDTO();
		buildResponseDTO(response);
        return new JacksonRepresentation<ViewDataResponseDTO>(response);
    }
	
	@Override
	public void buildResponseDTO(ViewDataResponseDTO dto) {
		super.buildResponseDTO(dto);
		
    	CaseQuery query = (CaseQuery) getRequest().getAttributes().get("query");
    	assert query != null;

    	Studies study = (Studies) getRequest().getAttributes().get("study");
    	Views view = (Views) getRequest().getAttributes().get("view");
    	dto.setStudy(new StudyDTO(study));
    	dto.setView(new ViewDTO(view));
    	
		@SuppressWarnings("unchecked")
		List<Attributes> attributes = (List<Attributes>) getRequest().getAttributes().get("attributes");

    	List<JsonNode> records = getRepository().getData(study, view, attributes, query);
    	dto.setRecords(records);
    	dto.getCounts().setTotal(getRepository().getRecordCount(study, view));

	}
}
