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
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader(""));
		Token token = tokenizer.getNextToken();
		Assert.assertNull(token);
	}

	@Test
	public void testEmptyStringWithSpaces() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("  \t\r\n"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof WhitespaceToken);
		Assert.assertEquals(5, token.getValue().length());
	}

	@Test
	public void testOpenParenToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("()"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("(", token.getValue());
	}

	@Test
	public void testNAToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("NA"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof ValueToken);
		Assert.assertEquals("NA", token.getValue());
	}

	@Test
	public void testWhitespacePrefixedAndToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader(" AND"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof WhitespaceToken);
		Assert.assertEquals(1, token.getValue().length());
		token = tokenizer.getNextToken();
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("AND", token.getValue());
	}

	@Test
	public void testValueToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("ANDY  "));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof ValueToken);
		Assert.assertEquals("ANDY", token.getValue());
	}

	@Test
	public void testQuotedToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("\"ANDY \"  "));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof QuotedStringToken);
		Assert.assertEquals("\"ANDY \"", token.getValue());
	}

	@Test
	public void testQuotedTokenError() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("\"ANDY  "));
		
		thrown.expect(InvalidTokenException.class);
		thrown.expectMessage(containsString("Missing end quote"));

		tokenizer.getNextToken();
	}
	
	private List<Token> getTokens(SimpleTokenizer tokenizer) throws IOException, InvalidTokenException {
		List<Token> result = new ArrayList<Token>();
		Token token;
		while((token = tokenizer.getNextToken()) != null) {
			result.add(token);
		}

		return result;
	}
	
	@Test
	public void testMultipleTokens1() throws IOException, InvalidTokenException {
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("KRAS OR TP53  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(6, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(2) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(4) instanceof ValueToken);
	}

	@Test
	public void testMultipleTokens2() throws IOException, InvalidTokenException {
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("KRAS* OR *P53*  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(6, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(2) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(4) instanceof ValueToken);
	}

	@Test
	public void testMultipleTokens3() throws IOException, InvalidTokenException {
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("(\"KRAS*\" OR NA OR \"\")  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(12, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(1) instanceof QuotedStringToken);
		Assert.assertTrue(tokens.get(3) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(5) instanceof ValueToken);
		Assert.assertTrue(tokens.get(7) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(9) instanceof QuotedStringToken);
		Assert.assertTrue(tokens.get(10) instanceof OperatorToken);
	}
	
	@Test
	public void testMultipleTokens4() throws IOException, InvalidTokenException {
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("KRAS, TP53  "));
		List<Token> tokens = getTokens(tokenizer);
		Assert.assertEquals(5, tokens.size());
		Assert.assertTrue(tokens.get(0) instanceof ValueToken);
		Assert.assertTrue(tokens.get(1) instanceof OperatorToken);
		Assert.assertTrue(tokens.get(3) instanceof ValueToken);
	}
	
	@Test
	public void testBeforeToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("BEFORE"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("BEFORE", token.getValue());
		Assert.assertTrue(OperatorToken.isPrefixOperator(token.getValue()));
	}

	@Test
	public void testAfterToken() throws IOException, InvalidTokenException {
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(new StringReader("AFTER"));
		Token token = tokenizer.getNextToken();
		Assert.assertNotNull(token);
		Assert.assertTrue(token instanceof OperatorToken);
		Assert.assertEquals("AFTER", token.getValue());
		Assert.assertTrue(OperatorToken.isPrefixOperator(token.getValue()));
	}
}
