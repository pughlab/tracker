package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class StudyDTO {
	
	public StudyDTO(Studies s) {
		setId(s.getId());
		setName(s.getName());
		setDescription(s.getDescription());
	}
	
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	private Integer id;
	private String name;
	private String description;
}
