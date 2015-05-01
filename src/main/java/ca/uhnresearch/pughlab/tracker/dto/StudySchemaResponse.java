package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudySchemaResponse extends StudyViewsResponse {

	public StudySchemaResponse() {
		super();
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
