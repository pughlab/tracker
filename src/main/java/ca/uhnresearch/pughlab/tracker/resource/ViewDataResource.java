package ca.uhnresearch.pughlab.tracker.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponse> {
	
	private final Logger logger = LoggerFactory.getLogger(ViewDataResource.class);

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
		
		Subject currentUser = SecurityUtils.getSubject();

    	CaseQuery query = RequestAttributes.getRequestCaseQuery(getRequest());

    	Study study = RequestAttributes.getRequestStudy(getRequest());
    	View view = RequestAttributes.getRequestView(getRequest());
    	dto.setStudy(study);
    	dto.setView(view);
    	
		List<ViewAttributes> attributes = getRepository().getViewAttributes(study, view);
		
    	// Security is now an issue here. We need to check read permission for the 
    	// view attributes. 
    	
    	List<ViewAttributes> readable = new ArrayList<ViewAttributes>();
    	for(ViewAttributes va : attributes) {
    		if (currentUser.isPermitted(study.getName() + ":attribute:read:" + va.getName())) {
    			readable.add(va);
    		}
    	}

		dto.setAttributes(readable);
		
    	List<ObjectNode> records = getRepository().getData(study, view, readable, query);
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
