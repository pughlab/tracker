package ca.uhnresearch.pughlab.tracker.validation;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.databind.JsonNode;

public class BooleanValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(ViewAttributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(Boolean.class, true, null);
		}
		
		if (! value.isNull() && ! value.isBoolean()) {
			throw new InvalidValueException("Invalid boolean value: " + value.toString());
		}
		Boolean finalValue = value.isNull() ? null : value.asBoolean();
		return new WritableValue(Boolean.class, false, finalValue);
	}
}
