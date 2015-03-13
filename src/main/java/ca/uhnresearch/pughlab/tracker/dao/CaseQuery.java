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
	private String field = null;
	
	/**
	 * For a field filter, specifies a value.
	 */
	private String pattern = null;
	
	/**
	 * When selecting a set of cases by order, for paging, specifies the start offset.
	 */
	private Integer offset = null;

	/**
	 * When selecting a set of cases by order, for paging, specifies the case limit.
	 */
	private Integer limit = null;
	
	/**
	 * When we are using a case order, which we kind of always should, specifies which 
	 * field to order by.
	 */
	private String orderField = null;

	/**
	 * When we are using a case order, which we kind of always should, specifies how 
	 * to order that field.
	 */
	private OrderDirection orderDirection;

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * @return the orderField
	 */
	public String getOrderField() {
		return orderField;
	}

	/**
	 * @param orderField the orderField to set
	 */
	public void setOrderField(String orderField) {
		this.orderField = orderField;
	}

	/**
	 * @return the orderDirection
	 */
	public OrderDirection getOrderDirection() {
		return orderDirection;
	}

	/**
	 * @param orderDirection the orderDirection to set
	 */
	public void setOrderDirection(OrderDirection orderDirection) {
		this.orderDirection = orderDirection;
	}

}
