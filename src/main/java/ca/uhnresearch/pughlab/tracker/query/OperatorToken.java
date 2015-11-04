package ca.uhnresearch.pughlab.tracker.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OperatorToken extends Token {
	
	public static final OperatorToken OPERATOR_AND = new OperatorToken("AND");

	public static final OperatorToken OPERATOR_IMPLIED = new OperatorToken("");

	public static final OperatorToken OPERATOR_OR = new OperatorToken("OR");
	
	public static final OperatorToken OPERATOR_COMMA = new OperatorToken(",");
	
	public static final OperatorToken OPERATOR_LEFT_PARENTHESIS = new OperatorToken("(");

	public static final OperatorToken OPERATOR_RIGHT_PARENTHESIS = new OperatorToken(")");

	private static final String[] SET_VALUES = new String[] { 
		OPERATOR_AND.getValue(), 
		OPERATOR_OR.getValue(), 
		OPERATOR_COMMA.getValue(), 
		OPERATOR_LEFT_PARENTHESIS.getValue(), 
		OPERATOR_RIGHT_PARENTHESIS.getValue()
	};
	
	private static final String[] SET_INFIX_VALUES = new String[] { 
		OPERATOR_AND.getValue(), 
		OPERATOR_OR.getValue(), 
		OPERATOR_COMMA.getValue()
	};

	private static final Set<String> allOperators = new HashSet<String>(Arrays.asList(SET_VALUES));
	
	private static final Set<String> infixOperators = new HashSet<String>(Arrays.asList(SET_INFIX_VALUES));

	public static final boolean isOperator(String token) {
		return allOperators.contains(token);
	}
	
	public static final boolean isInfixOperator(String token) {
		return infixOperators.contains(token);
	}

	public OperatorToken(String value) {
		super(value);
	}
}
