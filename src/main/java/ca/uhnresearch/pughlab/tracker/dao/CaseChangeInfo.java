package ca.uhnresearch.pughlab.tracker.dao;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class CaseChangeInfo {
	
	public CaseChangeInfo(Integer caseId) {
		super();
		this.caseId = caseId;
	}

	private Integer caseId;
	
	private Map<String, Change> changes = new HashMap<String, Change>();
	
	public void addValueChange(String field, JsonNode oldValue, JsonNode newValue) {
		changes.put(field, new Change(oldValue, newValue));
	}
	
	public Iterable<String> fields() {
		return changes.keySet();
	}
	
	public Change getChange(String field) {
		return changes.get(field);
	}
	
	public class Change {
		
		JsonNode oldValue;
		JsonNode newValue;
		
		private Change(JsonNode oldValue, JsonNode newValue) {
			this.newValue = newValue;
			this.oldValue = oldValue;
		}
		
		/**
		 * @return the oldValue
		 */
		public JsonNode getOldValue() {
			return oldValue;
		}
		/**
		 * @return the newValue
		 */
		public JsonNode getNewValue() {
			return newValue;
		};
	}

	/**
	 * @return the caseId
	 */
	public Integer getCaseId() {
		return caseId;
	}
}
