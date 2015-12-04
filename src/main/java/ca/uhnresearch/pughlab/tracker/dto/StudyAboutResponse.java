package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyAboutResponse extends AbstractResponse {

    private String description;

    private Integer id;

    private String name;
    
    private String about;
    
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

	/**
	 * @return the description
	 */
    @JsonProperty
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the id
	 */
    @JsonProperty
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
    @JsonProperty
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the about
	 */
    @JsonProperty
	public String getAbout() {
		return about;
	}

	/**
	 * @param about the about to set
	 */
	public void setAbout(String about) {
		this.about = about;
	}
}
