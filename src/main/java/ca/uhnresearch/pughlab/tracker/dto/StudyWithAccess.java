package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyWithAccess extends Study {

	private Permissions access = new Permissions();

	/**
	 * @return the access
	 */
	@JsonProperty
	public Permissions getAccess() {
		return access;
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(Permissions access) {
		this.access = access;
	}
}
