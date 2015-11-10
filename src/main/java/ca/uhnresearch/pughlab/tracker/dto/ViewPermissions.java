package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewPermissions extends StudyPermissions {

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

	private Boolean read = false;
	private Boolean write = false;
	private Boolean download = false;
}
