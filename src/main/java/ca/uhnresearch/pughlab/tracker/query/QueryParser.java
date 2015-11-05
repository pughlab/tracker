package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;

public interface QueryParser {
	
	public void setReader(Tokenizer input);
	
	public QueryNode parse() throws IOException, InvalidTokenException;
}
