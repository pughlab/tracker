package ca.uhnresearch.pughlab.tracker.dto;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewDTO {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ViewDTO() { }
	
	public ViewDTO(Views v) {
		setId(v.getId());
		setName(v.getName());
		setDescription(v.getDescription());

		String attributeOptions = v.getOptions();
		if (attributeOptions != null) {
			try {
				setOptions(mapper.readValue(attributeOptions, JsonNode.class));
			} catch (IOException e) {
				logger.error("Error in JSON attribute options", e.getMessage());
			}
		}
	}
	
	@JsonIgnore
	public Views getViews() {
		Views result = new Views();
		result.setId(getId());
		result.setName(getName());
		result.setDescription(getDescription());
		try {
			result.setOptions(mapper.writeValueAsString(getOptions()));
		} catch (JsonProcessingException e) {
			logger.error("Error in JSON attribute options", e.getMessage());
		}
		return result;
	}
	
	/**
	 * @return the name
	 */
	@JsonProperty
	public String getName() {
		return name;
	}

	/**
	 * @param name the permissions to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	@JsonProperty
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the permissions to set
	 */
	public void setId(Integer id) {
		this.id = id;
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

	private Integer id;
	private String name;
	private String description;
	private JsonNode options;
}
