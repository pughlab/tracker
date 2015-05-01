package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewDataResponse extends ViewAttributesResponse {

	Counts counts = new Counts();
	List<ObjectNode> records = new ArrayList<ObjectNode>();
	
	public ViewDataResponse() {
		super();
	}
	
	@JsonProperty
	public List<ObjectNode> getRecords() {
		return records;
	}

	public void setRecords(List<ObjectNode> records) {
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
