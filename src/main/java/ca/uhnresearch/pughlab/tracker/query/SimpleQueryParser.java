package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple recursive descent parser used to generate a query expression.
 * For convenience, we use tokens both for AST type nodes and for lexical
 * tokens. The grammar is essentially as follows.
 * <p>
 * query ::= term (infix_operator term)* 
 * <p>
 * term ::= value | '(' query ')' | prefix_operator value
 * <p>
 * value ::= token (WS token)*
 * <p>
 * Precedence on operators is not implemented, except through the syntax
 * attaching different types of operators at different rules. 
 */
public class SimpleQueryParser implements QueryParser {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Tokenizer reader = null;
	
	public SimpleQueryParser(Tokenizer input) {
		super();
		this.reader = input;
	}
	
	private Token token;
	
	private void skipToken() throws IOException, InvalidTokenException {
		logger.trace("Currently on: {} - skipping", token);
		token = reader.getNextToken();
	}
	
	private Token getNextToken() {
		logger.trace("Next token: {}", token);
		return token;
	}
	
	public QueryNode parse() throws IOException, InvalidTokenException {
		skipToken();
		return parseQuery();
	}
	
	public QueryNode parseQuery() throws IOException, InvalidTokenException {
		
		logger.trace("> parseQuery");

		QueryNode term = parseTerm();
		
		while(true) {
			Token operator = getNextToken();
			
			if (operator == null) {
				
				break;
				
			} else if (OperatorToken.isInfixOperator(operator.getValue())) {
				
				skipToken();
				term = new ExpressionNode(term, operator, parseTerm());
				
			} else {
				
				break;
				
			}
		}
		
		logger.trace("< parseQuery");
		
		return term;
	}

	public QueryNode parseTerm() throws IOException, InvalidTokenException {

		logger.trace("> parseTerm");

		QueryNode next = getNextToken();
		
		if (next == null) {
			return next;
		}
		
		if (! (next instanceof OperatorToken)) {
			skipToken();
			return next;
		}
		
		final OperatorToken nextOperator = (OperatorToken) next;
		
		if (nextOperator.equals(OperatorToken.OPERATOR_LEFT_PARENTHESIS)) {
			
			skipToken();
			final QueryNode left = parseQuery();
			
			next = getNextToken();
			if (next == null || ! next.equals(OperatorToken.OPERATOR_RIGHT_PARENTHESIS)) {
				throw new InvalidTokenException("Missing right parenthesis");
			}
			skipToken();
			return left;

		} else if (OperatorToken.isPrefixOperator(nextOperator.getValue())) {
			
			skipToken();
			final QueryNode right = getNextToken();
			
			if (right == null) {
				throw new InvalidTokenException("Missing value for: " + nextOperator.getValue());
			}

			skipToken();
			return new ExpressionNode(null, nextOperator, right);
			
		} else {
			
			throw new InvalidTokenException("Unexpected operator: " + nextOperator.getValue());
		}
	}

	/**
	 * @return the reader
	 */
	public Tokenizer getReader() {
		return reader;
	}

	/**
	 * @param reader the reader to set
	 */
	public void setReader(Tokenizer reader) {
		this.reader = reader;
	}
}
