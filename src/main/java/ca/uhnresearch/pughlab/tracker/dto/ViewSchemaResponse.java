package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewSchemaResponse extends ViewResponse {

	public ViewSchemaResponse() {
		super();
	}

	public ViewSchemaResponse(URL url, User user, Study s, View v) {
		super(url, user, s, v);
	}

	List<Attributes> attributes = new ArrayList<Attributes>();

	@JsonProperty
	public List<Attributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attributes> attributes) {
		this.attributes = attributes;
	}
}
