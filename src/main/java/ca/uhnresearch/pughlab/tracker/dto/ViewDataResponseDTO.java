package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewDataResponseDTO extends ViewAttributesResponseDTO {

	CountsDTO counts = new CountsDTO();
	List<JsonNode> records = new ArrayList<JsonNode>();
	
	public ViewDataResponseDTO() {
		super();
	}
	
	public ViewDataResponseDTO(URL url, UserDTO user, Studies s, Views v) {
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
	public CountsDTO getCounts() {
		return counts;
	}

	public void setCounts(CountsDTO counts) {
		this.counts = counts;
	}
}
