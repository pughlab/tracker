package ca.uhnresearch.pughlab.tracker.dao;

/**
 * A simple class, which we can pass in to select a set of cases for data querying. 
 * 
 * @author stuartw
 */
public class CasePager {
	
	/**
	 * Allows the order direction to be specified.
	 */
	public enum OrderDirection {
		ASC, DESC
	}
	
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
	 * Returns the offset for this pager.
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * Returns true if there is an offset for this pager.
	 * @return the existence of the offset
	 */
	public boolean hasOffset() {
		return offset != null;
	}

	/**
	 * Sets the offset for this pager.
	 * @param offsetValue the offset to set
	 */
	public void setOffset(Integer offsetValue) {
		this.offset = offsetValue;
	}

	/**
	 * Returns the limit for this pager.
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * Returns true if there is a limit for this pager.
	 * @return the existence of the limit
	 */
	public boolean hasLimit() {
		return limit != null;
	}

	/**
	 * Sets the limit for this pager.
	 * @param limitValue the limit to set
	 */
	public void setLimit(Integer limitValue) {
		this.limit = limitValue;
	}

	/**
	 * Returns the order field for this pager.
	 * @return the orderField
	 */
	public String getOrderField() {
		return orderField;
	}

	/**
	 * Sets the order field for this pager.
	 * @param orderFieldValue the orderField to set
	 */
	public void setOrderField(String orderFieldValue) {
		this.orderField = orderFieldValue;
	}

	/**
	 * Returns the order direction for this pager.
	 * @return the orderDirection
	 */
	public OrderDirection getOrderDirection() {
		return orderDirection;
	}

	/**
	 * Sets the order direction for this pager.
	 * @param orderDirectionValue the orderDirection to set
	 */
	public void setOrderDirection(OrderDirection orderDirectionValue) {
		this.orderDirection = orderDirectionValue;
	}

}
