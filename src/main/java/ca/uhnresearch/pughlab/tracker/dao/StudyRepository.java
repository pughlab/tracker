package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public interface StudyRepository {

	public abstract List<Studies> getAllStudies();
	
}