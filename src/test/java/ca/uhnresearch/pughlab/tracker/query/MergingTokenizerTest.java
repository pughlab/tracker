package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

public class MergingTokenizerTest {

	private List<Token> getTokens(String value) throws IOException, InvalidTokenException {
		Tokenizer tokenizer = new MergingTokenizer(new SimpleTokenizer(new StringReader(value)));
		List<Token> result = new ArrayList<Token>();
		Token token;
		while((token = tokenizer.getNextToken()) != null) {
			result.add(token);
		}

		return result;
	}
	

	@Test
	public void testMerging() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("A BC");
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("A BC", tokens.get(0).getValue());
	}

	@Test
	public void testMergingLong() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("A BC D E FG");
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("A BC D E FG", tokens.get(0).getValue());
	}

	@Test
	public void testMergingStops() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("A BC AND E FG");
		Assert.assertEquals(3, tokens.size());
		Assert.assertEquals("A BC", tokens.get(0).getValue());
		Assert.assertEquals("AND", tokens.get(1).getValue());
		Assert.assertEquals("E FG", tokens.get(2).getValue());
	}

	@Test
	public void testMergingParentheses() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("A BC(D E F)");
		Assert.assertEquals(4, tokens.size());
		Assert.assertEquals("A BC", tokens.get(0).getValue());
		Assert.assertEquals("(", tokens.get(1).getValue());
		Assert.assertEquals("D E F", tokens.get(2).getValue());
		Assert.assertEquals(")", tokens.get(3).getValue());
	}

	@Test
	public void testMergingWithPeriod() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("Dr. X");
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("Dr. X", tokens.get(0).getValue());
	}

	@Test
	public void testMergingWithMultipleSpaces() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("Dr.  X");
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("Dr.  X", tokens.get(0).getValue());
	}

	@Test
	public void testMergingWitPrefixSpaces() throws IOException, InvalidTokenException {
		
		List<Token> tokens = getTokens("  Dr. X");
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("Dr. X", tokens.get(0).getValue());
	}
}
