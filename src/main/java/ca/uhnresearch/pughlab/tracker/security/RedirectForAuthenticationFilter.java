package ca.uhnresearch.pughlab.tracker.security;

import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectForAuthenticationFilter extends AdviceFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(RedirectForAuthenticationFilter.class);
	
	private String openIDUrl;
	private String clientID;
	
	@Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        String redirectUrl = getRedirectUrl();
        issueRedirect(request, response, redirectUrl);
        return false;
    }
	
	/**
     * Issues an HTTP redirect to the specified URL after subject logout.  This implementation simply calls
     * {@code WebUtils.}{@link WebUtils#issueRedirect(javax.servlet.ServletRequest, javax.servlet.ServletResponse, String) issueRedirect(request,response,redirectUrl)}.
     *
     * @param request  the incoming Servlet request
     * @param response the outgoing Servlet response
     * @param redirectUrl the URL to where the browser will be redirected immediately after Subject logout.
     * @throws Exception if there is any error.
     */
    protected void issueRedirect(ServletRequest request, ServletResponse response, String redirectUrl) throws Exception {
        WebUtils.issueRedirect(request, response, redirectUrl);
    }

    public String getRedirectUrl() throws URISyntaxException {
		
		String nonce = UUID.randomUUID().toString();

		Subject subject = SecurityUtils.getSubject();
		Session session = subject.getSession(true);
		session.setAttribute("nonce", nonce);
				
		// Restlet References are a decent URI builder
		Reference seeOther = new Reference();
		seeOther.setBaseRef(getOpenIDUrl());
		seeOther.setRelativePart("authorize");
		seeOther.addQueryParameter("response_type", "code");
		seeOther.addQueryParameter("nonce", nonce);
		seeOther.addQueryParameter("client_id", getClientID());
		
		return seeOther.getTargetRef().toString();
	}

	/**
	 * @return the openIDUrl
	 */
	public String getOpenIDUrl() {
		return openIDUrl;
	}

	/**
	 * @param openIDUrl the openIDUrl to set
	 */
	public void setOpenIDUrl(String openIDUrl) {
		this.openIDUrl = openIDUrl;
	}

	/**
	 * @return the clientID
	 */
	public String getClientID() {
		return clientID;
	}

	/**
	 * @param clientID the clientID to set
	 */
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
}
