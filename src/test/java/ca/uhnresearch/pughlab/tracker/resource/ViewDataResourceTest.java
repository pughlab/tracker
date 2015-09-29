package ca.uhnresearch.pughlab.tracker.resource;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;
import ca.uhnresearch.pughlab.tracker.services.Writer;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ViewDataResourceTest extends AbstractShiroTest {

	private ViewDataResource resource;
	private StudyRepository repository = new MockStudyRepository();

	@Before
	public void initialize() {
		
		resource = new ViewDataResource();
		resource.setRepository(repository);
		Request request = new Request(Method.GET, "http://localhost:9998/services/studies");
		Reference rootReference = new Reference("http://localhost:9998/services");
		request.setRootRef(rootReference);
		resource.setRequest(request);
	}
	
	@After
	public void tearDownSubject() {
        clearSubject();
    }
	
	/**
	 * Checks that the excelWriter accessor can be used to set and get the 
	 * writer service; this is used from dependency injection.
	 */
	@Test
	public void resourceAccessors() {
		
        Writer oldWriter = resource.getExcelWriter();
        
        Writer mockWriter = createMock(Writer.class);
        resource.setExcelWriter(mockWriter);
        
        assertEquals(mockWriter, resource.getExcelWriter());
        resource.setExcelWriter(oldWriter);
	}

	@Test
	public void resourceTest() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted(anyObject(String.class))).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
        RequestAttributes.setRequestCaseQuery(resource.getRequest(), query);

		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		assertEquals( "http://localhost:9998/services", data.get("serviceUrl").getAsString());
		
		JsonObject view = data.get("view").getAsJsonObject();
		assertEquals( "complete", view.get("name").getAsString() );
		
		assertFalse(data.get("attributes").isJsonNull());
		JsonArray attributes = data.get("attributes").getAsJsonArray();
		assertNotNull(attributes);
		assertEquals(5, attributes.size());
		
		assertTrue( data.get("records").isJsonArray() );
	}
	
	@Test
	public void resourceXmlTest() throws IOException, ParserConfigurationException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted(anyObject(String.class))).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
        RequestAttributes.setRequestCaseQuery(resource.getRequest(), query);
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);			
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document doc = builder.newDocument();

        Writer mockWriter = createMock(Writer.class);
        expect(mockWriter.getXMLDocument(anyObject(ViewDataResponse.class))).andStubReturn(doc);
        replay(mockWriter);
        resource.setExcelWriter(mockWriter);

        Representation result = resource.getXmlResource();
		assertEquals("application/vnd.ms-excel", result.getMediaType().toString());
		String data = result.getText();
		
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", data);
	}
	
	@Test
	public void resourceTestPermissionsFull() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted(anyObject(String.class))).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
        RequestAttributes.setRequestCaseQuery(resource.getRequest(), query);

		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		JsonObject permissions = data.get("permissions").getAsJsonObject();
		
		assertEquals(false, permissions.get("adminAllowed").getAsBoolean());
		assertEquals(true, permissions.get("readAllowed").getAsBoolean());
		assertEquals(true, permissions.get("writeAllowed").getAsBoolean());
		assertEquals(true, permissions.get("downloadAllowed").getAsBoolean());
	}

	@Test
	public void resourceTestPermissionsImplied() throws IOException {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andStubReturn(false);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "test"));
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:dateEntered")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:patientId")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:mrn")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:consentDate")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:attribute:read:specimenAvailable")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:read:complete")).andStubReturn(false);
        expect(subjectUnderTest.isPermitted("DEMO:write:complete")).andStubReturn(true);
        expect(subjectUnderTest.isPermitted("DEMO:download:complete")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        Study testStudy = repository.getStudy("DEMO");		
		View testView = repository.getStudyView(testStudy, "complete");
		RequestAttributes.setRequestStudy(resource.getRequest(), testStudy);
		RequestAttributes.setRequestView(resource.getRequest(), testView);
		CaseQuery query = new CaseQuery();
		query.setLimit(5);
		query.setOffset(0);
        RequestAttributes.setRequestCaseQuery(resource.getRequest(), query);

		Representation result = resource.getResource();
		assertEquals("application/json", result.getMediaType().toString());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(result.getText(), JsonObject.class);
		
		JsonObject permissions = data.get("permissions").getAsJsonObject();
		
		assertEquals(false, permissions.get("adminAllowed").getAsBoolean());
		assertEquals(true, permissions.get("readAllowed").getAsBoolean());
		assertEquals(true, permissions.get("writeAllowed").getAsBoolean());
		assertEquals(false, permissions.get("downloadAllowed").getAsBoolean());
	}
}
