package ca.uhnresearch.pughlab.tracker.validation;

import static org.hamcrest.Matchers.containsString;

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

public class StringValueValidatorTest {

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	StringValueValidator validator = null;
	ViewAttributes va = null;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		validator = new StringValueValidator();
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
	public void testString() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.textNode("Hello");
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertFalse(validated.getNotAvailable());
		Assert.assertEquals("Hello", validated.getValue());
	}

	@Test
	public void testInvalid() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.booleanNode(true);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		validator.validate(va, value);
	}

}
