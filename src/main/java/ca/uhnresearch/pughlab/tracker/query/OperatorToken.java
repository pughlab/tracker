package ca.uhnresearch.pughlab.tracker.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A token that represents an operator.
 * 
 * @author stuartw
 */
public class OperatorToken extends Token {
	
	/**
	 * The operator for a boolean and. 
	 */
	public static final OperatorToken OPERATOR_AND = new OperatorToken("AND");

	/**
	 * The operator for a boolean or. 
	 */
	public static final OperatorToken OPERATOR_OR = new OperatorToken("OR");
	
	/**
	 * The operator for a comma, whatever that is mapped to. 
	 */
	public static final OperatorToken OPERATOR_COMMA = new OperatorToken(",");
	
	/**
	 * The operator for a left parenthesis. 
	 */
	public static final OperatorToken OPERATOR_LEFT_PARENTHESIS = new OperatorToken("(");

	/**
	 * The operator for a right parenthesis. 
	 */
	public static final OperatorToken OPERATOR_RIGHT_PARENTHESIS = new OperatorToken(")");

	/**
	 * The operator for a before date operator. 
	 */
	public static final OperatorToken OPERATOR_BEFORE = new OperatorToken("BEFORE");

	/**
	 * The operator for an after date operator. 
	 */
	public static final OperatorToken OPERATOR_AFTER = new OperatorToken("AFTER");

	private static final String[] SET_VALUES = new String[] { 
		OPERATOR_AND.getValue(), 
		OPERATOR_OR.getValue(), 
		OPERATOR_COMMA.getValue(), 
		OPERATOR_BEFORE.getValue(), 
		OPERATOR_AFTER.getValue(),
		OPERATOR_LEFT_PARENTHESIS.getValue(), 
		OPERATOR_RIGHT_PARENTHESIS.getValue()
	};
	
	private static final String[] SET_INFIX_VALUES = new String[] { 
		OPERATOR_AND.getValue(), 
		OPERATOR_OR.getValue(), 
		OPERATOR_COMMA.getValue()
	};

	private static final String[] SET_PREFIX_VALUES = new String[] { 
		OPERATOR_BEFORE.getValue(), 
		OPERATOR_AFTER.getValue(),
	};

	private static final Set<String> allOperators = new HashSet<String>(Arrays.asList(SET_VALUES));
	
	private static final Set<String> infixOperators = new HashSet<String>(Arrays.asList(SET_INFIX_VALUES));

	private static final Set<String> prefixOperators = new HashSet<String>(Arrays.asList(SET_PREFIX_VALUES));

	/**
	 * Returns true if this string corresponds to an operator.
	 * @param token
	 * @return true if this is an operator
	 */
	public static final boolean isOperator(String token) {
		return allOperators.contains(token);
	}
	
	/**
	 * Returns true if this string corresponds to an infix operator.
	 * @param token
	 * @return true if this is an infix operator
	 */
	public static final boolean isInfixOperator(String token) {
		return infixOperators.contains(token);
	}

	/**
	 * Returns true if this string corresponds to an prefix operator.
	 * @param token
	 * @return true if this is a prefix operator
	 */
	public static final boolean isPrefixOperator(String token) {
		return prefixOperators.contains(token);
	}

	/**
	 * Constructs a new operator token from a string.
	 * @param value
	 */
	public OperatorToken(String value) {
		super(value);
	}
}
