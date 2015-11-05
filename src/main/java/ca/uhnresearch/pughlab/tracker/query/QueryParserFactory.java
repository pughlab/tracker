package ca.uhnresearch.pughlab.tracker.query;

public interface QueryParserFactory {
	public QueryParser newQueryParser(String query);
}
