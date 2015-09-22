package ca.uhnresearch.pughlab.tracker.resource;

import java.util.List;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.Get;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponse> {
	
	private Writer excelWriter;

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
		Document xmlDocument = excelWriter.getXMLDocument(response);
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

    	Study study = (Study) getRequest().getAttributes().get("study");
    	View view = (View) getRequest().getAttributes().get("view");
    	dto.setStudy(study);
    	dto.setView(view);
    	
		List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
		dto.setAttributes(attributes);
		
    	List<ObjectNode> records = getRepository().getData(study, view, attributes, query);
    	dto.setRecords(records);
    	dto.getCounts().setTotal(getRepository().getRecordCount(study, view));

	}

	/**
	 * @return the excelWriter
	 */
	public Writer getExcelWriter() {
		return excelWriter;
	}

	/**
	 * @param excelWriter the excelWriter to set
	 */
	public void setExcelWriter(Writer excelWriter) {
		this.excelWriter = excelWriter;
	}
}
