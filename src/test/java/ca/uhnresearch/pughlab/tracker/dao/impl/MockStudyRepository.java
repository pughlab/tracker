package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.Attributes;
import ca.uhnresearch.pughlab.tracker.domain.CaseAttributeBooleans;
import ca.uhnresearch.pughlab.tracker.domain.CaseAttributeDates;
import ca.uhnresearch.pughlab.tracker.domain.CaseAttributeStrings;
import ca.uhnresearch.pughlab.tracker.domain.Cases;
import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class MockStudyRepository implements StudyRepository {

	private Logger logger = LoggerFactory.getLogger(MockStudyRepository.class);

	List<Studies> studies = new ArrayList<Studies>();
	List<Attributes> attributes = new ArrayList<Attributes>();
	List<Views> views = new ArrayList<Views>();
	List<ViewAttributes> viewAttributes = new ArrayList<ViewAttributes>();
	List<Cases> cases = new ArrayList<Cases>();
	List<CaseAttributeStrings> strings = new ArrayList<CaseAttributeStrings>();
	List<CaseAttributeDates> dates = new ArrayList<CaseAttributeDates>();
	List<CaseAttributeBooleans> booleans = new ArrayList<CaseAttributeBooleans>();

	public MockStudyRepository() {
		
		// Initialize the studies
		studies.add(mockStudy(1, "DEMO", "A demo clinical genomics study"));
		studies.add(mockStudy(1, "OTHER", "A different clinical genomics study"));
		
		views.add(mockView(1, "complete", "Manages the whole study", 1));
		views.add(mockView(2, "track", "Tracks the study", 1));
		views.add(mockView(3, "secondary", "Tracks only secondary", 1));
		
		// Initialize the attributes
		attributes.add(mockAttribute(1, "dateEntered", "Date Entered", 1, 1, "date"));
		attributes.add(mockAttribute(2, "patientId", "Patient", 2, 1, "string"));
		attributes.add(mockAttribute(3, "mrn", "MRN", 3, 1, "string"));
		attributes.add(mockAttribute(4, "consentDate", "Date of Consent", 4, 1, "date"));
		attributes.add(mockAttribute(5, "specimenAvailable", "Biobank Specimen Available? (Yes/No)", 5, 1, "date"));
		
		// And the view attribute mapping
		viewAttributes.add(mockViewAttribute(1, 1, null));
		viewAttributes.add(mockViewAttribute(2, 1, null));
		viewAttributes.add(mockViewAttribute(3, 1, null));
		viewAttributes.add(mockViewAttribute(4, 1, null));
		viewAttributes.add(mockViewAttribute(5, 1, null));
		
		viewAttributes.add(mockViewAttribute(1, 2, null));
		viewAttributes.add(mockViewAttribute(2, 2, null));
		viewAttributes.add(mockViewAttribute(5, 2, "{\"classes\": [\"label5\"]}"));
		
		viewAttributes.add(mockViewAttribute(1, 3, null));
		viewAttributes.add(mockViewAttribute(2, 3, null));
		viewAttributes.add(mockViewAttribute(5, 3, "{\"classes\": [\"label5\"]}"));
		
		// And finally add some cases
		for(Integer i = 0; i < 10; i++) {
			cases.add(mockCase(i));
		}
		
		// And case attributes, of different types - modelling the persistence (which in 
		// an ideal world we'd handle better by superimposing an interface on the generated
		// classes, but we can't really make things that simple.

		for(Integer i = 0; i < 10; i++) {
			Calendar date = Calendar.getInstance();
			date.set(2014, 8, i + 10);
			dates.add(mockCaseAttributeDates(i, "patientId", new Date(date.getTimeInMillis())));
			strings.add(mockCaseAttributeStrings(i, "patientId", String.format("DEMO-%02d", i)));
		}
		
		strings.add(mockCaseAttributeStrings(1, "mrn", "0101010"));
		strings.add(mockCaseAttributeStrings(2, "mrn", "0202020"));
		strings.add(mockCaseAttributeStrings(3, "mrn", "0303030"));
		strings.add(mockCaseAttributeStrings(4, "mrn", "0404040"));
		strings.add(mockCaseAttributeStrings(5, "mrn", "0505050"));

		booleans.add(mockCaseAttributeBooleans(1, "specimenAvailable", true));
		booleans.add(mockCaseAttributeBooleans(2, "specimenAvailable", false));
		booleans.add(mockCaseAttributeBooleans(3, "specimenAvailable", true));
		CaseAttributeBooleans bv = mockCaseAttributeBooleans(4, "specimenAvailable", null);
		bv.setNotAvailable(true);
		booleans.add(bv);
		booleans.add(mockCaseAttributeBooleans(5, "specimenAvailable", false));
}
	
	private Cases mockCase(Integer id) {
		Cases c = new Cases();
		c.setId(id);
		return c;
	}
	
	private CaseAttributeStrings mockCaseAttributeStrings(Integer caseId, String attribute, String value) {
		CaseAttributeStrings obj = new CaseAttributeStrings();
		obj.setActive(true);
		obj.setCaseId(caseId);
		obj.setAttribute(attribute);
		obj.setValue(value);
		return obj;
	}
	
	private CaseAttributeDates mockCaseAttributeDates(Integer caseId, String attribute, Date value) {
		CaseAttributeDates obj = new CaseAttributeDates();
		obj.setActive(true);
		obj.setCaseId(caseId);
		obj.setAttribute(attribute);
		obj.setValue(value);
		return obj;
	}
	
	private CaseAttributeBooleans mockCaseAttributeBooleans(Integer caseId, String attribute, Boolean value) {
		CaseAttributeBooleans obj = new CaseAttributeBooleans();
		obj.setActive(true);
		obj.setCaseId(caseId);
		obj.setAttribute(attribute);
		obj.setValue(value);
		return obj;
	}
	
	private ViewAttributes mockViewAttribute(Integer attributeId, Integer viewId, String options) {
		ViewAttributes vatt = new ViewAttributes();
		vatt.setAttributeId(attributeId);
		vatt.setViewId(viewId);
		vatt.setOptions(options);
		return vatt;
	}
	
	private Studies mockStudy(Integer id, String name, String description) {
		Studies study = new Studies();
		study.setId(id);
		study.setName(name);
		study.setDescription(description);
		return study;
	}

	private Attributes mockAttribute(Integer id, String name, String label, Integer rank, Integer studyId, String type) {
		Attributes att = new Attributes();
		att.setId(id);
		att.setName(name);
		att.setLabel(label);
		att.setRank(rank);
		att.setStudyId(studyId);
		att.setType(type);
		return att;
	}

	private Views mockView(Integer id, String name, String description, Integer studyId) {
		Views view = new Views();
		view.setId(id);
		view.setStudyId(studyId);
		view.setName(name);
		view.setDescription(description);
		return view;
	}

	// Mocked getStudy
	public Studies getStudy(String name) {
		for(Studies s : studies) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}

	// Mocked getAll
	public List<Studies> getAllStudies() {
		return studies;
	}
	
	/**
	 * A mocked getStudyViews
	 */
	public List<Views> getStudyViews(Studies study) {
		List<Views> result = new ArrayList<Views>();
		for (Views v : views) {
			if (v.getStudyId().equals(study.getId())) {
				result.add(v);
			}
		}
		logger.info("Found views: " + result.size());
		return result;
	}

	/**
	 * A mocked getStudyView
	 */
	public Views getStudyView(Studies study, String viewName) {
		for (Views v : views) {
			if (v.getStudyId().equals(study.getId()) && v.getName().equals(viewName)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * A mocked getViewAttributes
	 */
	public List<Attributes> getViewAttributes(Studies study, Views view) {
		List<Attributes> result = new ArrayList<Attributes>();
		for(Attributes a : attributes) {
			final Integer attributeId = a.getId();
			final Predicate<ViewAttributes> pred = new Predicate<ViewAttributes>() { 
				public boolean apply(ViewAttributes va) {
					return va.getAttributeId().equals(attributeId);
				}
			};
			if (Iterables.any(viewAttributes, pred)) {
				result.add(a);
			}			
		}
		return result;
	}

	/**
	 * A mocked getData
	 */
	public List<JsonNode> getData(Studies study, Views view, List<Attributes> attributes, CaseQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
}
