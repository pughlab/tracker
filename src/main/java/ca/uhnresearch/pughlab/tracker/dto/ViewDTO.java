package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewDTO {
	
	public ViewDTO(Views v) {
		setId(v.getId());
		setName(v.getName());
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

	private Integer id;
	private String name;
}
