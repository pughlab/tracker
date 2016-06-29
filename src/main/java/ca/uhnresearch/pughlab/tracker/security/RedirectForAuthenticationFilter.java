package ca.uhnresearch.pughlab.tracker.security;

import io.buji.pac4j.ShiroWebContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectForAuthenticationFilter extends AdviceFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(RedirectForAuthenticationFilter.class);
	
	private Clients clients;
	
	@Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        issueRedirect(request, response, getSeeOtherUrl(request, response));
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
    	
    	final HttpServletRequest httpRequest = (HttpServletRequest) request;
    	final HttpServletResponse httpResponse = (HttpServletResponse) response;

    	final String acrHeaders = httpRequest.getHeader("Access-Control-Request-Headers");
    	final String acrMethod = httpRequest.getHeader("Access-Control-Request-Method");

    	if (acrHeaders != null) {
    		httpResponse.setHeader("Access-Control-Allow-Headers", acrHeaders);
    	}
    	
    	if (acrMethod != null) {
    		httpResponse.setHeader("Access-Control-Allow-Methods", acrMethod);
    	}
    	
    	httpResponse.setHeader("Access-Control-Allow-Origin", "*");
    	
        WebUtils.issueRedirect(request, response, redirectUrl);
    }

    public String getSeeOtherUrl(ServletRequest request, ServletResponse response) throws Exception {
    	
    	final HttpServletRequest httpRequest = (HttpServletRequest) request;
    	final HttpServletResponse httpResponse = (HttpServletResponse) response;
    	final String clientNames[] = request.getParameterValues("client_name");
    	
    	if (clientNames == null || clientNames.length != 1) {
    		throw new RuntimeException("Can't find client_name query parameter for login request redirection");
    	}
    	
    	final ShiroWebContext context = new ShiroWebContext(httpRequest, httpResponse);
    	
    	@SuppressWarnings("rawtypes")
    	final BaseClient client = (BaseClient) clients.findClient(clientNames[0]);
    	
    	final String location = client.getRedirectAction(context, false, false).getLocation();
    	logger.debug("Redirecting to: " + location);
    	
    	return location;
    			
	}
    
	/**
	 * @return the clients
	 */
	public Clients getClients() {
		return clients;
	}

	/**
	 * @param clients the clients to set
	 */
	public void setClients(Clients clients) {
		this.clients = clients;
	}
}
