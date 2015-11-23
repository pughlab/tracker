package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;

public interface Tokenizer {
	public Token getNextToken() throws IOException, InvalidTokenException;
}
