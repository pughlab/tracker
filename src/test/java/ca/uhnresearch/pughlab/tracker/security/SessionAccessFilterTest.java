package ca.uhnresearch.pughlab.tracker.security;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class SessionAccessFilterTest extends AbstractShiroTest {

	private SessionAccessFilter filter;
	
	@Before
	public void initialize() {
		filter = new SessionAccessFilter();
		
		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
	}
	
	@After
	public void tearDown() {
		setSecurityManager(null);
	}

	@Test
	public void testScheme() {
		Assert.assertEquals("session", filter.getAuthcScheme());
	}

	@Test
	public void testSetScheme() {
		filter.setAuthcScheme("awesome");
		Assert.assertEquals("awesome", filter.getAuthcScheme());
	}

	@Test
	public void testApplicationName() {
		Assert.assertEquals("application", filter.getApplicationName());
	}

	@Test 
	public void testOnAccessDeniedWithoutSession() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setContextPath("http://localhost:8666/");
		request.setRequestURI("/api/studies");
				
		Assert.assertEquals(false, filter.onAccessDenied(request, response));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
	}
}
