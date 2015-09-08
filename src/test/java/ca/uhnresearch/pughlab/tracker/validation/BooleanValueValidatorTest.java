package ca.uhnresearch.pughlab.tracker.validation;

import static org.hamcrest.Matchers.containsString;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.SpecialValues;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

public class BooleanValueValidatorTest {
	
	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	BooleanValueValidator validator = null;
	ViewAttributes va = null;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		validator = new BooleanValueValidator();
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
	public void testBoolean() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.booleanNode(true);
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertFalse(validated.getNotAvailable());
		Assert.assertEquals(new Boolean(true), validated.getValue());
	}

	@Test
	public void testInvalid() throws InvalidValueException {
		JsonNode value = jsonNodeFactory.numberNode(1.0);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid boolean"));

		validator.validate(va, value);
	}

}
