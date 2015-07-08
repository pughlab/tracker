package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewAttributesResponse extends ViewResponse {
	
	public ViewAttributesResponse() {
		super();
	}
	
	List<ViewAttributes> attributes = new ArrayList<ViewAttributes>();

	@JsonProperty
	public List<ViewAttributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ViewAttributes> attributes) {
		this.attributes = attributes;
	}

	Permissions permissions = new Permissions();

	@JsonProperty
	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}
}
