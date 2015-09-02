package ca.uhnresearch.pughlab.tracker.security;

import static org.junit.matchers.JUnitMatchers.containsString;
import mockit.Expectations;
import mockit.Mocked;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.oidc.client.OidcClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RedirectForAuthenticationFilterTest extends AbstractShiroTest {
	
	RedirectForAuthenticationFilter filter;
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	@Mocked
	private BaseClient client;
	
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
	public void testPreHandle(@Mocked final BaseClient client) throws Exception {
		
		new Expectations() {{
			client.getCallbackUrl(); result = "http://example.com/client";
			client.getName(); result = "uhn";
			client.getRedirectAction(withInstanceOf(WebContext.class), false, false); result = RedirectAction.redirect("redirect");
	    }};
		
		Clients clients = new Clients();
		clients.setClients(client);
		clients.setCallbackUrl("http://example.com/");
		filter.setClients(clients);
				
		request.addParameter("client_name", new String[] { "uhn" });
		
	    boolean result = filter.preHandle(request, response);
	    Assert.assertFalse(result);
	}
}
