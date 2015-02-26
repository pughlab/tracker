package ca.uhnresearch.pughlab.tracker.dao;

/**
 * A simple class, which we can pass in to select a set of cases for data querying. 
 * 
 * @author stuartw
 */
public class CaseQuery {
	
	/**
	 * Allows the ordre direction to be specified.
	 */
	public enum OrderDirection {
		ASC, DESC
	}
	
	/**
	 * For a field filter, specifies which field to fiter by.
	 */
	public String field = null;
	
	/**
	 * For a field filter, specifies a value.
	 */
	public String pattern = null;
	
	/**
	 * When selecting a set of cases by order, for paging, specifies the start offset.
	 */
	public Integer offset = 0;

	/**
	 * When selecting a set of cases by order, for paging, specifies the case limit.
	 */
	public Integer limit = null;
	
	/**
	 * When we are using a case order, which we kind of always should, specifies which 
	 * field to order by.
	 */
	public String orderField = null;

	/**
	 * When we are using a case order, which we kind of always should, specifies how 
	 * to order that field.
	 */
	public OrderDirection orderDirection;
}
