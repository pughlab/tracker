package ca.uhnresearch.pughlab.tracker.domain;

import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

public interface ValueValidator {
	
	public WritableValue validate(ViewAttributes a, JsonNode value) throws InvalidValueException;

}
