package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyWithAccess extends Study {

	private StudyPermissions access = new StudyPermissions();

	/**
	 * @return the access
	 */
	@JsonProperty
	public StudyPermissions getAccess() {
		return access;
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(StudyPermissions access) {
		this.access = access;
	}
}
