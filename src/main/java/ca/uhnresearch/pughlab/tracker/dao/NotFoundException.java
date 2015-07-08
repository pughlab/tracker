package ca.uhnresearch.pughlab.tracker.dao;

public class NotFoundException extends RepositoryException {

	private static final long serialVersionUID = 2897006812479253183L;
	
	public NotFoundException(String message) {
		super(message);
	}

}
