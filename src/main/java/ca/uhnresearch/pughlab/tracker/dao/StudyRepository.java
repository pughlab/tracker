package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public interface StudyRepository {

	public abstract List<Studies> getAllStudies();
	
	public abstract Studies getStudy(String name);
	
	public abstract List<Views> getStudyViews(Studies study);

	public abstract Views getStudyView(Studies study, String name);
}