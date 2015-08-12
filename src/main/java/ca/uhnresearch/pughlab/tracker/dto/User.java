package ca.uhnresearch.pughlab.tracker.dto;

import java.util.Iterator;

import org.apache.shiro.SecurityUtils;
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
	
	public User() { 
		this(SecurityUtils.getSubject());
	}

	public User(String username) {
		setUsername(username);
	}
	
	public User(Subject subject) {
		
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
				setEmail(profile.getEmail());
			} else if (p instanceof OidcProfile) {
				OidcProfile profile = (OidcProfile) p;
				setUsername(profile.getAttribute("preferred_username").toString());
				setDisplayName(profile.getAttribute("name").toString());
				setEmail(profile.getAttribute("email").toString());
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
}
