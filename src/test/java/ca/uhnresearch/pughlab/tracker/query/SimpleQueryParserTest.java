package ca.uhnresearch.pughlab.tracker.query;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleQueryParserTest {
	
	private QueryParser getParser(String input) throws IOException, InvalidTokenException {
		return new SimpleQueryParser(new MergingTokenizer(new SimpleTokenizer(new StringReader(input))));
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("");
		QueryNode node = parser.parse();
		Assert.assertNull(node);
	}

	@Test
	public void testNAString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("NA");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("NA", ((Token) node).getValue());
	}

	@Test
	public void testSimpleQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("KRAS");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("KRAS", ((Token) node).getValue());
	}

	@Test
	public void testWildcardQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("*KRAS*");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("*KRAS*", ((Token) node).getValue());
	}

	@Test
	public void testQuotedWildcardQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser(" \"*KRAS*\" ");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof QuotedStringToken);
		Assert.assertEquals("\"*KRAS*\"", ((Token) node).getValue());
	}

	@Test
	public void testCommaExpressionQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser(" KRAS, TP53 ");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		ExpressionNode expression = (ExpressionNode) node;
		
		Assert.assertTrue(expression.getOperandLeft() instanceof ValueToken);
		Assert.assertTrue(expression.getOperandRight() instanceof ValueToken);
		Assert.assertTrue(expression.getOperator() instanceof OperatorToken);
		Assert.assertTrue(expression.getOperator().equals(OperatorToken.OPERATOR_COMMA));
	}

	@Test
	public void testAndExpressionQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser(" KRAS AND TP53 ");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		ExpressionNode expression = (ExpressionNode) node;
		
		Assert.assertTrue(expression.getOperandLeft() instanceof ValueToken);
		Assert.assertTrue(expression.getOperandRight() instanceof ValueToken);
		Assert.assertTrue(expression.getOperator() instanceof OperatorToken);
		Assert.assertTrue(expression.getOperator().equals(OperatorToken.OPERATOR_AND));
	}

	@Test
	public void testAndExpressionParenthesesQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("  (KRAS AND TP53) ");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		ExpressionNode expression = (ExpressionNode) node;
		
		Assert.assertTrue(expression.getOperandLeft() instanceof ValueToken);
		Assert.assertTrue(expression.getOperandRight() instanceof ValueToken);
		Assert.assertTrue(expression.getOperator() instanceof OperatorToken);
		Assert.assertTrue(expression.getOperator().equals(OperatorToken.OPERATOR_AND));
	}

	@Test
	public void testAndExpressionParenthesesErrorQueryString() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("  (KRAS AND TP53 ");
		
		thrown.expect(InvalidTokenException.class);
		thrown.expectMessage(containsString("Missing right parenthesis"));

		parser.parse();
	}
	
	@Test
	public void testStringWithSpaces() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("Dr. X");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("Dr. X", ((Token) node).getValue());
	}

	@Test
	public void testBeforeOperator() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("BEFORE 2008-12-01");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		Assert.assertEquals("BEFORE", ((ExpressionNode) node).getOperator().getValue());
		Assert.assertNull(((ExpressionNode) node).getOperandLeft());
		Assert.assertNotNull(((ExpressionNode) node).getOperandRight());
		Assert.assertEquals("BEFORE 2008-12-01", ((ExpressionNode) node).toString());
	}

	@Test
	public void testAfterOperator() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("AFTER 2008-12-01");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		Assert.assertEquals("AFTER", ((ExpressionNode) node).getOperator().getValue());
		Assert.assertNull(((ExpressionNode) node).getOperandLeft());
		Assert.assertNotNull(((ExpressionNode) node).getOperandRight());
		Assert.assertEquals("AFTER 2008-12-01", ((ExpressionNode) node).toString());
	}

	@Test
	public void testCombinedDateOperator() throws IOException, InvalidTokenException {
		
		QueryParser parser = getParser("BEFORE 2010-12-01 AND AFTER 2008-12-01");
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		Assert.assertEquals("BEFORE 2010-12-01 AND AFTER 2008-12-01", ((ExpressionNode) node).toString());
	}
}
