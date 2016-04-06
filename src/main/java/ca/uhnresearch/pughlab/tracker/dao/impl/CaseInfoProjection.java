package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;
import ca.uhnresearch.pughlab.tracker.domain.QCases;

import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

public class CaseInfoProjection extends MappingProjection<CaseInfo>{

	public CaseInfoProjection(QCases cases) {
		super(CaseInfo.class, cases.id, cases.guid, cases.state);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2178068631562986800L;

	@Override
	protected CaseInfo map(Tuple tuple) {
		return new CaseInfo(tuple.get(cases.id), tuple.get(cases.guid), tuple.get(cases.state));
	}
}
