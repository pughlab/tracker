package ca.uhnresearch.pughlab.tracker.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
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

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;

public class ViewDataResource extends StudyRepositoryResource<ViewDataResponse> {
	
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(ViewDataResource.class);
	
	private Map<String, Writer> writers = new HashMap<String, Writer>();

	private Representation getFormatted(Writer writer, String filename) {
		logger.info("Writing data to {}", filename);
		ViewDataResponse response = new ViewDataResponse();
		buildResponseDTO(response);
		Document xmlDocument = writer.getXMLDocument(response);
		Representation result = new DomRepresentation(MediaType.APPLICATION_EXCEL, xmlDocument);
		Disposition disposition = new Disposition();
		disposition.setFilename(filename);
		disposition.setType(Disposition.TYPE_ATTACHMENT);
		result.setDisposition(disposition);
		return result;
	}

	@Get("json")
    public Representation getResource()  {
		ViewDataResponse response = new ViewDataResponse();
		buildResponseDTO(response);
        return new JacksonRepresentation<ViewDataResponse>(response);
    }
	
	@Get("xml")
    public Representation getXmlResource()  {
		return getFormatted(getWriter("xml"), "report.xls");
	}
	
	@Get("html")
    public Representation getHtmlResource()  {
		return getFormatted(getWriter("html"), "report.htm");
	}
	
	@Override
	public void buildResponseDTO(ViewDataResponse dto) {
		super.buildResponseDTO(dto);
		
		Subject currentUser = SecurityUtils.getSubject();

		Request request = getRequest();
    	Study study = RequestAttributes.getRequestStudy(request);
    	View view = RequestAttributes.getRequestView(request);
    	StudyCaseQuery query = RequestAttributes.getRequestCaseQuery(request);
		CasePager pager = RequestAttributes.getRequestCasePager(request);
		ObjectNode filter = RequestAttributes.getRequestFilter(request);

		dto.setStudy(study);
    	dto.setView(view);
    	
    	// Filters aren't persisted, but we reflect it from the request
    	if (filter != null) {
    		dto.setFilter(filter);
    	}
    	
    	query = getRepository().applyPager(query, pager);
    	
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
		
    	List<ObjectNode> records = getRepository().getCaseData(query, view);
    	dto.setRecords(records);
    	dto.getCounts().setTotal(getRepository().getRecordCount(study, view));
    	
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

	/**
	 * @return the writers
	 */
	public Map<String, Writer> getWriters() {
		return writers;
	}

	/**
	 * @param writers the writers to set
	 */
	public void setWriters(Map<String, Writer> writers) {
		this.writers = writers;
	}
	
	public Writer getWriter(String type) {
		if (! writers.containsKey(type)) {
			throw new RuntimeException("Can't get writer for type: " + type);
		}
		return writers.get(type);
	}
}
