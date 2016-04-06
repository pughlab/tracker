package ca.uhnresearch.pughlab.tracker.dao.impl;

public class CaseInfo {
	
	private int id;
	
	private String guid;

	private String state;

	public CaseInfo(int id, String guid, String state) {
		super();
		this.id = id;
		this.guid = guid;
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
	 * @return the GUID
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
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
	}}
