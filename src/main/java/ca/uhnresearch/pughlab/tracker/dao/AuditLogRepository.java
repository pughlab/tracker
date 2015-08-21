package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.fasterxml.jackson.databind.JsonNode;

public interface AuditLogRepository {

	/**
	 * Retrieves the audit log data. This is formatted as a set of JSON nodes, as there is 
	 * some reformatting of identifiers to match the tagging within the repository itself. 
	 * @return list of JSON nodes
	 */
	List<JsonNode> getAuditData(Study study, CaseQuery query);
	
	/**
	 * Writes an audit log entry
	 */
	void writeAuditLogRecord(AuditLogRecord record);
}