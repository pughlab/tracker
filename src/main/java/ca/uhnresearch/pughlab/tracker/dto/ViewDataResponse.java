package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewDataResponse extends ViewAttributesResponse {

	Counts counts = new Counts();
	List<ObjectNode> records = new ArrayList<ObjectNode>();
	ObjectNode filter = null;
	
	/**
	 * Makes a new ViewDataResponse
	 */
	public ViewDataResponse() {
		super();
	}
	
	/**
	 * @return the records
	 */
	@JsonProperty
	public List<ObjectNode> getRecords() {
		return records;
	}

	/**
	 * @param records the records to set
	 */
	public void setRecords(List<ObjectNode> records) {
		this.records = records;
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

	/**
	 * @return the filter
	 */
	@JsonProperty
	public ObjectNode getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(ObjectNode filter) {
		this.filter = filter;
	}

}
