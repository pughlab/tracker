package ca.uhnresearch.pughlab.tracker.validation;

import java.util.Iterator;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

public class OptionValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(String.class, true, null);
		}

		if (a.getOptions() == null || ! a.getOptions().has("values") || ! a.getOptions().get("values").isArray()) {
			throw new InvalidValueException("No option values specified: " + a.getName());
		}
		
		if (! value.isNull()) {
    		Boolean found = false;
    		Iterator<JsonNode> elements = a.getOptions().get("values").elements();
    		while(elements.hasNext()) {
    			if (elements.next().equals(value)) {
    				found = true;
    				break;
    			}
    		}
    		if (! found) {
    			throw new InvalidValueException("Invalid string value: " + value.toString());
    		}
		}

		String finalValue = value.isNull() ? null : value.asText();
		return new WritableValue(String.class, false, finalValue);
	}
}
