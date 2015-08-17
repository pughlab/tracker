package ca.uhnresearch.pughlab.tracker.security;

import java.net.URISyntaxException;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;

import io.buji.pac4j.ShiroWebContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
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
    	
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
    	HttpServletResponse httpResponse = (HttpServletResponse) response;

    	String acrHeaders = httpRequest.getHeader("Access-Control-Request-Headers");
    	String acrMethod = httpRequest.getHeader("Access-Control-Request-Method");

    	httpResponse.setHeader("Access-Control-Allow-Headers", acrHeaders);
    	httpResponse.setHeader("Access-Control-Allow-Methods", acrMethod);
    	
    	httpResponse.setHeader("Access-Control-Allow-Origin", "*");
    	
        WebUtils.issueRedirect(request, response, redirectUrl);
    }

    public String getSeeOtherUrl(ServletRequest request, ServletResponse response) throws Exception {
    	
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
    	HttpServletResponse httpResponse = (HttpServletResponse) response;
    	String clientNames[] = request.getParameterValues("client_name");
    	
    	if (clientNames.length != 1) {
    		throw new RuntimeException("Can't find client_name query parameter for login request redirection");
    	}
    	
    	ShiroWebContext context = new ShiroWebContext(httpRequest, httpResponse);
    	
    	@SuppressWarnings("rawtypes")
		BaseClient client = (BaseClient) clients.findClient(clientNames[0]);
    	
    	String location = client.getRedirectAction(context, false, false).getLocation();
    	logger.debug("Redirecting to: " + location);
    	
    	return location;
    			
	}
    
    protected URI fixRelativeCallback(HttpServletRequest httpRequest, URI location) throws URISyntaxException {
    	
    	List<NameValuePair> parameters = URLEncodedUtils.parse(location, "UTF-8");
    	for (ListIterator<NameValuePair> i = parameters.listIterator(); i.hasNext(); ) {
    		NameValuePair param = i.next();
    		if ("redirect_uri".equals(param.getName())) {
    			String value = param.getValue();
    			URIBuilder redirect = new URIBuilder(value);
    			if (redirect.getScheme() == null) {
    				redirect.setScheme(httpRequest.getScheme());
    				redirect.setHost(httpRequest.getServerName());
    				redirect.setPort(httpRequest.getLocalPort());
    				redirect.setPath(httpRequest.getContextPath() + redirect.getPath());
    				i.set(new BasicNameValuePair("redirect_uri", redirect.build().toString()));
    			}
    		}
    	}
    	
    	URIBuilder builder = new URIBuilder(location);
    	builder.removeQuery();
    	builder.addParameters(parameters);
    	return builder.build();
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
