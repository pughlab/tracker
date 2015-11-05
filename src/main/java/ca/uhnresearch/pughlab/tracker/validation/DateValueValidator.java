package ca.uhnresearch.pughlab.tracker.validation;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

public class DateValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(java.sql.Date.class, true, null);
		}

		if (! value.isNull() && ! value.isTextual()) {
			throw new InvalidValueException("Invalid date value: " + value.toString());
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date finalValue = null;
			if (! value.isNull()) {
				String input = value.asText().trim();
				Date parsed = format.parse(input);
				String formatted = format.format(parsed);
				if (! formatted.equals(input)) {
					throw new InvalidValueException("Invalid date value: " + input);
				}
				finalValue = new java.sql.Date(parsed.getTime());
			}
			return new WritableValue(java.sql.Date.class, false, finalValue);
		} catch (ParseException e) {
			throw new InvalidValueException("Invalid date value: " + value.toString());
		}
	}
}
