package ca.uhnresearch.pughlab.tracker.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A session authentication filter for Shiro, that combines elements of basic
 * authentication (i.e., use of WWW-Authenticate headers and 401 responses)
 * and elements of form authentication (accepting a POST submission with 
 * a specific URL for validating the credentials.
 * 
 * This doesn't subclass either FormAuthenticationFilter or 
 * BasicHttpAuthenticationFilter, because it isn't really either. However, it
 * is a good tool for RESTful API authentication, especially in single page 
 * apps.  
 * 
 * @author stuartw
 *
 */
public class SessionAuthenticationFilter extends AuthenticatingFilter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	/**
     * HTTP Authorization header, equal to <code>Authorization</code>
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    
    protected static final String SESSION_AUTH = "session";
    
    /**
     * The name that is displayed during the challenge process of authentication, defaults to <code>application</code>
     * and can be overridden by the {@link #setApplicationName(String) setApplicationName} method.
     */
    private String applicationName = "application";
	
    public static final String DEFAULT_ERROR_KEY_ATTRIBUTE_NAME = "shiroLoginFailure";
	public static final String DEFAULT_USERNAME_PARAM = "username";
    public static final String DEFAULT_PASSWORD_PARAM = "password";
	
    private String failureKeyAttribute = DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;
	private String usernameParam = DEFAULT_USERNAME_PARAM;
    private String passwordParam = DEFAULT_PASSWORD_PARAM;
	
    /**
     * The authcScheme to look for in the <code>Authorization</code> header, defaults to <code>session</code>
     */
    private String authcScheme = SESSION_AUTH;

    /**
     * The authzScheme value to look for in the <code>Authorization</code> header, defaults to <code>session</code>
     */
    private String authzScheme = SESSION_AUTH;
    
    /**
     * Returns the name to use in the ServletResponse's <b><code>WWW-Authenticate</code></b> header.
     *
     * @return the name to use in the ServletResponse's 'WWW-Authenticate' header.
     */
    public String getApplicationName() {
        return applicationName;
    }
    
    /**
     * Sets the name to use in the ServletResponse's <b><code>WWW-Authenticate</code></b> header.
     *
     * @param applicationName the name to use in the ServletResponse's 'WWW-Authenticate' header.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    /**
     * Returns the HTTP <b><code>Authorization</code></b> header value that this filter will respond to as indicating
     * a login request.
     *
     * @return the Http 'Authorization' header value that this filter will respond to as indicating a login request
     */
    public String getAuthzScheme() {
        return authzScheme;
    }

    /**
     * Sets the HTTP <b><code>Authorization</code></b> header value that this filter will respond to as indicating a
     * login request.
     *
     * @param authzScheme the HTTP <code>Authorization</code> header value that this filter will respond to as
     *                    indicating a login request.
     */
    public void setAuthzScheme(String authzScheme) {
        this.authzScheme = authzScheme;
    }
    
    /**
     * Returns the HTTP <b><code>WWW-Authenticate</code></b> header scheme that this filter will use when sending
     * the HTTP Basic challenge response.  The default value is <code>BASIC</code>.
     *
     * @return the HTTP <code>WWW-Authenticate</code> header scheme that this filter will use when sending the HTTP
     *         Basic challenge response.
     * @see #sendChallenge
     */
    public String getAuthcScheme() {
        return authcScheme;
    }

    /**
     * Sets the HTTP <b><code>WWW-Authenticate</code></b> header scheme that this filter will use when sending the
     * HTTP Basic challenge response.  The default value is <code>BASIC</code>.
     *
     * @param authcScheme the HTTP <code>WWW-Authenticate</code> header scheme that this filter will use when
     *                    sending the Http Basic challenge response.
     * @see #sendChallenge
     */
    public void setAuthcScheme(String authcScheme) {
        this.authcScheme = authcScheme;
    }

    /**
     * Processes unauthenticated requests. It handles the two-stage request/challenge authentication protocol.
     *
     * @param request  incoming ServletRequest
     * @param response outgoing ServletResponse
     * @return true if the request should be processed; false if the request should not continue to be processed
     */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
    	boolean loggedIn = false;
    	
        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                logger.trace("Login submission detected.  Attempting to execute login.");
                
                loggedIn = executeLogin(request, response);
            }
        }
        
    	logger.trace("Logged in: " + loggedIn);
        
        if (!loggedIn) {
            sendChallenge(request, response);
        }
        return loggedIn;
    }
    
    /**
     * Builds the challenge for authorization by setting a HTTP <code>401</code> (Unauthorized) status as well as the
     * response's {@link #AUTHENTICATE_HEADER AUTHENTICATE_HEADER}.
     * <p>
     * The header value constructed is equal to:
     * <p>
     * <code>{@link #getAuthcScheme() getAuthcScheme()} + " realm=\"" + {@link #getApplicationName() getApplicationName()} + "\"";</code>
     *
     * @param request  incoming ServletRequest, ignored by this implementation
     * @param response outgoing ServletResponse
     * @return false - this sends the challenge to be sent back
     */
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        logger.debug("Authentication required: sending 401 Authentication challenge response.");
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String authcHeader = getAuthcScheme() + " realm=\"" + getApplicationName() + "\"";
        httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
        return false;
    }

    
    /**
     * This default implementation merely returns <code>true</code> if the request is an HTTP <code>POST</code>,
     * <code>false</code> otherwise. Can be overridden by subclasses for custom login submission detection behavior.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse.
     * @return <code>true</code> if the request is an HTTP <code>POST</code>, <code>false</code> otherwise.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isLoginSubmission(ServletRequest request, ServletResponse response) {
        return (request instanceof HttpServletRequest) && WebUtils.toHttp(request).getMethod().equalsIgnoreCase(POST_METHOD);
    }

    /**
     * Returns the {@link #AUTHORIZATION_HEADER AUTHORIZATION_HEADER} from the specified ServletRequest.
     * <p>
     * This implementation merely casts the request to an <code>HttpServletRequest</code> and returns the header:
     * <p>
     * <code>HttpServletRequest httpRequest = {@link WebUtils#toHttp(javax.servlet.ServletRequest) toHttp(reaquest)};<br>
     * return httpRequest.getHeader({@link #AUTHORIZATION_HEADER AUTHORIZATION_HEADER});</code>
     *
     * @param request the incoming <code>ServletRequest</code>
     * @return the <code>Authorization</code> header's value.
     */
    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getHeader(AUTHORIZATION_HEADER);
    }

    
    public SessionAuthenticationFilter() {
        setLoginUrl(DEFAULT_LOGIN_URL);
    }
	
	@Override
    public void setLoginUrl(String loginUrl) {
        String previous = getLoginUrl();
        if (previous != null) {
            this.appliedPaths.remove(previous);
        }
        super.setLoginUrl(loginUrl);
    	logger.trace("Adding login url to applied paths.");
        this.appliedPaths.put(getLoginUrl(), null);
    }
	
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        this.isLoginRequest(request, response);
        return createToken(username, password, request, response);
    }
	
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response)
			throws Exception {
    	logger.trace("onLoginSuccess: issuing success redirect.");
        issueSuccessRedirect(request, response);
		// we handled the success redirect directly, prevent the chain from
		// continuing:
		return false;
	}

	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request,
			ServletResponse response) {
		
		// login failed, let request continue back to the login page:
		try {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setContentType("application/json");
			httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			PrintWriter out = httpResponse.getWriter();
			ObjectNode marked = jsonNodeFactory.objectNode();
			marked.put("message", e.getMessage());
			out.print(objectMapper.writeValueAsString(marked));
			out.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return false;
	}

	protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
		String className = ae.getClass().getName();
		request.setAttribute(getFailureKeyAttribute(), className);
	}
	
	protected String getUsername(ServletRequest request) {
        return WebUtils.getCleanParam(request, getUsernameParam());
    }
	
	protected String getPassword(ServletRequest request) {
        return WebUtils.getCleanParam(request, getPasswordParam());
    }

	/**
	 * @return the usernameParam
	 */
	public String getUsernameParam() {
		return usernameParam;
	}

	/**
	 * @param usernameParam the usernameParam to set
	 */
	public void setUsernameParam(String usernameParam) {
		this.usernameParam = usernameParam;
	}

	/**
	 * @return the passwordParam
	 */
	public String getPasswordParam() {
		return passwordParam;
	}

	/**
	 * @param passwordParam the passwordParam to set
	 */
	public void setPasswordParam(String passwordParam) {
		this.passwordParam = passwordParam;
	}
	
    public String getFailureKeyAttribute() {
        return failureKeyAttribute;
    }

    public void setFailureKeyAttribute(String failureKeyAttribute) {
        this.failureKeyAttribute = failureKeyAttribute;
    }

}
