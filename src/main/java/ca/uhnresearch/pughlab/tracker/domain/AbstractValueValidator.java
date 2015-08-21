package ca.uhnresearch.pughlab.tracker.domain;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractValueValidator implements ValueValidator {

	public abstract WritableValue validate(ViewAttributes a, JsonNode value) throws InvalidValueException;
	
	protected boolean isNotAvailable(JsonNode value) {
		return value.isObject() && value.has("$notAvailable");
	}

}
