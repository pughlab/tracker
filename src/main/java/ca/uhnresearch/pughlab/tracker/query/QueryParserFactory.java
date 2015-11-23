package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;

public interface QueryParserFactory {
	public QueryParser newQueryParser(String query) throws IOException, InvalidTokenException;
}
