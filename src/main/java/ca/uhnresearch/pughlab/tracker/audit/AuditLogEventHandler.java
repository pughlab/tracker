package ca.uhnresearch.pughlab.tracker.audit;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;
import ca.uhnresearch.pughlab.tracker.events.RedactedJsonNode;

public class AuditLogEventHandler implements EventHandler {
	
	private AuditLogRepository repository;

	/**
	 * Handles a message by writing a new audit log record.
	 * @param event the event
	 * @param scope the event scope
	 */
	@Override
	public void sendMessage(Event event, String scope) {
		
		// Strip redaction here, so we write clear. There's no need to redact when writing to the
		// audit log, although it's handy for scripting and event handling in other forms, like
		// socket data.
		final ObjectNode parameters = RedactedJsonNode.redactedToClear(event.getData().getParameters());
		
		AuditLogRecord record = new AuditLogRecord();
		if (parameters.has("study_id")) {
			record.setStudyId(parameters.get("study_id").asInt());
		}
		if (parameters.has("case_id")) {
			record.setCaseId(parameters.get("case_id").asInt());
		}
		if (parameters.has("field")) {
			record.setAttribute(parameters.get("field").asText());
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
	 * @param repository the repository to set
	 */
	public void setRepository(AuditLogRepository repository) {
		this.repository = repository;
	}

}
