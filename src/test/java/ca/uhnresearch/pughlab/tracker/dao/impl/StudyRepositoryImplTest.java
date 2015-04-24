package ca.uhnresearch.pughlab.tracker.dao.impl;

import static junit.framework.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/applicationContextDatabase.xml" })
public class StudyRepositoryImplTest {

	@Autowired
    private StudyRepository studyRepository;
	
	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testWiring() {
		assertNotNull(studyRepository);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudy() {
		Studies s = studyRepository.getStudy("DEMO");
		assertNotNull(s);
		assertEquals("DEMO", s.getName());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudy() {
		Studies s = studyRepository.getStudy("DEMOX");
		assertNull(s);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudies() {
		List<Studies> list = studyRepository.getAllStudies();
		assertNotNull(list);
		assertEquals(2, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyViews() {
		Studies study = studyRepository.getStudy("DEMO");
		List<Views> list = studyRepository.getStudyViews(study);
		assertNotNull(list);
		assertEquals(3, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyView() {
		Studies study = studyRepository.getStudy("DEMO");
		Views v = studyRepository.getStudyView(study, "complete");
		assertNotNull(v);
		assertEquals("complete", v.getName());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudyView() {
		Studies study = studyRepository.getStudy("DEMO");
		Views v = studyRepository.getStudyView(study, "completed");
		assertNull(v);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyAttributes() {
		Studies study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		assertNotNull(list);
		assertEquals(27, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetViewAttributes() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "complete");
		List<Attributes> list = studyRepository.getViewAttributes(study, view);
		assertNotNull(list);
		assertEquals(27, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSmallerGetViewAttributes() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		List<Attributes> list = studyRepository.getViewAttributes(study, view);
		assertNotNull(list);
		assertEquals(15, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetData() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		List<Attributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);
		List<JsonNode> list = studyRepository.getData(study, view, attributes, query);
		assertNotNull(list);
		assertEquals(10, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoLimit() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		List<Attributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(null);
		List<JsonNode> list = studyRepository.getData(study, view, attributes, query);
		assertNotNull(list);
		assertEquals(20, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoOffset() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		List<Attributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(null);
		query.setLimit(5);
		List<JsonNode> list = studyRepository.getData(study, view, attributes, query);
		assertNotNull(list);
		assertEquals(5, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataOrdered() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		List<Attributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		query.setOrderField("consentDate");
		query.setOrderDirection(CaseQuery.OrderDirection.DESC);
		List<JsonNode> list = studyRepository.getData(study, view, attributes, query);
		assertNotNull(list);
		assertEquals(5, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testRecordCount() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Long count = studyRepository.getRecordCount(study, view);
		assertEquals(20, count.intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCase() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		assertNotNull(caseValue);
		assertEquals(1, caseValue.getId().intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleMissingCase() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 100);
		assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleFromDifferentStudy() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 22);
		assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValues() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseData(study, view, caseValue);
		assertNotNull(data);
		
		String date = data.get("dateEntered").asText();
		assertNotNull(date);
		assertEquals("2014-08-20", date);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValues() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "dateEntered");
		assertNotNull(data);
		assertEquals("2014-08-20", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValuesNotAvailable() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 2);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "trackerDate");
		assertNotNull(data);
		assertTrue(data.isObject());
		assertTrue(data.has("$notAvailable"));
		assertEquals("true", data.get("$notAvailable").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeMissing() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "bloodCollDate");
		assertNull(data);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLog() {
		Studies study = studyRepository.getStudy("DEMO");
		
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		
		assertNotNull(auditEntries);
		assertEquals(0, auditEntries.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDate() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			studyRepository.setCaseAttributeValue(study, view, caseValue, "dateEntered", "stuart", null);
		} catch (RepositoryException e) {
			fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("dateEntered", entry.get("attribute").asText());
		assertEquals("2014-08-20", entry.get("eventArgs").get("old").asText());
		assertTrue(entry.get("eventArgs").get("value").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "dateEntered");
		assertNotNull(data);
		assertTrue(data.isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueString() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode stringValue = jsonNodeFactory.textNode("DEMO-XX");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "patientId", "stuart", stringValue);
		} catch (RepositoryException e) {
			fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("patientId", entry.get("attribute").asText());
		assertEquals("DEMO-01", entry.get("eventArgs").get("old").asText());
		assertEquals("DEMO-XX", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "patientId");
		assertNotNull(data);
		assertFalse(data.isNull());
		assertEquals("DEMO-XX", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOption() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode stringValue = jsonNodeFactory.textNode("St. Michaels");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "sampleAvailable", "stuart", stringValue);
		} catch (RepositoryException e) {
			fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("sampleAvailable", entry.get("attribute").asText());
		assertEquals("LMP", entry.get("eventArgs").get("old").asText());
		assertEquals("St. Michaels", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "sampleAvailable");
		assertNotNull(data);
		assertFalse(data.isNull());
		assertEquals("St. Michaels", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBoolean() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("specimenAvailable", entry.get("attribute").asText());
		assertEquals("true", entry.get("eventArgs").get("old").asText());
		assertEquals("false", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		assertNotNull(data);
		assertTrue(data.isBoolean());
		assertEquals("false", data.asText());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanValueError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid boolean"));

		JsonNode stringValue = jsonNodeFactory.textNode("BAD");
		studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", stringValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringValueError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
		studyRepository.setCaseAttributeValue(study, view, caseValue, "patientId", "stuart", booleanValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateValueError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
		studyRepository.setCaseAttributeValue(study, view, caseValue, "dateEntered", "stuart", booleanValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateValueFormatError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		JsonNode stringValue = jsonNodeFactory.textNode("2015-02-XX");
		studyRepository.setCaseAttributeValue(study, view, caseValue, "dateEntered", "stuart", stringValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOptionValueError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
		studyRepository.setCaseAttributeValue(study, view, caseValue, "sampleAvailable", "stuart", booleanValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOptionUnexpectedValueError() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		JsonNode stringValue = jsonNodeFactory.textNode("BAD");
		studyRepository.setCaseAttributeValue(study, view, caseValue, "sampleAvailable", "stuart", stringValue);
	}

	// Regression test for #6 -- check that multiple writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanTwice() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			fail();
		}
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(true);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			fail();
		}

		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(2, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("specimenAvailable", entry.get("attribute").asText());
		assertEquals("false", entry.get("eventArgs").get("old").asText());
		assertEquals("true", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		assertNotNull(data);
		assertTrue(data.isBoolean());
		assertEquals("true", data.asText());
	}

	// Regression test for #7 -- check that N/A writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanNotAvailable() {
		Studies study = studyRepository.getStudy("DEMO");
		Views view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			ObjectNode notAvailable = objectMapper.createObjectNode();
			notAvailable.put("$notAvailable", true);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", notAvailable);
		} catch (RepositoryException e) {
			fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = studyRepository.getAuditData(study, query);
		assertNotNull(auditEntries);
		assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		assertEquals("stuart", entry.get("eventUser").asText());
		assertEquals("specimenAvailable", entry.get("attribute").asText());
		assertEquals("true", entry.get("eventArgs").get("old").asText());
		assertTrue(entry.get("eventArgs").get("value").isObject());
		assertEquals(true, entry.get("eventArgs").get("value").get("$notAvailable").asBoolean());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		assertNotNull(data);
		assertTrue(data.isObject());
		assertEquals(true, data.get("$notAvailable").asBoolean());
	}
	
	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyAttributes() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		assertNotNull(list);
		assertEquals(27, list.size());
		
		studyRepository.setStudyAttributes(study, list);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		
		assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}
	
	/**
	 * Simple test of deleting a number of attributes.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyAttributes() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		assertNotNull(list);
		assertEquals(27, list.size());
		
		studyRepository.setStudyAttributes(study, list.subList(0, 10));

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		assertEquals(10, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyAttributes() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		assertNotNull(list);
		assertEquals(27, list.size());
		
		Attributes att1 = new Attributes();
		att1.setName("test");
		att1.setType("string");
		att1.setLabel("Test");
		att1.setDescription("First test attribute");
		
		List<Attributes> modified = list.subList(0, 10);
		modified.add(att1);
		
		studyRepository.setStudyAttributes(study, modified);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		assertEquals(11, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Attributes loadedAtt1 = listAgain.get(10);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedAtt1.setId(null);
		assertTrue(EqualsBuilder.reflectionEquals(att1, loadedAtt1));
	}

	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViews() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Views> list = studyRepository.getStudyViews(study);
		assertNotNull(list);
		assertEquals(3, list.size());
		
		studyRepository.setStudyViews(study, list);

		List<Views> listAgain = studyRepository.getStudyViews(study);
		
		assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			Views oldView = list.get(i);
			Views newView = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}
	
	/**
	 * Simple test of deleting a view.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyView() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Views> list = studyRepository.getStudyViews(study);
		assertNotNull(list);
		assertEquals(3, list.size());
		
		studyRepository.setStudyViews(study, list.subList(0, 2));

		List<Views> listAgain = studyRepository.getStudyViews(study);
		assertEquals(2, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			Views oldView = list.get(i);
			Views newView = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyViews() throws RepositoryException {
		Studies study = studyRepository.getStudy("DEMO");
		List<Views> list = studyRepository.getStudyViews(study);
		assertNotNull(list);
		assertEquals(3, list.size());
		
		Views v1 = new Views();
		v1.setName("test");
		v1.setDescription("First test attribute");
		
		List<Views> modified = list.subList(0, 2);
		modified.add(v1);
		
		studyRepository.setStudyViews(study, modified);

		List<Views> listAgain = studyRepository.getStudyViews(study);
		assertEquals(3, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			Views oldAttribute = list.get(i);
			Views newAttribute = listAgain.get(i);
			assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Views loadedV1 = listAgain.get(2);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedV1.setId(null);
		assertTrue(EqualsBuilder.reflectionEquals(v1, loadedV1));
	}


}
