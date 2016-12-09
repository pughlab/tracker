package ca.uhnresearch.pughlab.tracker.dao.impl;

import ca.uhnresearch.pughlab.tracker.domain.QCases;

import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

/**
 * Used to map a tuple into a {@link CaseInfo} record
 * .
 * @author stuartw
 */
public class CaseInfoProjection extends MappingProjection<CaseInfo>{

	/**
	 * Constructor from a {@link QCases}.
	 * @param cases
	 */
	public CaseInfoProjection(QCases cases) {
		super(CaseInfo.class, cases.id, cases.guid, cases.order, cases.state);
	}

	/**
	 * Generated serial identity.
	 */
	private static final long serialVersionUID = 2178068631562986800L;

	/**
	 * The mapping method.
	 * @param tuple the input tuple
	 * @return the new {@link CaseInfo}.
	 */
	@Override
	protected CaseInfo map(Tuple tuple) {
		final QCases cases = QCases.cases;
		return new CaseInfo(tuple.get(cases.id), tuple.get(cases.guid), tuple.get(cases.order), tuple.get(cases.state));
	}
}
