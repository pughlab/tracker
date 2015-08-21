package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.databind.JsonNode;

public class DateValueValidator extends AbstractValueValidator implements ValueValidator {

	@Override
	public WritableValue validate(ViewAttributes a, JsonNode value) throws InvalidValueException {
		
		if (isNotAvailable(value)) {
			return new WritableValue(java.sql.Date.class, true, null);
		}

		if (! value.isNull() && ! value.isTextual()) {
			throw new InvalidValueException("Invalid date value: " + value.toString());
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			java.sql.Date finalValue = value.isNull() ? null : new Date(format.parse(value.asText()).getTime());
			return new WritableValue(java.sql.Date.class, false, finalValue);
		} catch (ParseException e) {
			throw new InvalidValueException("Invalid date value: " + value.toString());
		}
	}
}
