package ca.uhnresearch.pughlab.tracker.sockets;

/**
 * A socket-level exception.
 * @author stuartw
 */
public class SocketException extends Exception {

	/**
	 * Generated serial identifier.
	 */
	private static final long serialVersionUID = 3454869361302294312L;

	/**
	 * Default constructor.
	 * @param message the error message
	 */
	public SocketException(String message) {
        super(message);
    }

}
