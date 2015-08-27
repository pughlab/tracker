package ca.uhnresearch.pughlab.tracker.dao.impl;

import com.mysema.query.sql.SQLSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;

public class QueryStudyCaseQuery implements StudyCaseQuery {
	
	private SQLSubQuery query;

	public QueryStudyCaseQuery(SQLSubQuery query) {
		this.query = query;
	}

	/**
	 * @return the query
	 */
	public SQLSubQuery getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(SQLSubQuery query) {
		this.query = query;
	}

}
