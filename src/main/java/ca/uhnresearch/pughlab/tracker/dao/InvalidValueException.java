package ca.uhnresearch.pughlab.tracker.dao;

/**
 * An exception that gets thrown where there's an invalid value.
 * 
 * @author stuartw
 */
public class InvalidValueException extends RepositoryException {

	/**
	 * Serialization identity.
	 */
	private static final long serialVersionUID = 4488421367265819771L;

	/**
	 * Constructs a new {@link InvalidValueException} with a given message.
	 * @param message the message string
	 */
	public InvalidValueException(String message) {
		super(message);
	}

}
