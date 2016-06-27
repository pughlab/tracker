package ca.uhnresearch.pughlab.tracker.dao.impl;

/**
 * A case information record.
 * 
 * @author stuartw
 */
public class CaseInfo {
	
	/**
	 * The internal case identity.
	 */
	private int id;
	
	/**
	 * The GUID.
	 */
	private String guid;

	/**
	 * The state.
	 */
	private String state;

	/**
	 * Standard constructor for a new CaseInfo.
	 * @param idValue the new id value
	 * @param guidValue the new guid value
	 * @param stateValue the new state value
	 */
	public CaseInfo(int idValue, String guidValue, String stateValue) {
		super();
		this.id = idValue;
		this.guid = guidValue;
		this.state = stateValue;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param idValue the id to set
	 */
	public void setId(int idValue) {
		this.id = idValue;
	}

	/**
	 * @return the GUID
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guidValue the guid to set
	 */
	public void setGuid(String guidValue) {
		this.guid = guidValue;
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
	 * @param stateValue the state to set
	 */
	public void setState(String stateValue) {
		this.state = stateValue;
	}
}

