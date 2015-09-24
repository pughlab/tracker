package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Study {

    private String description;

    private Integer id;

    private String name;
    
    private JsonNode options;

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
	public JsonNode getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(JsonNode options) {
		this.options = options;
	}
}

