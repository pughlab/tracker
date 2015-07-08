package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleListResponse extends AbstractResponse {

	List<Role> roles = new ArrayList<Role>();
	Counts counts = new Counts();
	
	public RoleListResponse() { 
		super();
	}

	/**
	 * @return the roles
	 */
	@JsonProperty
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	/**
	 * @return the counts
	 */
	@JsonProperty
	public Counts getCounts() {
		return counts;
	}

	/**
	 * @param counts the counts to set
	 */
	public void setCounts(Counts counts) {
		this.counts = counts;
	}

}
