package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.StringReader;

public class SimpleQueryParserFactory implements QueryParserFactory {

	@Override
	public QueryParser newQueryParser(String query) throws IOException, InvalidTokenException {
		Tokenizer input = new MergingTokenizer(new SimpleTokenizer(new StringReader(query)));
		return new SimpleQueryParser(input);
	}

}
