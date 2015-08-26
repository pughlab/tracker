package ca.uhnresearch.pughlab.tracker.dao.impl;

class MockCaseAttribute {
	private Integer caseId;
	private String attribute;
	private Object value;
	private Boolean notAvailable = false;
	
	MockCaseAttribute(Integer caseId, String attribute, Object value) {
		this.caseId = caseId;
		this.attribute = attribute;
		this.value = value;
	}
	/**
	 * @return the notAvailable
	 */
	public Boolean getNotAvailable() {
		return notAvailable;
	}
	/**
	 * @param notAvailable the notAvailable to set
	 */
	public void setNotAvailable(Boolean notAvailable) {
		this.notAvailable = notAvailable;
	}
	/**
	 * @return the caseId
	 */
	public Integer getCaseId() {
		return caseId;
	}
	/**
	 * @param caseId the caseId to set
	 */
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}
	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}
	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}