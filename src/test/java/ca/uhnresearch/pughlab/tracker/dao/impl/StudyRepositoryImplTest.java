package ca.uhnresearch.pughlab.tracker.dao.impl;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Rule;
import org.junit.Test;
import junit.framework.Assert;

import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlInsertWithKeyCallback;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.domain.QAuditLog;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.UpdateEventService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/testContextDatabase.xml" })
public class StudyRepositoryImplTest {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private StudyRepositoryImpl studyRepository;
	
	@Autowired
    private AuditLogRepositoryImpl auditLogRepository;

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testWiring() {
		Assert.assertNotNull(studyRepository);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudy() {
		Study s = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(s);
		Assert.assertEquals("DEMO", s.getName());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudy() {
		Study s = studyRepository.getStudy("DEMOX");
		Assert.assertNull(s);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudies() {
		List<Study> list = studyRepository.getAllStudies();
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyViews() {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyView() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "complete");
		Assert.assertNotNull(v);
		Assert.assertEquals("complete", v.getName());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyViewOptions() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "secondary");
		Assert.assertNotNull(v);
		Assert.assertEquals("secondary", v.getName());
		
		Assert.assertNotNull(v.getOptions());
		Assert.assertNotNull(v.getOptions().get("rows"));
		Assert.assertTrue(v.getOptions().get("rows").isArray());
		Assert.assertEquals(1, v.getOptions().get("rows").size());
		Assert.assertNotNull(v.getOptions().get("rows").get(0));
		Assert.assertTrue(v.getOptions().get("rows").get(0).isObject());
		Assert.assertEquals("study", v.getOptions().get("rows").get(0).get("attribute").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewOptions() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "track");
		Assert.assertNotNull(v);
		Assert.assertEquals("track", v.getName());
		
		ObjectNode viewOptions = objectMapper.createObjectNode();
		ObjectNode viewOptionDescriptor = objectMapper.createObjectNode();
		ArrayNode viewArray = objectMapper.createArrayNode();
		viewOptionDescriptor.put("attribute", "dateEntered");
		viewOptionDescriptor.put("value", "test");
		viewArray.add(viewOptionDescriptor);
		viewOptions.set("rows", viewArray);
		
		v.setOptions(viewOptions);
		
		try {
			studyRepository.setStudyView(study, v);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		View modifiedView = studyRepository.getStudyView(study, "track");
		
		Assert.assertNotNull(modifiedView.getOptions());
		Assert.assertNotNull(modifiedView.getOptions().get("rows"));
		Assert.assertTrue(modifiedView.getOptions().get("rows").isArray());
		Assert.assertEquals(1, modifiedView.getOptions().get("rows").size());
		Assert.assertNotNull(modifiedView.getOptions().get("rows").get(0));
		Assert.assertTrue(modifiedView.getOptions().get("rows").get(0).isObject());
		Assert.assertEquals("dateEntered", modifiedView.getOptions().get("rows").get(0).get("attribute").asText());
		Assert.assertEquals("test", modifiedView.getOptions().get("rows").get(0).get("value").asText());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewOptionsInvalidView() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "track");
		Assert.assertNotNull(v);
		Assert.assertEquals("track", v.getName());
		
		ObjectNode viewOptions = objectMapper.createObjectNode();
		ObjectNode viewOptionDescriptor = objectMapper.createObjectNode();
		ArrayNode viewArray = objectMapper.createArrayNode();
		viewOptionDescriptor.put("attribute", "dateEntered");
		viewOptionDescriptor.put("value", "test");
		viewArray.add(viewOptionDescriptor);
		viewOptions.set("rows", viewArray);
		
		v.setOptions(viewOptions);
		v.setStudyId(100);
	
		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString("Can't update view for a different study"));

		studyRepository.setStudyView(study, v);
	}


	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudyView() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "completed");
		Assert.assertNull(v);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(27, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetViewAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(27, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSmallerGetViewAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetData() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);
		List<ObjectNode> list = studyRepository.getData(study, view, attributes, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(10, list.size());
	}
	
	/**
	 * Regression test for #53 -- checks that only lrgitimate view attributes are 
	 * returned.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataSecurity() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(10);
		List<ObjectNode> list = studyRepository.getData(study, view, attributes, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(10, list.size());
		Assert.assertFalse(list.get(0).has("mrn"));
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoLimit() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(null);
		List<ObjectNode> list = studyRepository.getData(study, view, attributes, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(20, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoOffset() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(null);
		query.setLimit(5);
		List<ObjectNode> list = studyRepository.getData(study, view, attributes, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(5, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataOrdered() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		query.setOrderField("consentDate");
		query.setOrderDirection(CaseQuery.OrderDirection.DESC);
		List<ObjectNode> list = studyRepository.getData(study, view, attributes, query);
		Assert.assertNotNull(list);
		Assert.assertEquals(5, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testRecordCount() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Long count = studyRepository.getRecordCount(study, view);
		Assert.assertEquals(20, count.intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCase() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(1, caseValue.getId().intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleMissingCase() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 100);
		Assert.assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleFromDifferentStudy() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 22);
		Assert.assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseData(study, view, caseValue);
		Assert.assertNotNull(data);
		
		String date = data.get("dateEntered").asText();
		Assert.assertNotNull(date);
		Assert.assertEquals("2014-08-20", date);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseNumberValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseData(study, view, caseValue);
		Assert.assertNotNull(data);
		
		Assert.assertTrue(data.has("numberCores"));
		Double cores = data.get("numberCores").asDouble();
		Assert.assertNotNull(cores);
		Assert.assertTrue(Math.abs(cores - 2.0) < 0.00000001);
	}

	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValuesNotes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseData(study, view, caseValue);
		Assert.assertNotNull(data);
		
		JsonNode notes = data.get("$notes");
		Assert.assertNotNull(notes);
		
		// No notes here
		Assert.assertNull(notes.get("specimenAvailable"));

		// Notes here
		JsonNode consentDateNotes = notes.get("consentDate");
		Assert.assertNotNull(consentDateNotes);

		JsonNode consentDateLocked = consentDateNotes.get("locked");
		Assert.assertNotNull(consentDateLocked);
		Assert.assertTrue(consentDateLocked.isBoolean());
		Assert.assertTrue(consentDateLocked.asBoolean());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "dateEntered");
		Assert.assertNotNull(data);
		Assert.assertEquals("2014-08-20", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValuesNotAvailable() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 2);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "trackerDate");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isObject());
		Assert.assertTrue(data.has("$notAvailable"));
		Assert.assertEquals("true", data.get("$notAvailable").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeMissing() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "bloodCollDate");
		Assert.assertNull(data);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLog() {
		Study study = studyRepository.getStudy("DEMO");
		
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(0, auditEntries.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithNoLimits() {
		Study study = studyRepository.getStudy("DEMO");
		
		CaseQuery query = new CaseQuery();
		query.setOffset(null);
		query.setLimit(null);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(0, auditEntries.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithBadData() {
		Study study = studyRepository.getStudy("DEMO");
		
		CaseQuery query = new CaseQuery();
		query.setOffset(null);
		query.setLimit(null);
		
		List<AuditLogRecord> data = new ArrayList<AuditLogRecord>();
		AuditLogRecord entry = new AuditLogRecord();
		entry.setEventTime(Timestamp.from(Instant.now()));
		entry.setEventArgs("{");
		data.add(entry);
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.newSqlQuery()).andStubReturn(originalTemplate.newSqlQuery());
		expect(mockTemplate.query(anyObject(SQLQuery.class), anyObject(QAuditLog.class))).andStubReturn(data);
		replay(mockTemplate);
		
		studyRepository.setTemplate(mockTemplate);
		List<JsonNode> auditEntries = null;

		try {
			auditEntries = auditLogRepository.getAuditData(study, query);
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
		
		Assert.assertNotNull(auditEntries);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithGoodData() {
		Study study = studyRepository.getStudy("DEMO");
		
		CaseQuery query = new CaseQuery();
		query.setOffset(null);
		query.setLimit(null);
		
		List<AuditLogRecord> data = new ArrayList<AuditLogRecord>();
		AuditLogRecord entry = new AuditLogRecord();
		entry.setEventTime(Timestamp.from(Instant.now()));
		entry.setEventArgs("{\"old\":null,\"value\":100}");
		data.add(entry);
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.newSqlQuery()).andStubReturn(originalTemplate.newSqlQuery());
		expect(mockTemplate.query(anyObject(SQLQuery.class), anyObject(QAuditLog.class))).andStubReturn(data);
		replay(mockTemplate);
		
		studyRepository.setTemplate(mockTemplate);
		List<JsonNode> auditEntries = null;

		try {
			auditEntries = auditLogRepository.getAuditData(study, query);
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
		
		Assert.assertNotNull(auditEntries);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDate() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			studyRepository.setCaseAttributeValue(study, view, caseValue, "dateEntered", "stuart", jsonNodeFactory.nullNode());
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("dateEntered", entry.get("attribute").asText());
		Assert.assertEquals("2014-08-20", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("value").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "dateEntered");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateInsert() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 6);
		
		try {
			JsonNode dateValue = jsonNodeFactory.textNode("2014-02-03");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "procedureDate", "stuart", dateValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("procedureDate", entry.get("attribute").asText());
		Assert.assertTrue(entry.get("eventArgs").get("old").isNull());
		Assert.assertEquals("2014-02-03", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "procedureDate");
		Assert.assertNotNull(data);
		Assert.assertEquals("2014-02-03", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueString() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode stringValue = jsonNodeFactory.textNode("DEMO-XX");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "patientId", "stuart", stringValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("patientId", entry.get("attribute").asText());
		Assert.assertEquals("DEMO-01", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("DEMO-XX", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "patientId");
		Assert.assertNotNull(data);
		Assert.assertFalse(data.isNull());
		Assert.assertEquals("DEMO-XX", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringInsert() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 10);
		
		try {
			JsonNode stringValue = jsonNodeFactory.textNode("SMP-XX");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenNo", "stuart", stringValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenNo", entry.get("attribute").asText());
		Assert.assertTrue(entry.get("eventArgs").get("old").isNull());
		Assert.assertEquals("SMP-XX", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenNo");
		Assert.assertNotNull(data);
		Assert.assertFalse(data.isNull());
		Assert.assertEquals("SMP-XX", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringNull() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			studyRepository.setCaseAttributeValue(study, view, caseValue, "patientId", "stuart", jsonNodeFactory.nullNode());
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("patientId", entry.get("attribute").asText());
		Assert.assertEquals("DEMO-01", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("value").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "patientId");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOption() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode stringValue = jsonNodeFactory.textNode("St. Michaels");
			studyRepository.setCaseAttributeValue(study, view, caseValue, "sampleAvailable", "stuart", stringValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("sampleAvailable", entry.get("attribute").asText());
		Assert.assertEquals("LMP", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("St. Michaels", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "sampleAvailable");
		Assert.assertNotNull(data);
		Assert.assertFalse(data.isNull());
		Assert.assertEquals("St. Michaels", data.asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBoolean() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("false", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isBoolean());
		Assert.assertEquals("false", data.asText());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
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
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(false);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		try {
			JsonNode booleanValue = jsonNodeFactory.booleanNode(true);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", booleanValue);
		} catch (RepositoryException e) {
			Assert.fail();
		}

		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(2, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("false", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("value").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isBoolean());
		Assert.assertEquals("true", data.asText());
	}

	// Regression test for #7 -- check that N/A writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanNotAvailable() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			ObjectNode notAvailable = objectMapper.createObjectNode();
			notAvailable.put("$notAvailable", true);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", notAvailable);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("value").isObject());
		Assert.assertEquals(true, entry.get("eventArgs").get("value").get("$notAvailable").asBoolean());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isObject());
		Assert.assertEquals(true, data.get("$notAvailable").asBoolean());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanNull() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);
		
		try {
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", jsonNodeFactory.nullNode());
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("value").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isNull());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteNonExistentValue() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 15);
		
		try {
			ObjectNode notAvailable = objectMapper.createObjectNode();
			notAvailable.put("$notAvailable", true);
			studyRepository.setCaseAttributeValue(study, view, caseValue, "specimenAvailable", "stuart", notAvailable);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CaseQuery query = new CaseQuery();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("null", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("value").isObject());
		Assert.assertEquals(true, entry.get("eventArgs").get("value").get("$notAvailable").asBoolean());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = studyRepository.getCaseAttributeValue(study, view, caseValue, "specimenAvailable");
		Assert.assertNotNull(data);
		Assert.assertTrue(data.isObject());
		Assert.assertEquals(true, data.get("$notAvailable").asBoolean());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteMissingAttribute() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Cases caseValue = studyRepository.getStudyCase(study, view, 1);

		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString("Can't find attribute"));
	
		studyRepository.setCaseAttributeValue(study, view, caseValue, "dateEnteredX", "stuart", null);
	}

	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(27, list.size());
		
		studyRepository.setStudyAttributes(study, list);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}
	
	/**
	 * Simple test of deleting a number of attributes.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(27, list.size());
		
		studyRepository.setStudyAttributes(study, list.subList(0, 10));

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		Assert.assertEquals(10, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(27, list.size());
		
		Attributes att1 = new Attributes();
		att1.setName("test");
		att1.setType("string");
		att1.setLabel("Test");
		att1.setDescription("First test attribute");
		
		List<Attributes> modified = list.subList(0, 10);
		modified.add(att1);
		
		studyRepository.setStudyAttributes(study, modified);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		Assert.assertEquals(11, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Attributes loadedAtt1 = listAgain.get(10);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedAtt1.setId(null);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(att1, loadedAtt1));
	}

	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViews() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		
		studyRepository.setStudyViews(study, list);

		List<View> listAgain = studyRepository.getStudyViews(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			View oldView = list.get(i);
			View newView = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}
	
	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewsUpdateKey() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		
		View oldView = list.remove(2);
		View newView = new View();
		newView.setId(oldView.getId());
		newView.setStudyId(oldView.getStudyId());
		newView.setOptions(oldView.getOptions());
		newView.setName("testView");
		newView.setDescription("Test View");
		list.add(newView);
		Assert.assertEquals(3, list.size());
		
		studyRepository.setStudyViews(study, list);

		List<View> listAgain = studyRepository.getStudyViews(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			View oldViewRead = list.get(i);
			View newViewREad = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldViewRead, newViewREad));
		}
	}
	
	/**
	 * Simple test of deleting a view.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyView() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		
		studyRepository.setStudyViews(study, list.subList(0, 2));

		List<View> listAgain = studyRepository.getStudyViews(study);
		Assert.assertEquals(2, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			View oldView = list.get(i);
			View newView = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyViews() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		
		View v1 = new View();
		v1.setName("test");
		v1.setDescription("First test attribute");
		
		List<View> modified = list.subList(0, 2);
		modified.add(v1);
		
		studyRepository.setStudyViews(study, modified);

		List<View> listAgain = studyRepository.getStudyViews(study);
		Assert.assertEquals(3, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			View oldAttribute = list.get(i);
			View newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		View loadedV1 = listAgain.get(2);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedV1.setId(null);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(v1, loadedV1));
	}

	/**
	 * Simple test of writing the exact same attributes back into the view. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		studyRepository.setViewAttributes(study, view, list);

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			ViewAttributes oldAttribute = list.get(i);
			ViewAttributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}
	
	/**
	 * Simple test of deleting a number of attributes.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		studyRepository.setViewAttributes(study, view, list.subList(0, 10));

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		Assert.assertEquals(10, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			ViewAttributes oldAttribute = list.get(i);
			ViewAttributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		ViewAttributes att1 = new ViewAttributes();
		att1.setId(8);
		att1.setName("specimenNo");
		att1.setType("string");
		att1.setLabel("Specimen #");
		att1.setStudyId(study.getId());
		
		List<ViewAttributes> modified = list.subList(0, 10);
		modified.add(att1);
		
		studyRepository.setViewAttributes(study, view, modified);

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		Assert.assertEquals(11, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Attributes loadedAtt1 = listAgain.get(10);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedAtt1.setId(att1.getId());
		loadedAtt1.setRank(att1.getRank());
		Assert.assertTrue(EqualsBuilder.reflectionEquals(att1, loadedAtt1));
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddMissingViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		ViewAttributes att1 = new ViewAttributes();
		att1.setId(28);
		att1.setName("unknown");
		att1.setType("string");
		att1.setLabel("Specimen #");
		att1.setStudyId(study.getId());
		
		List<ViewAttributes> modified = list.subList(0, 10);
		modified.add(att1);
	
		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString("Missing attribute"));

		studyRepository.setViewAttributes(study, view, modified);
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testNewCase() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		Cases newCase = studyRepository.newStudyCase(study, view, "test");
		Assert.assertNotNull(newCase);
		Assert.assertNotNull(newCase.getId());
		Assert.assertNotNull(newCase.getStudyId());
		
		// And now let's dig out the new case -- mainly to check that we can actually
		// follow this identifier.
		Cases caseValue = studyRepository.getStudyCase(study, view, newCase.getId());
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(newCase.getId(), caseValue.getId());
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Transactional
	@Rollback(true)
	public void testFailingNewCase() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.insertWithKey(anyObject(RelationalPath.class), anyObject(SqlInsertWithKeyCallback.class))).andStubReturn(null);
		replay(mockTemplate);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Can't create new case"));
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		studyRepository.setTemplate(mockTemplate);

		try {
			studyRepository.newStudyCase(study, view, "test");
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testNewCaseWithoutManager() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		UpdateEventService oldService = studyRepository.getUpdateEventService();
		studyRepository.setUpdateEventService(null);
		
		Cases newCase = null;
		try {
			newCase = studyRepository.newStudyCase(study, view, "test");
		} finally {
			studyRepository.setUpdateEventService(oldService);
		}
		
		Assert.assertNotNull(newCase);
		Assert.assertNotNull(newCase.getId());
		Assert.assertNotNull(newCase.getStudyId());
		
		// And now let's dig out the new case -- mainly to check that we can actually
		// follow this identifier.
		Cases caseValue = studyRepository.getStudyCase(study, view, newCase.getId());
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(newCase.getId(), caseValue.getId());
	}

}
