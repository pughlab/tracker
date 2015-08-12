package ca.uhnresearch.pughlab.tracker.security;

import org.pac4j.core.context.WebContext;
import org.pac4j.oidc.credentials.OidcCredentials;

import com.nimbusds.oauth2.sdk.AuthorizationCode;

public class ContextualOidcCredentials extends OidcCredentials {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 341445238257358465L;

	private transient WebContext context;

	public ContextualOidcCredentials(AuthorizationCode code, WebContext context) {
		super(code);
		this.context = context;
	}

	/**
	 * @return the context
	 */
	public WebContext getContext() {
		return context;
	}

}
