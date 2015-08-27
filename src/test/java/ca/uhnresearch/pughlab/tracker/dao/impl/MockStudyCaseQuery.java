package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.ArrayList;
import java.util.List;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;

public class MockStudyCaseQuery implements StudyCaseQuery {
	
	private List<Integer> caseIds = new ArrayList<Integer>();
	
	public MockStudyCaseQuery() { }
	
	public MockStudyCaseQuery(List<Integer> caseIds) {
		this.caseIds = caseIds;
	}

	/**
	 * @return the cases
	 */
	public List<Integer> getCases() {
		return caseIds;
	}

	/**
	 * @param cases the cases to set
	 */
	public void setCases(List<Integer> caseIds) {
		this.caseIds = caseIds;
	}
}
