package ca.uhnresearch.pughlab.tracker.dto;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	private String username;
	
	public User() { }

	public User(String username) {
		setUsername(username);
	}
	
	public User(Subject subject) {
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
