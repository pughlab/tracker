package ca.uhnresearch.pughlab.tracker.validation;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

public class StringValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(String.class, true, null);
		}

		if (! value.isNull() && ! value.isTextual()) {
			throw new InvalidValueException("Invalid string value: " + value.toString());
		}
		
		// There remains the issue of what to write when the value is a null. Make it null. 
		
		String finalValue = value.isNull() ? null : value.asText();
		return new WritableValue(String.class, false, finalValue);
	}
}
