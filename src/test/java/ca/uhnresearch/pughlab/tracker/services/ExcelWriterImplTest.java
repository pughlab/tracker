package ca.uhnresearch.pughlab.tracker.services;

import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.resource.ViewDataResource;
import ca.uhnresearch.pughlab.tracker.services.impl.ExcelWriterImpl;

public class ExcelWriterImplTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ViewDataResource viewResource;
	private ViewDataResponse dto;
	private StudyRepository repository = new MockStudyRepository();
	
	private ExcelWriter excelWriter;

	@Before
	public void initialize() {
		
		viewResource = new ViewDataResource();
		viewResource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		viewResource.setRequest(request);
		
		dto = new ViewDataResponse();
		
        Study study = repository.getStudy("DEMO");		
		View view = repository.getStudyView(study, "complete");
		List<ViewAttributes> attributes = repository.getViewAttributes(study, view);
		
    	dto.setStudy(study);
    	dto.setView(view);
		dto.setAttributes(attributes);
		
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(100000);
    	List<JsonNode> records = repository.getData(study, view, attributes, query);
    	dto.setRecords(records);
    	
    	dto.getCounts().setTotal(repository.getRecordCount(study, view));
    	
    	excelWriter = new ExcelWriterImpl();
	}
	
	@Test
	public void excelTest() {
		assertNotNull(dto);
		
		Document result = excelWriter.getExcelDocument(dto);
		assertNotNull(result);
		
		String xml = getStringFromDocument(result);
		assertNotNull(xml);
	}
	
	private String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			writer.flush();
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
