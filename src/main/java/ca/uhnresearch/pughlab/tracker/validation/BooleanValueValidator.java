package ca.uhnresearch.pughlab.tracker.validation;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Validator for a boolean field.
 * 
 * @author stuartw
 */
public class BooleanValueValidator extends AbstractValueValidator implements ValueValidator {

	/**
	 * Validates a boolean value field. N/A is allowed, as is a boolean value (true or false)
	 * and these get mapped to null/true/false accordingly.
	 * @param a the attribute
	 * @param value the JSON node
	 * @return the writable value
	 */
	@Override
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(Boolean.class, true, null);
		}
		
		if (! value.isNull() && ! value.isBoolean()) {
			throw new InvalidValueException("Invalid boolean value: " + value.toString());
		}
		final Boolean finalValue = value.isNull() ? null : value.asBoolean();
		return new WritableValue(Boolean.class, false, finalValue);
	}
}
