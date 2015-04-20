package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewDTO {
	
	public ViewDTO(Views v) {
		setId(v.getId());
		setName(v.getName());
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

	private Integer id;
	private String name;
}
