package ca.uhnresearch.pughlab.tracker.validation;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

public class NumberValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(Double.class, true, null);
		}
		
		if (! value.isNull() && ! value.isNumber()) {
			throw new InvalidValueException("Invalid number value: " + value.toString());
		}
		Double finalValue = value.isNull() ? null : value.asDouble();
		return new WritableValue(Double.class, false, finalValue);
	}
}
