package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ViewDataResponse extends ViewAttributesResponse {

	Counts counts = new Counts();
	List<JsonNode> records = new ArrayList<JsonNode>();
	
	public ViewDataResponse() {
		super();
	}
	
	public ViewDataResponse(URL url, User user, Study s, View v) {
		super(url, user, s, v);
	}

	@JsonProperty
	public List<JsonNode> getRecords() {
		return records;
	}

	public void setRecords(List<JsonNode> records) {
		this.records = records;
	}

	@JsonProperty
	public Counts getCounts() {
		return counts;
	}

	public void setCounts(Counts counts) {
		this.counts = counts;
	}
}
