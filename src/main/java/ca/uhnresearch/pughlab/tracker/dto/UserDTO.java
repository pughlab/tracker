package ca.uhnresearch.pughlab.tracker.dto;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {

	private String username;

	public UserDTO(String username) {
		setUsername(username);
	}
	
	public UserDTO(Subject subject) {
		setUsername(subject.getPrincipal().toString());
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
