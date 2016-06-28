package ca.uhnresearch.pughlab.tracker.dao;

/**
 * An exception that gets thrown where there's a data integrity issue,
 * that is, hopefully never.
 * 
 * @author stuartw
 */
public class DataIntegrityException extends RepositoryException {

	/**
	 * Serialization identity.
	 */
	private static final long serialVersionUID = 6434155518262270072L;

	/**
	 * Constructs a new {@link DataIntegrityException} with a given message.
	 * @param message the message string
	 */
	public DataIntegrityException(String message) {
		super(message);
	}

}
