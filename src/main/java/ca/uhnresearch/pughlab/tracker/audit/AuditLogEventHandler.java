package ca.uhnresearch.pughlab.tracker.audit;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;
import ca.uhnresearch.pughlab.tracker.events.RedactedJsonNode;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Handles events and puts them into the audit log.
 * 
 * @author stuartw
 */
public class AuditLogEventHandler implements EventHandler {
	
	/**
	 * Field name for the attribute.
	 */
	private static final String FIELD = "field";
	
	/**
	 * Field name for the case identifier.
	 */
	private static final String CASE_ID = "case_id";
	
	/**
	 * field name for the study identifier.
	 */
	private static final String STUDY_ID = "study_id";
	
	/**
	 * The {@link AuditLogRepository}, which will be injected.
	 */
	private AuditLogRepository repository;

	/**
	 * Handles a message by writing a new audit log record.
	 * @param event the event
	 */
	@Override
	public void sendMessage(Event event) {
		
		// Strip redaction here, so we write clear. There's no need to redact when writing to the
		// audit log, although it's handy for scripting and event handling in other forms, like
		// socket data.
		final ObjectNode parameters = 
						 RedactedJsonNode.redactedToClear(event.getData().getParameters());
		
		final AuditLogRecord record = new AuditLogRecord();
		if (parameters.has(STUDY_ID)) {
			record.setStudyId(parameters.get(STUDY_ID).asInt());
		}
		if (parameters.has(CASE_ID)) {
			record.setCaseId(parameters.get(CASE_ID).asInt());
		}
		if (parameters.has(FIELD)) {
			record.setAttribute(parameters.get(FIELD).asText());
		}
		record.setEventType(event.getType());
		record.setEventUser(event.getData().getUser());
		record.setEventArgs(parameters.toString());
		repository.writeAuditLogRecord(record);
	}

	/**
	 * Retrieves the audit log repository.
	 * @return the repository
	 */
	public AuditLogRepository getRepository() {
		return repository;
	}

	/**
	 * Sets the audit log repository.
	 * @param r the repository to set
	 */
	public void setRepository(AuditLogRepository r) {
		this.repository = r;
	}

}
