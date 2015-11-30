package ca.uhnresearch.pughlab.tracker.query;

public class ExpressionNode extends QueryNode {

	public ExpressionNode(QueryNode operandLeft, Token operator, QueryNode operandRight) {
		this.operandLeft = operandLeft;
		this.operator = operator;
		this.operandRight = operandRight;
	}

	private final QueryNode operandLeft;
	
	private final Token operator;
	
	private final QueryNode operandRight;

	/**
	 * @return the operandLeft
	 */
	public QueryNode getOperandLeft() {
		return operandLeft;
	}

	/**
	 * @return the operator
	 */
	public Token getOperator() {
		return operator;
	}

	/**
	 * @return the operandRight
	 */
	public QueryNode getOperandRight() {
		return operandRight;
	}
	
	/**
	 * Check for expression node equality
	 */
	public boolean equals(Object other) {
		if (other instanceof ExpressionNode) {
			ExpressionNode otherExpression = (ExpressionNode) other;
			return operandLeft.equals(otherExpression.getOperandLeft()) &&
					operator.equals(otherExpression.getOperator()) &&
					operandRight.equals(otherExpression.getOperandRight());
		} else {
			return false;
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (operandLeft != null) {
			builder.append(operandLeft.toString());
			builder.append(' ');
		}
		builder.append(operator.toString());
		if (operandRight != null) {
			builder.append(' ');			
			builder.append(operandRight.toString());
		}
		return builder.toString();
	}
}
