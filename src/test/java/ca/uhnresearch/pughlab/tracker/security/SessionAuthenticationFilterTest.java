package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class SessionAuthenticationFilterTest {
	
	private SessionAuthenticationFilter filter;
	
	@Before
	public void initialize() {
		filter = new SessionAuthenticationFilter();
	}

	@Test
	public void testScheme() {
		Assert.assertEquals("session", filter.getAuthcScheme());
	}

	@Test
	public void testApplicationName() {
		Assert.assertEquals("application", filter.getApplicationName());
	}
	
	@Test
	public void testGetUsername() {
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("username")).andStubReturn("admin");
		replay(request);
		Assert.assertEquals("admin", filter.getUsername(request));
	}
	
	@Test
	public void testGetPassword() {
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("password")).andStubReturn("junk");
		replay(request);
		Assert.assertEquals("junk", filter.getPassword(request));
	}
	
	@Test
	public void testOnLoginFailure() throws UnsupportedEncodingException {
		AuthenticationToken token = createMock(AuthenticationToken.class);
		HttpServletRequest request = createMock(HttpServletRequest.class);
		replay(token);
		replay(request);
		
		AuthenticationException e = createMock(AuthenticationException.class);
		expect(e.getMessage()).andStubReturn("Oops: my bad");
		replay(e);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Assert.assertEquals(false, filter.onLoginFailure(token, e, request, response));
		
		Assert.assertEquals("application/json", response.getContentType());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("{\"message\":\"Oops: my bad\"}", response.getContentAsString());
	}

}
