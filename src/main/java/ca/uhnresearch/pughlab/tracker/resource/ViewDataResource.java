package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.Get;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.ExcelWriter;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponse> {
	
	private ExcelWriter excelWriter;

	@Get("json")
    public Representation getResource()  {
		ViewDataResponse response = new ViewDataResponse();
		buildResponseDTO(response);
        return new JacksonRepresentation<ViewDataResponse>(response);
    }
	
	@Get("xml")
    public Representation getXmlResource()  {
		ViewDataResponse response = new ViewDataResponse();
		buildResponseDTO(response);
		Document xmlDocument = excelWriter.getExcelDocument(response);
		Representation result = new DomRepresentation(MediaType.APPLICATION_EXCEL, xmlDocument);
		Disposition disposition = new Disposition();
		disposition.setFilename("report.xls");
		disposition.setType(Disposition.TYPE_ATTACHMENT);
		result.setDisposition(disposition);
		return result;
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
    	
		List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
		dto.setAttributes(attributes);
		
    	List<JsonNode> records = getRepository().getData(study, view, attributes, query);
    	dto.setRecords(records);
    	dto.getCounts().setTotal(getRepository().getRecordCount(study, view));

	}

	/**
	 * @return the excelWriter
	 */
	public ExcelWriter getExcelWriter() {
		return excelWriter;
	}

	/**
	 * @param excelWriter the excelWriter to set
	 */
	public void setExcelWriter(ExcelWriter excelWriter) {
		this.excelWriter = excelWriter;
	}
}
