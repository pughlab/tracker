package ca.uhnresearch.pughlab.tracker.query;

/**
 * Exception for an invalid token in the query language.
 *
 * @author stuartw
 */
public class InvalidTokenException extends Exception {

	/**
	 * Default empty constructor for the exception.
	 */
	public InvalidTokenException() {
		super();
	}

	/**
	 * Constructor with a message.
	 * @param message
	 */
	public InvalidTokenException(String message) {
		super(message);
	}

	/**
	 * Serialization identity value.
	 */
	private static final long serialVersionUID = 2723046549909525638L;

}
