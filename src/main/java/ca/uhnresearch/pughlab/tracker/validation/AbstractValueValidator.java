package ca.uhnresearch.pughlab.tracker.validation;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Abstract class for all value validators, providing default behaviour 
 * for N/A values.
 * 
 * @author stuartw
 */
public abstract class AbstractValueValidator implements ValueValidator {

	/**
	 * Requires a default validation method, which might throw an
	 * {@link InvalidValueException}.
	 */
	public abstract WritableValue validate(Attributes a, JsonNode value) 
		   throws InvalidValueException;
	
	/**
	 * Checks to see if the value is N/A. 
	 * 
	 * @param value the value
	 * @return true if the value is N/A
	 */
	protected boolean isNotAvailable(JsonNode value) {
		return value.isObject() && value.has("$notAvailable");
	}

}
