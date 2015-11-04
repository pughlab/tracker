package ca.uhnresearch.pughlab.tracker.query;

import java.io.StringReader;

public class SimpleQueryParserFactory implements QueryParserFactory {

	@Override
	public QueryParser newQueryParser(String query) {
		return new SimpleQueryParser(new StringReader(query));
	}

}
