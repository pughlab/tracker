package ca.uhnresearch.pughlab.tracker.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OperatorToken extends Token {
	
	public static final String OPERATOR_AND = "AND";

	public static final String OPERATOR_OR = "OR";
	
	private static final String[] SET_VALUES = new String[] { OPERATOR_AND, OPERATOR_OR };
	
	private static final Set<String> allOperators = new HashSet<String>(Arrays.asList(SET_VALUES));
	
	public static final boolean isOperator(String token) {
		return allOperators.contains(token);
	}

	public OperatorToken(String value) {
		super(value);
	}
	
}
