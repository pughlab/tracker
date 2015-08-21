package ca.uhnresearch.pughlab.tracker.dao;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpecialValues {
		
	public static final ObjectNode NOT_AVAILABLE = JsonNodeFactory.instance.objectNode().put("$notAvailable", Boolean.TRUE);

}
