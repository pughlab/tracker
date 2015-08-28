package ca.uhnresearch.pughlab.tracker.dao.impl;

import com.mysema.query.sql.SQLSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;

public class QueryStudyCaseQuery implements StudyCaseQuery {
	
	private SQLSubQuery query;
	private Study study;

	public QueryStudyCaseQuery(Study study, SQLSubQuery query) {
		this.query = query;
		this.study = study;
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

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

}
