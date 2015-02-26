package ca.uhnresearch.pughlab.tracker.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Attributes;

public class AttributeDTO {
	
	private Integer id;
	private String name;
	private String label;
	private Integer rank;
	private String type;
	private String description;

	public AttributeDTO(Attributes a) {
		setId(a.getId());
		setName(a.getName());
		setLabel(a.getLabel());
		setRank(a.getRank());
		setType(a.getType());
		setDescription(a.getDescription());
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
}
