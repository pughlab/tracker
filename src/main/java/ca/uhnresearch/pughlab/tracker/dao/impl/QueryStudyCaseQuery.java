package ca.uhnresearch.pughlab.tracker.dao.impl;

import com.mysema.query.sql.SQLSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Study;

public class QueryStudyCaseQuery implements StudyCaseQuery {
	
	private SQLSubQuery query;
	private Study study;

	public QueryStudyCaseQuery(Study studyValue, SQLSubQuery queryValue) {
		this.query = queryValue;
		this.study = studyValue;
	}

	/**
	 * @return the query
	 */
	public SQLSubQuery getQuery() {
		return query;
	}

	/**
	 * @param queryValue the query to set
	 */
	public void setQuery(SQLSubQuery queryValue) {
		this.query = queryValue;
	}

	/**
	 * @return the study
	 */
	@Override
	public Study getStudy() {
		return study;
	}

	/**
	 * @param studyValue the study to set
	 */
	@Override
	public void setStudy(Study studyValue) {
		this.study = studyValue;
	}

}
