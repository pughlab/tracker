package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PermissionsDTO {

	public PermissionsDTO() { }
	
	/**
	 * @return the readAllowed
	 */
	@JsonProperty
	public Boolean getReadAllowed() {
		return readAllowed;
	}

	/**
	 * @param readAllowed the readAllowed to set
	 */
	public void setReadAllowed(Boolean readAllowed) {
		this.readAllowed = readAllowed;
	}

	/**
	 * @return the writeAllowed
	 */
	@JsonProperty
	public Boolean getWriteAllowed() {
		return writeAllowed;
	}

	/**
	 * @param writeAllowed the writeAllowed to set
	 */
	public void setWriteAllowed(Boolean writeAllowed) {
		this.writeAllowed = writeAllowed;
	}

	/**
	 * @return the downloadAllowed
	 */
	@JsonProperty
	public Boolean getDownloadAllowed() {
		return downloadAllowed;
	}

	/**
	 * @param downloadAllowed the downloadAllowed to set
	 */
	public void setDownloadAllowed(Boolean downloadAllowed) {
		this.downloadAllowed = downloadAllowed;
	}

	/**
	 * @return the adminAllowed
	 */
	public Boolean getAdminAllowed() {
		return adminAllowed;
	}

	/**
	 * @param adminAllowed the adminAllowed to set
	 */
	public void setAdminAllowed(Boolean adminAllowed) {
		this.adminAllowed = adminAllowed;
	}

	private Boolean adminAllowed = false;
	private Boolean readAllowed = false;
	private Boolean writeAllowed = false;
	private Boolean downloadAllowed = false;
}
