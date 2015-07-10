package ca.uhnresearch.pughlab.tracker.dto;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.pac4j.core.profile.CommonProfile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	private String username;
	
	private Boolean administrator = false;
	
	public User() { 
		this(SecurityUtils.getSubject());
	}

	public User(String username) {
		setUsername(username);
	}
	
	public User(Subject subject) {
		
		// TODO factor this better -- the derivation of a username/identity shouldn't involve 
		// conditionals
		@SuppressWarnings("unchecked")
		List<Object> principals = SecurityUtils.getSubject().getPrincipals().asList();
		
		if (principals.size() == 2 && principals.get(1) instanceof CommonProfile) {
			CommonProfile profile = (CommonProfile) principals.get(1);
			setUsername(profile.getEmail());
		} else {
			setUsername(subject.getPrincipal().toString());
		}
		if (subject.hasRole("ROLE_ADMIN")) {
			setAdministrator(true);
		}
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty
	public Boolean getAdministrator() {
		return administrator;
	}

	public void setAdministrator(Boolean administrator) {
		this.administrator = administrator;
	}
}
