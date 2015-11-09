package ca.uhnresearch.pughlab.tracker.dao;

import java.util.List;

import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The audit log repository has a fairly simple interface, allowing queries and 
 * new records to be added.
 * 
 * @author Stuart Watt
 */
public interface AuditLogRepository {

	/**
	 * Retrieves the audit log data. This is formatted as a set of JSON nodes, as there is 
	 * some reformatting of identifiers to match the tagging within the repository itself. 
	 * @param study the study
	 * @param pager a page filter 
	 * @return list of JSON nodes
	 */
	List<JsonNode> getAuditData(Study study, CasePager pager);
	
	/**
	 * Writes an audit log entry.
	 * @param record the record
	 */
	void writeAuditLogRecord(AuditLogRecord record);
}
