package ca.uhnresearch.pughlab.tracker.validation;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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

public class DateValueValidatorTest {

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	DateValueValidator validator = null;
	ViewAttributes va = null;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		validator = new DateValueValidator();
		va = new ViewAttributes();
	}
	
	@Test
	public void testNotApplicable() throws InvalidValueException, ParseException {
		ViewAttributes va = new ViewAttributes();
		JsonNode value = SpecialValues.NOT_AVAILABLE;
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertTrue(validated.getNotAvailable());
	}


	@Test
	public void testDate() throws InvalidValueException, ParseException {
		ViewAttributes va = new ViewAttributes();
		JsonNode value = jsonNodeFactory.textNode("2015-08-22");
		
		WritableValue validated = validator.validate(va, value);
		Assert.assertNotNull(validated);
		Assert.assertFalse(validated.getNotAvailable());
		
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
		Date expected = new Date(parser.parse("2015-08-22").getTime());
		Assert.assertEquals(expected, validated.getValue());
	}

	@Test
	public void testInvalidType() throws InvalidValueException {
		ViewAttributes va = new ViewAttributes();
		JsonNode value = jsonNodeFactory.numberNode(1.0);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		validator.validate(va, value);
	}

	@Test
	public void testInvalidDateSyntax() throws InvalidValueException, ParseException {
		ViewAttributes va = new ViewAttributes();
		JsonNode value = jsonNodeFactory.textNode("915-08-22");
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		validator.validate(va, value);
	}

	@Test
	public void testInvalidDate() throws InvalidValueException, ParseException {
		ViewAttributes va = new ViewAttributes();
		JsonNode value = jsonNodeFactory.textNode("2015-08-35");
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		validator.validate(va, value);
	}
}
