package ca.uhnresearch.pughlab.tracker.dto;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;

public class AttributeDTO {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Integer id;
	private String name;
	private String label;
	private Integer rank;
	private String type;
	private String description;
	private JsonNode options;
	
	public AttributeDTO() { }

	public AttributeDTO(Attributes a) {
		setId(a.getId());
		setName(a.getName());
		setLabel(a.getLabel());
		setRank(a.getRank());
		setType(a.getType());
		setDescription(a.getDescription());
		
		String attributeOptions = a.getOptions();
		if (attributeOptions != null) {
			try {
				setOptions(mapper.readValue(attributeOptions, JsonNode.class));
			} catch (IOException e) {
				logger.error("Error in JSON attribute options", e.getMessage());
			}
		}
	}
	
	@JsonIgnore
	public Attributes getAttributes() {
		Attributes result = new Attributes();
		result.setId(getId());
		result.setName(getName());
		result.setLabel(getLabel());
		result.setRank(getRank());
		result.setType(getType());
		result.setDescription(getDescription());
		try {
			result.setOptions(mapper.writeValueAsString(getOptions()));
		} catch (JsonProcessingException e) {
			logger.error("Error in JSON attribute options", e.getMessage());
		}
		return result;
	}
	
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@JsonProperty
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@JsonProperty
	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@JsonProperty
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty
	public JsonNode getOptions() {
		return options;
	}

	public void setOptions(JsonNode options) {
		this.options = options;
	}
}
