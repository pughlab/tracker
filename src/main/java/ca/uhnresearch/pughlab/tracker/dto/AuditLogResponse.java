package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class AuditLogResponse extends StudyResponse {

	List<JsonNode> log = new ArrayList<JsonNode>();

	public AuditLogResponse() { }

	/**
	 * @return the log
	 */
	@JsonProperty
	public List<JsonNode> getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(List<JsonNode> log) {
		this.log = log;
	}


}
