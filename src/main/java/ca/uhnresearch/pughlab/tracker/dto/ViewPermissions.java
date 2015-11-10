package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewPermissions extends AdministratorPermissions {

	public ViewPermissions() { }
	
	/**
	 * Retrieves the read permission.
	 * @return the read
	 */
	@JsonProperty
	public Boolean getRead() {
		return read;
	}

	/**
	 * Sets the read permission.
	 * @param read the read to set
	 */
	public void setRead(Boolean read) {
		this.read = read;
	}

	/**
	 * Retrieves the write permission.
	 * @return the write
	 */
	@JsonProperty
	public Boolean getWrite() {
		return write;
	}

	/**
	 * Sets the write permission.
	 * @param write the write to set
	 */
	public void setWrite(Boolean write) {
		this.write = write;
	}

	/**
	 * Retrieves the download permission.
	 * @return the download
	 */
	@JsonProperty
	public Boolean getDownload() {
		return download;
	}

	/**
	 * Sets the download permission.
	 * @param download the download to set
	 */
	public void setDownload(Boolean download) {
		this.download = download;
	}

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

	private Boolean read = false;
	private Boolean write = false;
	private Boolean download = false;
	private Boolean create = false;
	private Boolean delete = false;
}
