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

/**
 * Builder class to translate a set of case objects into populated values that can be 
 * returned. This includes applying an attribute-level filter so that confidential
 * information is not passed back to the front end. 
 * 
 * @author stuartw
 */
public class CaseObjectBuilder {

	/**
	 * A logger.
	 */
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Factory to make new JSON nodes.
	 */
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	/**
	 * The mapper to translate from JSON.
	 */
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * A table mapping internal identities with object nodes.
	 */
	private Map<Integer, ObjectNode> table = new HashMap<Integer, ObjectNode>();

	/**
	 * The list of objects being built.
	 */
	private List<ObjectNode> objects = new ArrayList<ObjectNode>();
	
	/**
	 * True if an attribute name filter is being applied.
	 */
	private boolean attributeNameFilter = false;
	
	/**
	 * The attribute name set.
	 */
	private Set<String> attributeNameSet = new HashSet<String>();
	
	/**
	 * Sets an attribute name filter
	 * @param attributeNames the list of attribute names
	 */
	public void setAttributeNameFilter(List<String> attributeNames) {
		for(String attribute : attributeNames) {
			attributeNameSet.add(attribute);
		}
		attributeNameFilter = true;
	}
	
	/**
	 * Tests if an attribute name matches the filter.
	 * @param name the attribute name
	 * @return true if this attribute is not filtered
	 */
	private boolean attributeNameIncluded(String name) {
		return (! attributeNameFilter) || attributeNameSet.contains(name);
	}

	/**
	 * Generates an N/A value.
	 * @return the new N/A value
	 */
	private JsonNode getNotAvailableValue() {
		final ObjectNode marked = jsonNodeFactory.objectNode();
		marked.put("$notAvailable", Boolean.TRUE);
		return marked;
	}
	
	/**
	 * Makes a new builder for a set of {@link CaseInfo} records.
	 * @param caseInfos
	 */
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
	
	/**
	 * Returns the list of case objects
	 * @return the case objects
	 */
	public List<ObjectNode> getCaseObjects() {
		return objects;
	}
	
	/**
	 * Add the value into the returned object
	 * @param obj the object
	 * @param attributeName the attribute
	 * @param notAvailable is this N/A
	 * @param value the value
	 */
	private void addValue(ObjectNode obj, String attributeName, Boolean notAvailable, Object value) {
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
	}
	
	/**
	 * Add notes into the returned object value.
	 * @param obj the object
	 * @param attributeName the attribute
	 * @param notes the notes
	 */
	private void addNotes(ObjectNode obj, String attributeName, String notes) {
		if (notes == null) return;
		
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
			
			addValue(obj, attributeName, notAvailable, value);
			addNotes(obj, attributeName, notes);
		}
	}
}
