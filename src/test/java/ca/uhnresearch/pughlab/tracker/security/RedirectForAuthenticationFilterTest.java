package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RedirectForAuthenticationFilterTest extends AbstractShiroTest {
	
	RedirectForAuthenticationFilter filter;
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		filter = new RedirectForAuthenticationFilter();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		
		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
	}
	
	@Test
	public void testPreHandleNoClientNames() throws Exception {

		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Can't find client_name"));

	    boolean result = filter.preHandle(request, response);
	    Assert.assertFalse(result);
	}

	@Test
	public void testPreHandleEmptyClientNames() throws Exception {
		
		request.setParameter("client_name", new String[] {});

		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Can't find client_name"));

	    boolean result = filter.preHandle(request, response);
	    Assert.assertFalse(result);
	}

	@Test
	public void testPreHandle() throws Exception {
		
		RedirectAction action = createMock(RedirectAction.class);
		expect(action.getLocation()).andStubReturn("http://localhost:8000/oidc_redirect");
		replay(action);
		
		AbsolutifyingOidcClient client = createMock(AbsolutifyingOidcClient.class);
		expect(client.getCallbackUrl()).andStubReturn("http://localhost:8000/oidc");
		expect(client.isIncludeClientNameInCallbackUrl()).andStubReturn(true);
		expect(client.getName()).andStubReturn("uhn");
		client.setCallbackUrl("http://localhost:8000/oidc?client_name=uhn");
		expectLastCall().anyTimes();
		client.init();
		expectLastCall().anyTimes();
		expect(client.isDirectRedirection()).andStubReturn(true);
		expect(client.retrieveRedirectAction(anyObject(WebContext.class))).andStubReturn(action);
		replay(client);
		
		Clients clients = new Clients();
		clients.setClients(client);
		clients.setCallbackUrl("http://example.com/");
		filter.setClients(clients);
		
		// Check we can get clients while we're at it
		Assert.assertEquals(clients, filter.getClients());
		
		request.setParameter("client_name", "uhn");

		filter.preHandle(request, response);
		
		Assert.assertEquals("http://localhost:8000/oidc_redirect", response.getRedirectedUrl());
		Assert.assertNull(response.getHeader("Access-Control-Allow-Headers"));
		Assert.assertNull(response.getHeader("Access-Control-Allow-Methods"));
		Assert.assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
		
		// And change the headers and try again
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		request.setParameter("client_name", "uhn");
		request.addHeader("Access-Control-Request-Headers", "Vary");
		request.addHeader("Access-Control-Request-Method", "GET");

		filter.preHandle(request, response);
		Assert.assertEquals("Vary", response.getHeader("Access-Control-Allow-Headers"));
		Assert.assertEquals("GET", response.getHeader("Access-Control-Allow-Methods"));

	}
}
