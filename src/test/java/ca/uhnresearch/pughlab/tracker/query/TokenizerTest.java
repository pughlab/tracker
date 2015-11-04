package ca.uhnresearch.pughlab.tracker.query;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;

public class TokenizerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyString() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader(""));
		Token token = tokenizer.getNextToken();
		Assert.assertNull(token);
	}

	@Test
	public void testEmptyStringWithSpaces() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader("  \t\r\n"));
		Token token = tokenizer.getNextToken();
		Assert.assertNull(token);
	}

	@Test
	public void testOpenParenToken() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader("  ()"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("(", token.getValue());
	}

	@Test
	public void testNAToken() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader("NA"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof ValueToken);
		Assert.assertEquals("NA", token.getValue());
	}

	@Test
	public void testAndToken() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader(" AND"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("AND", token.getValue());
	}

	@Test
	public void testValueToken() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader(" ANDY  "));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof ValueToken);
		Assert.assertEquals("ANDY", token.getValue());
	}

	@Test
	public void testQuotedToken() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader(" \"ANDY \"  "));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof QuotedStringToken);
		Assert.assertEquals("\"ANDY \"", token.getValue());
	}

	@Test
	public void testQuotedTokenError() throws IOException, InvalidTokenException {
		
		Tokenizer tokenizer = new Tokenizer(new StringReader(" \"ANDY  "));
		
		thrown.expect(InvalidTokenException.class);
		thrown.expectMessage(containsString("Missing end quote"));

		tokenizer.getNextToken();
	}
	
	private List<Token> getTokens(Tokenizer tokenizer) throws IOException, InvalidTokenException {
		List<Token> result = new ArrayList<Token>();
		Token token;
		while((token = tokenizer.getNextToken()) != null) {
			result.add(token);
		}

		return result;
	}
	
	@Test
	public void testMultipleTokens1() throws IOException, InvalidTokenException {
		Tokenizer tokenizer = new Tokenizer(new StringReader(" KRAS OR TP53  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(3, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(1) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(2) instanceof ValueToken);
	}

	@Test
	public void testMultipleTokens2() throws IOException, InvalidTokenException {
		Tokenizer tokenizer = new Tokenizer(new StringReader(" KRAS* OR *P53*  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(3, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(1) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(2) instanceof ValueToken);
	}

	@Test
	public void testMultipleTokens3() throws IOException, InvalidTokenException {
		Tokenizer tokenizer = new Tokenizer(new StringReader(" (\"KRAS*\" OR NA OR \"\")  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(7, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(1) instanceof QuotedStringToken);
		Assert.assertTrue(tokens.get(2) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(3) instanceof ValueToken);
		Assert.assertTrue(tokens.get(4) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(5) instanceof QuotedStringToken);
		Assert.assertTrue(tokens.get(6) instanceof OperatorToken);
	}
	
	@Test
	public void testMultipleTokens4() throws IOException, InvalidTokenException {
		Tokenizer tokenizer = new Tokenizer(new StringReader(" KRAS, TP53  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(3, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(1) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(2) instanceof ValueToken);
	}
}
