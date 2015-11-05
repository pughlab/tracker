package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:**/testContextDatabase.xml" })
public class StudyCaseQueryTest {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private StudyRepositoryImpl studyRepository;

	/**
	 * Tests basic study selection. Should return all the cases in a study.
	 */
	@Test
	public void studyTest() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		
		Assert.assertEquals(20, data.size());
		Assert.assertEquals("DEMO-01", data.get(0).get("patientId").asText());
	}

	/**
	 * Tests basic view selection. When a view contains some kind of a row
	 * filter, this should be reflected in the set of rows returned. 
	 */
	@Test
	public void studyViewTest() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		
		Assert.assertEquals(20, data.size());
		Assert.assertEquals("DEMO-01", data.get(0).get("patientId").asText());
	}

	/**
	 * Tests basic case selection. When we have selected both a view and
	 * a study, checks we can filter down to a single case. 
	 */
	@Test
	public void studyViewCaseTest() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 4);
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		
		Assert.assertEquals(1, data.size());
		Assert.assertEquals("DEMO-03", data.get(0).get("patientId").asText());
	}
}
