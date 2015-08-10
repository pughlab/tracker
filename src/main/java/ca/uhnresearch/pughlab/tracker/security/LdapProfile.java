package ca.uhnresearch.pughlab.tracker.security;

public class LdapProfile {
	
	public LdapProfile(String name) {
		this.name = name;
	}
	
	private String name;
	
	private String displayName;
	
	private String email;
	
	public String toString() {
		return name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the realName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param realName the realName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
