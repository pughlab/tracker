package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;

/**
 * A tokenizer interface, which defines the behaviours that we expect from
 * a tokenizer.
 * 
 * @author stuartw
 */
public interface Tokenizer {
	
	/**
	 * Gets the next token from a tokenizer.
	 * @return the token
	 * @throws IOException
	 * @throws InvalidTokenException
	 */
	public Token getNextToken() throws IOException, InvalidTokenException;
}
