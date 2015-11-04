package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.Reader;

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
	
	private final Tokenizer input;

	public Parser(Reader input) {
		super();
		this.input = new Tokenizer(input);
	}
	
	private Token token;
	
	private void skipToken() throws IOException, InvalidTokenException {
		token = input.getNextToken();
	}
	
	private Token getNextToken() {
		return token;
	}
	
	public QueryNode parse() throws IOException, InvalidTokenException {
		skipToken();
		return parseQuery();
	}
	
	public QueryNode parseQuery() throws IOException, InvalidTokenException {

		QueryNode term = parseTerm();
		skipToken();
		
		while(true) {
			Token operator = getNextToken();
			if (OperatorToken.OPERATOR_COMMA.equals(operator)) {
				skipToken();
				term = new ExpressionNode(term, operator, parseTerm());
				skipToken();
			} else {
				break;
			}
		}
		
		return term;
	}

	public QueryNode parseTerm() throws IOException, InvalidTokenException {
		
		QueryNode next = getNextToken();
		
		while(next != null) {
			if (next.equals(OperatorToken.OPERATOR_LEFT_PARENTHESIS)) {
				
				skipToken();
				QueryNode left = parseQuery();
				
				next = getNextToken();
				if (! next.equals(OperatorToken.OPERATOR_RIGHT_PARENTHESIS)) {
					throw new InvalidTokenException("Missing right parenthesis");
				}
				
				return left;
			} else {
				break;
			}
		}
		
		return next;
	}
}
