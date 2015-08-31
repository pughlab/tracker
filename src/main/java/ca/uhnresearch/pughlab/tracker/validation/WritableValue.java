package ca.uhnresearch.pughlab.tracker.validation;

public class WritableValue {
	
	private Class<?> valueClass;
	
	private Boolean notAvailable;
	
	private Object value;
	
	public WritableValue(Class<?> valueClass, Boolean notAvailable, Object value) {
		this.valueClass = valueClass;
		this.notAvailable = notAvailable;
		this.value = value;
	}
	
	/**
	 * @return the valueClass
	 */
	public Class<?> getValueClass() {
		return valueClass;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the notAvailable
	 */
	public Boolean getNotAvailable() {
		return notAvailable;
	}
}
