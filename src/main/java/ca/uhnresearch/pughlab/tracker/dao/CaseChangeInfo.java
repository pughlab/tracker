package ca.uhnresearch.pughlab.tracker.dao;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class CaseChangeInfo {
	
	/**
	 * Constructs a new case change object.
	 * @param caseId the case id
	 */
	public CaseChangeInfo(Integer caseId) {
		super();
		this.caseId = caseId;
	}

	private Integer caseId;
	
	private Map<String, Change> changes = new HashMap<String, Change>();
	
	/**
	 * Adds a value change to the case change record.
	 * @param field the field
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	public void addValueChange(String field, JsonNode oldValue, JsonNode newValue) {
		changes.put(field, new Change(oldValue, newValue));
	}
	
	/**
	 * Returns an iterable on the fields.
	 * @return an iterable of field string names
	 */
	public Iterable<String> fields() {
		return changes.keySet();
	}
	
	/**
	 * Retrieves a specific change, located by field name.
	 * @param field the field name
	 * @return a change 
	 */
	public Change getChange(String field) {
		return changes.get(field);
	}
	
	/**
	 * An object for an individual change.
	 */
	public class Change {
		
		JsonNode oldValue;
		JsonNode newValue;
		
		private Change(JsonNode oldVal, JsonNode newVal) {
			this.newValue = newVal;
			this.oldValue = oldVal;
		}
		
		/**
		 * Retrieves the old value.
		 * @return the oldValue
		 */
		public JsonNode getOldValue() {
			return oldValue;
		}
		/**
		 * Retrieves the new value.
		 * @return the newValue
		 */
		public JsonNode getNewValue() {
			return newValue;
		};
	}

	/**
	 * Retrieves the case identifier.
	 * @return the caseId
	 */
	public Integer getCaseId() {
		return caseId;
	}
}
