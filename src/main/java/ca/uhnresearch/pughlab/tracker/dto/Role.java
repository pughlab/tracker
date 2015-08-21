package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

    private Integer id;
    
    private String studyName;

	private String name;

	public Role() { }

	public Role(String name) {
		setName(name);
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
	 * @return the studyName
	 */
	@JsonProperty
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

}
