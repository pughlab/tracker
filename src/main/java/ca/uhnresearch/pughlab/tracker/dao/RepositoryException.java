package ca.uhnresearch.pughlab.tracker.dao;

/**
 * An exception that gets thrown where there's some other kind of 
 * repository exception.
 * 
 * @author stuartw
 */
public abstract class RepositoryException extends Exception {

	/**
	 * Serialization identity.
	 */
	private static final long serialVersionUID = -940454860617142614L;

	/**
	 * Constructs a new {@link RepositoryException} with a given message.
	 * @param message the message string
	 */
	public RepositoryException(String message) {
        super(message);
    }
}
