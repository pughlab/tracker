package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewDTO {
	
	public ViewDTO() { }
	
	public ViewDTO(Views v) {
		setId(v.getId());
		setName(v.getName());
		setDescription(v.getDescription());
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

	private Integer id;
	private String name;
	private String description;
}
