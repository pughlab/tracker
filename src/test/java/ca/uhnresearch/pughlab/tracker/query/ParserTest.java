package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyString() throws IOException, InvalidTokenException {
		
		Parser parser = new Parser(new StringReader(""));
		QueryNode node = parser.parse();
		Assert.assertNull(node);
	}

	@Test
	public void testNAString() throws IOException, InvalidTokenException {
		
		Parser parser = new Parser(new StringReader("NA"));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("NA", ((Token) node).getValue());
	}
}
