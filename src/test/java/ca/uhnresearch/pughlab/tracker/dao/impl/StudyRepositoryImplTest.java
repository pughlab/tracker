package ca.uhnresearch.pughlab.tracker.dao.impl;

import static junit.framework.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
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
}
