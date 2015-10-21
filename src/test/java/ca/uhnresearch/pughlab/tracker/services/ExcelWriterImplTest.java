package ca.uhnresearch.pughlab.tracker.services;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.resource.ViewDataResource;
import ca.uhnresearch.pughlab.tracker.services.impl.ExcelWriterImpl;

public class ExcelWriterImplTest {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ViewDataResource viewResource;
	private ViewDataResponse dto;
	private StudyRepository repository = new MockStudyRepository();
	
	private Writer excelWriter;

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
		
		StudyCaseQuery query = repository.newStudyCaseQuery(study);
		query = repository.addViewCaseMatcher(query, view);
		
    	List<ObjectNode> records = repository.getCaseData(query, view);
    	dto.setRecords(records);
    	
    	dto.getCounts().setTotal(repository.getRecordCount(study, view));
    	
    	excelWriter = new ExcelWriterImpl();
	}
	
	@Test
	public void excelTest() {
    	assertNotNull(dto);
		
    	excelWriter.setDocumentBuilderFactory(DocumentBuilderFactory.newInstance());
    	Document result = excelWriter.getXMLDocument(dto);
		assertNotNull(result);
		
		String xml = getStringFromDocument(result);
		assertNotNull(xml);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void excelTestBadDocumentBuilder() throws ParserConfigurationException {
    	assertNotNull(dto);
    	
    	DocumentBuilderFactory badBuilder = createMock(DocumentBuilderFactory.class);
    	badBuilder.setNamespaceAware(true);
    	expectLastCall();
    	expect(badBuilder.newDocumentBuilder()).andThrow(new ParserConfigurationException("Bad parser"));
    	replay(badBuilder);
    	
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Failed to generate XML parser"));
		
    	excelWriter.setDocumentBuilderFactory(badBuilder);
    	excelWriter.getXMLDocument(dto);
	}
	
	
	@Test
	public void excelTestBadAttributes() throws ParserConfigurationException {
    	assertNotNull(dto);
    	
    	excelWriter.setDocumentBuilderFactory(DocumentBuilderFactory.newInstance());
    	
    	ViewDataResponse alternate = new ViewDataResponse();
    	alternate.setUser(dto.getUser());
    	alternate.setStudy(dto.getStudy());
    	alternate.setServiceUrl(dto.getServiceUrl());
    	alternate.setRecords(dto.getRecords());
    	
    	List<ViewAttributes> attributes = dto.getAttributes();
    	ViewAttributes first = new ViewAttributes();
    	first.setId(attributes.get(0).getId());
    	first.setName(attributes.get(0).getName());
    	first.setType("xxx");
    	first.setLabel(attributes.get(0).getLabel());
    	
    	List<ViewAttributes> newAttributes = new ArrayList<ViewAttributes>();
    	newAttributes.addAll(attributes);
    	newAttributes.remove(0);
    	newAttributes.add(0,  first);
    	alternate.setAttributes(newAttributes);

		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Invalid type: xxx"));
		
    	excelWriter.getXMLDocument(alternate);
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
