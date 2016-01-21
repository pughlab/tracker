package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Study {

    private String description;

    private Integer id;

    private String name;
    
    private String about;
    
    private ObjectNode options;

	@JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	@JsonProperty
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	@JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	/**
	 * @return the options
	 */
	@JsonProperty
	public ObjectNode getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(ObjectNode options) {
		this.options = options;
	}

	/**
	 * Returns the about text for the study.
	 * @return the about
	 */
	@JsonProperty
	public String getAbout() {
		return about;
	}

	/**
	 * Sets the about text for the study.
	 * @param about the about to set
	 */
	public void setAbout(String about) {
		this.about = about;
	}
}

