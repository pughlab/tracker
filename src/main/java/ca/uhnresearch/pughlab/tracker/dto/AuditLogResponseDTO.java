package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Studies;

public class AuditLogResponseDTO extends StudyResponseDTO {

	List<JsonNode> log = new ArrayList<JsonNode>();

	public AuditLogResponseDTO() { }

	public AuditLogResponseDTO(URL url, UserDTO user, Studies s) {
		super(url, user, s);
	}

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
