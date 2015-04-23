package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class StudyWithAccessDTO extends StudyDTO {

	public StudyWithAccessDTO(Studies s) {
		super(s);
	}

	private PermissionsDTO access = new PermissionsDTO();

	/**
	 * @return the access
	 */
	@JsonProperty
	public PermissionsDTO getAccess() {
		return access;
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(PermissionsDTO access) {
		this.access = access;
	}
}
