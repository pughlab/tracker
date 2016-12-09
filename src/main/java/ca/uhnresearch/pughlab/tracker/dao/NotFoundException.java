package ca.uhnresearch.pughlab.tracker.dao;

/**
 * An exception that gets thrown where there's a failure to find
 * an expected object.
 * 
 * @author stuartw
 */
public class NotFoundException extends RepositoryException {

	/**
	 * Serialization identity.
	 */
	private static final long serialVersionUID = 2897006812479253183L;
	
	/**
	 * Constructs a new {@link NotFoundException} with a given message.
	 * @param message the message string
	 */
	public NotFoundException(String message) {
		super(message);
	}

}
