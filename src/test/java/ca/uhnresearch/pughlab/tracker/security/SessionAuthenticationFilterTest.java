package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.*;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.hamcrest.core.StringContains;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class SessionAuthenticationFilterTest extends AbstractShiroTest {
	
	private SessionAuthenticationFilter filter;
	
	private DefaultWebSecurityManager manager;
	
	@Before
	public void initialize() {
		filter = new SessionAuthenticationFilter();
		
		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		
		manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
	}
	
	@After
	public void tearDown() {
		manager = null;
		setSecurityManager(null);
		clearSubject();
	}

	@Test
	public void testGetSetApplicationName() {
		Assert.assertEquals("application", filter.getApplicationName());
		filter.setApplicationName("awesome");
		Assert.assertEquals("awesome", filter.getApplicationName());
	}
	
	@Test
	public void testGetSetUsernameParam() {
		Assert.assertEquals("username", filter.getUsernameParam());
		filter.setUsernameParam("awesome");
		Assert.assertEquals("awesome", filter.getUsernameParam());
	}

	@Test
	public void testGetSetPasswordParam() {
		Assert.assertEquals("password", filter.getPasswordParam());
		filter.setPasswordParam("awesome");
		Assert.assertEquals("awesome", filter.getPasswordParam());
	}

	@Test
	public void testGetSetAuthcScheme() {
		Assert.assertEquals("session", filter.getAuthcScheme());
		filter.setAuthcScheme("awesome");
		Assert.assertEquals("awesome", filter.getAuthcScheme());
	}

	@Test
	public void testGetSetAuthzScheme() {
		Assert.assertEquals("session", filter.getAuthzScheme());
		filter.setAuthzScheme("awesome");
		Assert.assertEquals("awesome", filter.getAuthzScheme());
	}

	@Test
	public void testGetSetFailureKeyAttribute() {
		Assert.assertEquals("shiroLoginFailure", filter.getFailureKeyAttribute());
		filter.setFailureKeyAttribute("awesome");
		Assert.assertEquals("awesome", filter.getFailureKeyAttribute());
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
		replay(token);
		
		AuthenticationException e = createMock(AuthenticationException.class);
		expect(e.getMessage()).andStubReturn("Oops: my bad");
		replay(e);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Assert.assertEquals(false, filter.onLoginFailure(token, e, request, response));
		
		Assert.assertEquals("application/json", response.getContentType());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("{\"message\":\"Oops: my bad\"}", response.getContentAsString());
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
	
	/**
	 * Checks the main login process. This requires a security manager to be set up, 
	 * as it actually goes through the login process. 
	 * @throws Exception
	 */
	@Test 
	public void testOnAccessDeniedIncorrectMethod() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setContextPath("http://localhost:8666/");
		request.setRequestURI("/login.jsp");
		request.setMethod("GET");
				
		Assert.assertEquals(false, filter.onAccessDenied(request, response));
		Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
	}

	
	/**
	 * Checks the main login process. This requires a security manager to be set up, 
	 * as it actually goes through the login process. 
	 * @throws Exception
	 */
	@Test 
	public void testOnAccessDeniedEmptyLoginRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setContextPath("http://localhost:8666/");
		request.setRequestURI("/login.jsp");
		request.setMethod("POST");
				
		Assert.assertEquals(false, filter.onAccessDenied(request, response));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		
		Assert.assertEquals("application/json", response.getContentType());
		
		Gson gson = new Gson();
		JsonObject data = gson.fromJson(response.getContentAsString(), JsonObject.class);

		Assert.assertTrue(data.isJsonObject());
		Assert.assertTrue(data.has("message"));
		Assert.assertTrue(data.get("message").isJsonPrimitive());
		Assert.assertThat(data.get("message").getAsString(), StringContains.containsString("Authentication failed"));
	}

	/**
	 * Checks the main login process with a real username and password. 
	 * @throws Exception
	 */
	@Test 
	public void testOnAccessDeniedValidLoginRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setContextPath("http://localhost:8666/");
		request.setRequestURI("/login.jsp");
		request.setMethod("POST");
		request.setParameter("username", "user");
		request.setParameter("password", "password");
		
		PrincipalCollection principals = new SimplePrincipalCollection("user", "mock");
		
		AuthenticationInfo info = createMock(AuthenticationInfo.class);
		expect(info.getPrincipals()).andStubReturn(principals);
		replay(info);
		
		Realm realm = createMock(Realm.class);
		expect(realm.supports(anyObject(AuthenticationToken.class))).andStubReturn(true);
		expect(realm.getAuthenticationInfo(anyObject(AuthenticationToken.class))).andStubReturn(info);
		replay(realm);
		manager.setRealm(realm);
		
		Subject subjectUnderTest = new Subject.Builder(manager).buildSubject();
		setSubject(subjectUnderTest);
		
		Assert.assertEquals(false, filter.onAccessDenied(request, response));
		Assert.assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
	}

	/**
	 * Checks the main login process with a real username and password. 
	 * @throws Exception
	 */
	@Test 
	public void testOnAccessDeniedInvalidLoginRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setContextPath("http://localhost:8666/");
		request.setRequestURI("/login.jsp");
		request.setMethod("POST");
		request.setParameter("username", "user");
		request.setParameter("password", "password");
		
		// Null is the correct expected response for a missing account, which is enough for now
		Realm realm = createMock(Realm.class);
		expect(realm.supports(anyObject(AuthenticationToken.class))).andStubReturn(true);
		expect(realm.getAuthenticationInfo(anyObject(AuthenticationToken.class))).andStubReturn(null);
		replay(realm);
		manager.setRealm(realm);
		
		Subject subjectUnderTest = new Subject.Builder(manager).buildSubject();
		setSubject(subjectUnderTest);
		
		Assert.assertEquals(false, filter.onAccessDenied(request, response));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
	}
}
