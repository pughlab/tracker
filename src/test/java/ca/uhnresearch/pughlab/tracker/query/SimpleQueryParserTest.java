package ca.uhnresearch.pughlab.tracker.query;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleQueryParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(""));
		QueryNode node = parser.parse();
		Assert.assertNull(node);
	}

	@Test
	public void testNAString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader("NA"));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("NA", ((Token) node).getValue());
	}

	@Test
	public void testSimpleQueryString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader("KRAS"));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("KRAS", ((Token) node).getValue());
	}

	@Test
	public void testWildcardQueryString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader("*KRAS*"));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ValueToken);
		Assert.assertEquals("*KRAS*", ((Token) node).getValue());
	}

	@Test
	public void testQuotedWildcardQueryString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" \"*KRAS*\" "));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof QuotedStringToken);
		Assert.assertEquals("\"*KRAS*\"", ((Token) node).getValue());
	}

	@Test
	public void testCommaExpressionQueryString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" KRAS, TP53 "));
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
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" KRAS AND TP53 "));
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
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" (KRAS AND TP53) "));
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
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" (KRAS AND TP53 "));
		
		thrown.expect(InvalidTokenException.class);
		thrown.expectMessage(containsString("Missing right parenthesis"));

		parser.parse();
	}
	
	@Test
	public void testImpliedCommaExpressionQueryString() throws IOException, InvalidTokenException {
		
		SimpleQueryParser parser = new SimpleQueryParser(new StringReader(" KRAS TP53 "));
		QueryNode node = parser.parse();
		Assert.assertNotNull(node);
		Assert.assertTrue(node instanceof ExpressionNode);
		ExpressionNode expression = (ExpressionNode) node;
		
		Assert.assertTrue(expression.getOperandLeft() instanceof ValueToken);
		Assert.assertTrue(expression.getOperandRight() instanceof ValueToken);
		Assert.assertTrue(expression.getOperator() instanceof OperatorToken);
		Assert.assertTrue(expression.getOperator().equals(OperatorToken.OPERATOR_IMPLIED));
	}
}
