package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyPermissions extends AdministratorPermissions {

	/**
	 * Retrieves the create permission.
	 * @return the create
	 */
	@JsonProperty
	public Boolean getCreate() {
		return create;
	}

	/**
	 * Sets the create permission.
	 * @param create the create to set
	 */
	public void setCreate(Boolean create) {
		this.create = create;
	}

	/**
	 * Retrieves the view permission.
	 * @return the view
	 */
	@JsonProperty
	public Boolean getView() {
		return view;
	}

	/**
	 * Sets the view permission.
	 * @param view the view to set
	 */
	public void setView(Boolean view) {
		this.view = view;
	}

	/**
	 * Retrieves the delete permission.
	 * @return the delete
	 */
	@JsonProperty
	public Boolean getDelete() {
		return delete;
	}

	/**
	 * Sets the delete permission.
	 * @param delete the delete to set
	 */
	public void setDelete(Boolean delete) {
		this.delete = delete;
	}

	/**
	 * Retrieves the about permission.
	 * @return the about
	 */
	@JsonProperty
	public Boolean getAbout() {
		return about;
	}

	/**
	 * Sets the about permission.
	 * @param about the about to set
	 */
	public void setAbout(Boolean about) {
		this.about = about;
	}

	private Boolean create = false;
	private Boolean delete = false;
	private Boolean view = false;
	private Boolean about = false;
}
