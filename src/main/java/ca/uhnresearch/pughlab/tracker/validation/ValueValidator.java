package ca.uhnresearch.pughlab.tracker.validation;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

public interface ValueValidator {
	
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException;

}
