package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple recursive descent parser used to generate a query expression.
 * For convenience, we use tokens both for AST type nodes and for lexical
 * tokens. The grammar is essentially as follows.
 * <p>
 * query ::= term (term | operator term)* 
 * <p>
 * term ::= token | '(' query ')'
 * <p>
 * The most non-obvious feature is the "implied or" feature, in that if 
 * two terms are entered, it behaves like a kind of implicit or
 * operator. 
 * <p>
 * Precedence on operators is not implemented.
 */
public class Parser {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Tokenizer input;

	public Parser(Reader input) {
		super();
		this.input = new Tokenizer(input);
	}
	
	private Token token;
	
	private void skipToken() throws IOException, InvalidTokenException {
		logger.error("Currently on: {} - skipping", token);
		token = input.getNextToken();
	}
	
	private Token getNextToken() {
		logger.error("Next token: {}", token);
		return token;
	}
	
	public QueryNode parse() throws IOException, InvalidTokenException {
		skipToken();
		return parseQuery();
	}
	
	public QueryNode parseQuery() throws IOException, InvalidTokenException {
		
		logger.error("> parseQuery");

		QueryNode term = parseTerm();
		
		while(true) {
			Token operator = getNextToken();
			
			if (operator == null) {
				
				break;
				
			} else if (OperatorToken.isInfixOperator(operator.getValue())) {
				
				skipToken();
				term = new ExpressionNode(term, operator, parseTerm());
				
			} else if (OperatorToken.isOperator(operator.getValue())) {
				
				break;
				
			} else {
				
				// Implied comma handling
				QueryNode other = parseTerm();
				if (other != null) {
					term = new ExpressionNode(term, OperatorToken.OPERATOR_IMPLIED, other);
				}				
			}
		}
		
		logger.error("< parseQuery");
		
		return term;
	}

	public QueryNode parseTerm() throws IOException, InvalidTokenException {

		logger.error("> parseTerm");

		QueryNode next = getNextToken();

		while(next != null) {
			if (next.equals(OperatorToken.OPERATOR_LEFT_PARENTHESIS)) {
				
				skipToken();
				QueryNode left = parseQuery();
				
				next = getNextToken();
				if (next == null || ! next.equals(OperatorToken.OPERATOR_RIGHT_PARENTHESIS)) {
					throw new InvalidTokenException("Missing right parenthesis");
				}
				skipToken();
				next = left;
				break;
			} else {

				skipToken();
				break;
			}
		}
		
		logger.error("< parseTerm");
		
		return next;
	}
}
