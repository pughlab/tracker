package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseObjectBuilder {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	private static ObjectMapper objectMapper = new ObjectMapper();

	private Map<Integer, ObjectNode> table = new HashMap<Integer, ObjectNode>();

	private List<ObjectNode> objects = new ArrayList<ObjectNode>();
	
	private boolean attributeNameFilter = false;
	private Set<String> attributeNameSet = new HashSet<String>();
	
	public void setAttributeNameFilter(List<String> attributeNames) {
		for(String attribute : attributeNames) {
			attributeNameSet.add(attribute);
		}
		attributeNameFilter = true;
	}
	
	private boolean attributeNameIncluded(String name) {
		return (! attributeNameFilter) || attributeNameSet.contains(name);
	}

	private JsonNode getNotAvailableValue() {
		final ObjectNode marked = jsonNodeFactory.objectNode();
		marked.put("$notAvailable", Boolean.TRUE);
		return marked;
	}
	
	public CaseObjectBuilder(List<CaseInfo> caseInfos) {
		objects.clear();
		
		Integer index = 0;
		for(CaseInfo info : caseInfos) {
			final ObjectNode obj = jsonNodeFactory.objectNode();
			obj.put("$state", info.getState());
			obj.put("$guid", info.getGuid());
			obj.put("id", info.getId());
			objects.add(index++, obj);
			table.put(info.getId(), obj);
		}
	}
	
	public List<ObjectNode> getCaseObjects() {
		return objects;
	}
	
	/**
	 * Writes values from a set of data tuples retrieved with a very specific order within getData,
	 * and wires them into JSON values for returning. 
	 * @param values
	 */
	public void addTupleAttributes(List<Tuple> values) {
		for(Tuple v : values) {
			final Integer caseId = v.get(0, Integer.class);
			final String attributeName = v.get(1, String.class);
			final Object value = v.get(2, Object.class);
			final Boolean notAvailable = v.get(3, Boolean.class);
			final String notes = v.get(4, String.class);
			
			final ObjectNode obj = table.get(caseId);
			
			// Add the case identifier
			obj.put("id", caseId);
			
			if (! attributeNameIncluded(attributeName)) {
				continue;
			}
			
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
			} else if (value instanceof Double) {
				obj.put(attributeName, (Double) value);
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
