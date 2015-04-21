package ca.uhnresearch.pughlab.tracker.dao;

public abstract class RepositoryException extends Exception {

	private static final long serialVersionUID = -940454860617142614L;

	public RepositoryException(String message) {
        super(message);
    }
}
