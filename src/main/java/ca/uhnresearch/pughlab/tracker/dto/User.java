package ca.uhnresearch.pughlab.tracker.dto;

import java.util.Iterator;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.pac4j.oidc.profile.OidcProfile;

import ca.uhnresearch.pughlab.tracker.security.LdapProfile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	private String username;
	
	private Boolean administrator = false;
	
	private String email;
	
	private String displayName;
	
	private String givenName;

	private String familyName;

	public User() { 

	}

	public User(String username) {
		this();
		setUsername(username);
	}
	
	public User(Subject subject) {
		this();
		
		PrincipalCollection principals = subject.getPrincipals();
		
		// First get a string for the username
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = principals.iterator();
		while(iterator.hasNext()) {
		    Object p = iterator.next();
			if (p instanceof String) {
				setUsername((String) p);
			} else if (p instanceof LdapProfile) {
				LdapProfile profile = (LdapProfile) p;
				setDisplayName(profile.getDisplayName());
				setGivenName(profile.getGivenName());
				setFamilyName(profile.getFamilyName());
				setEmail(profile.getEmail());
			} else if (p instanceof OidcProfile) {
				OidcProfile profile = (OidcProfile) p;
				setUsername(profile.getAttribute("preferred_username").toString());
				setDisplayName(profile.getAttribute("name").toString());
				if (profile.getAttribute("given_name") != null) {
					setGivenName(profile.getAttribute("given_name").toString());
				}
				if (profile.getAttribute("family_name") != null) {
					setFamilyName(profile.getAttribute("family_name").toString());
				}
				setEmail(profile.getAttribute("email").toString());
			} else {
				throw new RuntimeException("Unexpected principal type: " + p.getClass().getCanonicalName());
			}
		}
		
		// Fallback in the case of a missing username
		if (getUsername() == null) {
			setUsername(principals.getPrimaryPrincipal().toString());
		}
		
		if (subject.hasRole("ROLE_ADMIN")) {
			setAdministrator(true);
		}
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty
	public Boolean getAdministrator() {
		return administrator;
	}

	public void setAdministrator(Boolean administrator) {
		this.administrator = administrator;
	}

	/**
	 * @return the email
	 */
	@JsonProperty
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the displayName
	 */
	@JsonProperty
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the givenName
	 */
	@JsonProperty
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the familyName
	 */
	@JsonProperty
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
}
