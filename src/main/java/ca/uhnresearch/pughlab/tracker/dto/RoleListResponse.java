package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleListResponse extends AbstractResponse {

	List<Role> roles = new ArrayList<Role>();
	
	public RoleListResponse() { 
		super();
	}

	@JsonProperty
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
