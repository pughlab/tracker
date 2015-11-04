package ca.uhnresearch.pughlab.tracker.query;

public abstract class Token extends QueryNode {
	private String value;

	public Token(String value) {
		super();
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Token) {
			String otherValue = ((Token) other).getValue();
			return getClass().equals(other.getClass()) && getValue().equals(otherValue);
		} else {
			return false;
		}
	}
}
