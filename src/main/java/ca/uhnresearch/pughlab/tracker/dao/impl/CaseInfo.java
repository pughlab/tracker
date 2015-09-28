package ca.uhnresearch.pughlab.tracker.dao.impl;

public class CaseInfo {
	
	private int id;
	
	private String state;

	public CaseInfo(int id, String state) {
		super();
		this.id = id;
		this.state = state;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the state
	 */
	public boolean hasState() {
		return state != null;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
}
