package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;

public class CaseObjectBuilder {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	private static ObjectMapper objectMapper = new ObjectMapper();

	private Map<Integer, ObjectNode> table = new HashMap<Integer, ObjectNode>();

	private List<ObjectNode> objects = new ArrayList<ObjectNode>();

	private JsonNode getNotAvailableValue() {
		ObjectNode marked = jsonNodeFactory.objectNode();
		marked.put("$notAvailable", Boolean.TRUE);
		return marked;
	}
	
	public CaseObjectBuilder(List<Integer> caseIds) {
		objects.clear();
		
		Integer index = 0;
		for(Integer id : caseIds) {
			ObjectNode obj = jsonNodeFactory.objectNode();
			objects.add(index++, obj);
			table.put(id, obj);
		}
	}
	
	public List<ObjectNode> getCaseObjects() {
		return objects;
	}
	
	/**
	 * Writes values from a set of data tuples retrieved with a very specific order within getData,
	 * and wires them into JSON values for returning. 
	 * @param table
	 * @param values
	 */
	public void addTupleAttributes(List<Tuple> values) {
		for(Tuple v : values) {
			Integer caseId = v.get(0, Integer.class);
			String attributeName = v.get(1, String.class);
			Object value = v.get(2, Object.class);
			Boolean notAvailable = v.get(3, Boolean.class);
			String notes = v.get(4, String.class);
			
			ObjectNode obj = table.get(caseId);
			
			// Add the case identifier
			obj.put("id", caseId);
			
			// Add the value
			if (notAvailable != null && notAvailable) {
				obj.replace(attributeName, getNotAvailableValue());
			} else if (value == null) {
				obj.put(attributeName, (String) null);
			} else if (value instanceof String) {
				obj.put(attributeName, (String) value);
			} else if (value instanceof Date) {
				obj.put(attributeName, ((Date) value).toString());
			} else if (value instanceof Boolean) {
				obj.put(attributeName, (Boolean) value);
			} else {
				throw new RuntimeException("Invalid attribute type: " + value.getClass().getCanonicalName());
			}
			
			// If we have notes, we need to add them. They are added in a per-record
			// holder attribute, $notes, and need to be decoded from JSON. 
			if (notes != null) {
				JsonNode notesNode = null;
				try {
					notesNode = objectMapper.readTree(notes);
				} catch (Exception e) {
					throw new RuntimeException("Invalid JSON: " + e.getMessage() + ": " + notes);
				}
				
				ObjectNode recordNotesNode;
				
				if (! obj.has("$notes")) {
					recordNotesNode = jsonNodeFactory.objectNode();
					obj.set("$notes", recordNotesNode);
				} else {
					recordNotesNode = (ObjectNode) obj.get("$notes");
				}
				recordNotesNode.set(attributeName, notesNode);
			}
		}
	}
}
