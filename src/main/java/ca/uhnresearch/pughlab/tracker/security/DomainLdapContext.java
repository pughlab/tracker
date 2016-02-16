package ca.uhnresearch.pughlab.tracker.security;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.standalone.StandaloneLdapApiService;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainLdapContext implements LdapContext {
	
    private static final Logger log = LoggerFactory.getLogger(DomainLdapContext.class);

	private String ldapHost = "192.168.198.100";
	
	private int ldapPort = 389;
	
	private int timeout = 180000;
	
	private String domain;

	private String searchTemplate = "OU=user,OU=accounts,DC=ads,DC=uhnresearch,DC=ca";
	
	private String filterTemplate = "(userPrincipalName={0})";
	
	private LdapConnectionPool pool;
	
	private int minEvictableIdleTimeMillis = 60000;
	
	private int timeBetweenEvictionRunsMillis = 60000;
	
	private String displayNameAttribute = "displayName";
	
	private String givenNameAttribute = "givenName";
	
	private String familyNameAttribute = "sn";
	
	private String emailAttribute = "mail";
	
	/**
	 * Lazily initialize a connection pool on demand.
	 * @return a connection
	 * @throws Exception 
	 */
	protected LdapConnectionPool getConnectionPool() throws Exception {
		if (pool != null) {
			return pool;
		}
		
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost(ldapHost);
		config.setLdapPort(ldapPort);
		LdapApiService service = new StandaloneLdapApiService();
		pool = new LdapConnectionPool(config, service, timeout);
		pool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		pool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		return pool;
	}
	
	private AuthenticationInfo queryInternal(String username, char[] password, Realm realm) throws LdapException, Exception {
		LdapConnectionPool pool = getConnectionPool();
		LdapConnection connection = pool.getConnection();
		AuthenticationInfo info = null;
		
		BindRequest bindRequest=new BindRequestImpl();
		bindRequest.setName(username);
		bindRequest.setCredentials((new String(password)).getBytes());
		
		BindResponse response = connection.bind(bindRequest);
		LdapResult result = response.getLdapResult();
		if (! ResultCodeEnum.SUCCESS.equals(result.getResultCode())) {
			getConnectionPool().releaseConnection(connection);
			throw new AuthenticationException("Failed to authenticate: " + result.getDiagnosticMessage());
		}
		
		if (! connection.isAuthenticated()) {
			getConnectionPool().releaseConnection(connection);
			throw new AuthenticationException("Failed to authenticate");
		}
		
		String search = MessageFormat.format(searchTemplate, username);
		String filter = MessageFormat.format(filterTemplate, username);
		
		// Do a search to make sure we can find the object somehow
		EntryCursor cursor = connection.search(search, filter, SearchScope.SUBTREE);
		if (cursor.next()) {
			Entry entry = cursor.get();
			log.debug("Found LDAP entry: " + entry.toString());
			final LdapProfile profile = getLdapProfile(username, entry);
			final List<? extends Object> principals = Arrays.asList(username, profile);
	        final PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, realm.getName());
	        info = new SimpleAuthenticationInfo(principalCollection, password);
		}

		getConnectionPool().releaseConnection(connection);
		return info;
	}
	
	private LdapProfile getLdapProfile(String username, Entry entry) {
		LdapProfile profile = new LdapProfile(username);
		
		try {
			if (displayNameAttribute != null) {
				Attribute attribute = entry.get(displayNameAttribute);
				if (attribute != null) {
					profile.setDisplayName(attribute.getString());
				}
			}
		} catch (LdapInvalidAttributeValueException e) {
			// No nothing
		}
		
		try {
			if (givenNameAttribute != null) {
				Attribute attribute = entry.get(givenNameAttribute);
				if (attribute != null) {
					profile.setGivenName(attribute.getString());
				}
			}
		} catch (LdapInvalidAttributeValueException e) {
			// No nothing
		}
		
		try {
			if (familyNameAttribute != null) {
				Attribute attribute = entry.get(familyNameAttribute);
				if (attribute != null) {
					profile.setFamilyName(attribute.getString());
				}
			}
		} catch (LdapInvalidAttributeValueException e) {
			// No nothing
		}
		
		try {
			if (emailAttribute != null) {
				Attribute attribute = entry.get(emailAttribute);
				if (attribute != null) {
					profile.setEmail(attribute.getString());
				}
			}
		} catch (LdapInvalidAttributeValueException e) {
			// No nothing
		}

		return profile;
	}

	@Override
	public AuthenticationInfo query(AuthenticationToken token, Realm realm) throws AuthenticationException {
		
		if (! (token instanceof UsernamePasswordToken)) {
			throw new AuthenticationException("Expecting a username and a password: " + token.toString());
		}
		
		UsernamePasswordToken userToken = (UsernamePasswordToken) token;
	    String username = userToken.getUsername();
	    char[] password = userToken.getPassword();
	    
        String[] parts = username.split("@");
        if (parts.length == 1) {
        	username = username + "@" + domain;
        }
	    
        log.debug("Authenticating user '{}' through LDAP", username);
        
        try {
			return queryInternal(username, password, realm);	
		} catch (Exception e) {
			throw new AuthenticationException(e);
		}
	}

	@Override
	public boolean canAuthenticate(AuthenticationToken token, Realm realm) {
	    Object principal = token.getPrincipal();
        log.debug("Verifying authentication of user '{}' through LDAP for domain '{}'", principal, domain);
        
        String sPrincipal = (String) principal;
        String[] parts = sPrincipal.split("@", 2);
        
        if (parts.length == 1) {
        	return true;
        } else {
        	String tokenDomain = parts[1];
        	return domain.equals(tokenDomain);
        }
	}

	/**
	 * @return the ldapHost
	 */
	public String getLdapHost() {
		return ldapHost;
	}

	/**
	 * @param ldapHost the ldapHost to set
	 */
	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	/**
	 * @return the ldapPort
	 */
	public int getLdapPort() {
		return ldapPort;
	}

	/**
	 * @param ldapPort the ldapPort to set
	 */
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the searchTemplate
	 */
	public String getSearchTemplate() {
		return searchTemplate;
	}

	/**
	 * @param searchTemplate the searchTemplate to set
	 */
	public void setSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
	}

	/**
	 * @return the filterTemplate
	 */
	public String getFilterTemplate() {
		return filterTemplate;
	}

	/**
	 * @param filterTemplate the filterTemplate to set
	 */
	public void setFilterTemplate(String filterTemplate) {
		this.filterTemplate = filterTemplate;
	}

	/**
	 * @return the minEvictableIdleTimeMillis
	 */
	public int getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	/**
	 * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set
	 */
	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * @return the timeBetweenEvictionRunsMillis
	 */
	public int getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	/**
	 * @param timeBetweenEvictionRunsMillis the timeBetweenEvictionRunsMillis to set
	 */
	public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	/**
	 * @return the displayNameAttribute
	 */
	public String getDisplayNameAttribute() {
		return displayNameAttribute;
	}

	/**
	 * @param displayNameAttribute the displayNameAttribute to set
	 */
	public void setDisplayNameAttribute(String displayNameAttribute) {
		this.displayNameAttribute = displayNameAttribute;
	}

	/**
	 * @return the emailAttribute
	 */
	public String getEmailAttribute() {
		return emailAttribute;
	}

	/**
	 * @param emailAttribute the emailAttribute to set
	 */
	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}

}
