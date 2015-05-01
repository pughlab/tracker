package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewSchemaResponse extends ViewResponse {

	public ViewSchemaResponse() {
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
}
