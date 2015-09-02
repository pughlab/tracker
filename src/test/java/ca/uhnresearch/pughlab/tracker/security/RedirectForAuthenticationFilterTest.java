package ca.uhnresearch.pughlab.tracker.security;

import static org.junit.matchers.JUnitMatchers.containsString;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
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
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;
import static org.easymock.EasyMock.*;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(BaseClient.class)
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
	public void testPreHandle() throws Exception {
		
		@SuppressWarnings("rawtypes")
		BaseClient client = PowerMock.createMock(BaseClient.class);
		
    	expect(client.getCallbackUrl()).andStubReturn("http://example.com/client");
    	expect(client.isIncludeClientNameInCallbackUrl()).andStubReturn(true);
    	expect(client.getRedirectAction(anyObject(WebContext.class), anyBoolean(), anyBoolean())).andStubReturn(RedirectAction.redirect("redirect"));
    	expect(client.getName()).andStubReturn("uhn");
    	client.setCallbackUrl("http://example.com/client?client_name=uhn");
    	client.init();
    	PowerMock.replay(client);
		
		Clients clients = new Clients();
		clients.setClients(client);
		clients.setCallbackUrl("http://example.com/");
		filter.setClients(clients);
		
		request.addParameter("client_name", new String[] { "uhn" });
		
	    boolean result = filter.preHandle(request, response);
	    Assert.assertFalse(result);
	    Assert.assertEquals("*", response.getHeaderValue("Access-Control-Allow-Origin"));
	    Assert.assertEquals("redirect", response.getHeaderValue("Location"));
	}
}
