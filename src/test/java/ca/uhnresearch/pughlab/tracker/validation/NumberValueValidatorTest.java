package ca.uhnresearch.pughlab.tracker.validation;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.SpecialValues;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class NumberValueValidatorTest {

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	NumberValueValidator validator = null;
	ViewAttributes va = null;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		validator = new NumberValueValidator();
		va = new ViewAttributes();
	}

	@Test
	public void testNotApplicable() throws InvalidValueException, ParseException {
		JsonNode value = SpecialValues.NOT_AVAILABLE;
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertTrue(validated.getNotAvailable());
	}

	@Test
	public void testNumber() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.numberNode(1);
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertFalse(validated.getNotAvailable());
		Assert.assertEquals(new Double(1.0), validated.getValue());
	}

	@Test
	public void testInvalid() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.textNode("Hello");
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid number"));

		validator.validate(va, value);
	}
}
