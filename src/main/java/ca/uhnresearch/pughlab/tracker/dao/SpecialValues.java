package ca.uhnresearch.pughlab.tracker.dao;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Placeholder class for special values.
 * 
 * @author stuartw
 */
public class SpecialValues {
		
	/**
	 * An object which represents N/A.
	 */
	public static final ObjectNode NOT_AVAILABLE = 
					JsonNodeFactory.instance.objectNode().put("$notAvailable", Boolean.TRUE);

}
