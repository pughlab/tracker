package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

	private String name;

	public Role() { }

	public Role(String name) {
		setName(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
