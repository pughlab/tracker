package ca.uhnresearch.pughlab.tracker.query;

/**
 * Abstract base class for a token.
 *
 * @author stuartw
 */
public abstract class Token extends QueryNode {
	private String value;

	/**
	 * Constructs a new token from a string.
	 * @param value
	 */
	public Token(String value) {
		super();
		this.value = value;
	}

	/**
	 * Returns the token string.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the token string.
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Tests two tokens for equality.
	 * @param other the other token
	 * @return true if the tokens are equal
	 */
	public boolean equals(Object other) {
		if (other instanceof Token) {
			final String otherValue = ((Token) other).getValue();
			return getClass().equals(other.getClass()) && getValue().equals(otherValue);
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the token string representation.
	 */
	public String toString() {
		return getValue();
	}
}
