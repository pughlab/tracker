package ca.uhnresearch.pughlab.tracker.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAccessFilter extends UserFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name that is displayed during the challenge process of authentication, defaults to <code>application</code>
     * and can be overridden by the {@link #setApplicationName(String) setApplicationName} method.
     */
    private String applicationName = "application";
    
    /**
     * Default prompt isn't set.
     */
    private String prompt = null;

    protected static final String SESSION_AUTH = "session";

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    
    /**
     * Non-standard header, used to transmit an optional prompt to the client.
     */
    protected static final String PROMPT_HEADER = "X-Tracker-Login-Prompt";

    /**
     * The authcScheme to look for in the <code>Authorization</code> header, defaults to <code>session</code>
     */
    private String authcScheme = SESSION_AUTH;

    /**
     * This default implementation simply returns a 401 rather than a URL.
     */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        logger.debug("Authentication required: sending 401 Authentication challenge response.");
        final HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final String authcHeader = getAuthcScheme() + " realm=\"" + getApplicationName() + "\"";
        httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
        if (prompt != null) {
        	httpResponse.setHeader(PROMPT_HEADER, prompt);
        }
        return false;
    }

	/**
	 * @return the authcScheme
	 */
	public String getAuthcScheme() {
		return authcScheme;
	}

	/**
	 * @param authcScheme the authcScheme to set
	 */
	public void setAuthcScheme(String authcScheme) {
		this.authcScheme = authcScheme;
	}
	
    /**
     * Returns the name to use in the ServletResponse's <b><code>WWW-Authenticate</code></b> header.
     *
     * @return the name to use in the ServletResponse's 'WWW-Authenticate' header.
     */
    public String getApplicationName() {
        return applicationName;
    }

	/**
	 * @return the prompt
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * @param prompt the prompt to set
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

}
