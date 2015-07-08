package ca.uhnresearch.pughlab.tracker.security;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class SpringShiroInterceptorTest extends AbstractShiroTest {
	
	public SpringShiroInterceptor interceptor;

	@Before
	public void initialize() {
		interceptor = new SpringShiroInterceptor();
		
		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
		
		interceptor.setSecurityManager(manager);
	}
	
	@After
	public void tearDown() {
		setSecurityManager(null);
	}

	@Test
	public void testInspectWebsockets() {
		
		AtmosphereRequest req = createMock(AtmosphereRequest.class);
		expect(req.getAttribute(FrameworkConfig.WEBSOCKET_MESSAGE)).andStubReturn("sockets");
		replay(req);
		
		AtmosphereResourceImpl r = createMock(AtmosphereResourceImpl.class);
		expect(r.getRequest()).andStubReturn(req);
		expect(r.getRequest(false)).andStubReturn(req);
		replay(r);
		
		Action result = interceptor.inspect(r);
		Assert.assertEquals(Action.CONTINUE.type(), result.type());
		
	}

	@Test
	public void testInspectWebWithSecurity() {
		
		Map<String, Object> atts = new HashMap<String, Object>();
		atts.put(FrameworkConfig.SECURITY_SUBJECT, true);
		
		AtmosphereRequest req = createMock(AtmosphereRequest.class);
		expect(req.getAttribute(FrameworkConfig.WEBSOCKET_MESSAGE)).andStubReturn(null);
		expect(req.localAttributes()).andStubReturn(atts);
		replay(req);
		
		AtmosphereResourceImpl r = createMock(AtmosphereResourceImpl.class);
		expect(r.getRequest()).andStubReturn(req);
		expect(r.getRequest(false)).andStubReturn(req);
		replay(r);
		
		Action result = interceptor.inspect(r);
		Assert.assertEquals(Action.CONTINUE.type(), result.type());
	}

	@Test
	public void testInspectWebWithoutSecurityPollingTransport() {
		
		Subject subjectUnderTest = new Subject.Builder(getSecurityManager()).buildSubject();
		setSubject(subjectUnderTest);

		Map<String, Object> atts = new HashMap<String, Object>();
		
		AtmosphereRequest req = createMock(AtmosphereRequest.class);
		expect(req.getAttribute(FrameworkConfig.WEBSOCKET_MESSAGE)).andStubReturn(null);
		expect(req.localAttributes()).andStubReturn(atts);
		req.setAttribute(anyObject(String.class), anyObject(Subject.class));
		expectLastCall();
		replay(req);
		
		AtmosphereResponse res = createMock(AtmosphereResponse.class);
		replay(res);
		
		AtmosphereResourceImpl r = createMock(AtmosphereResourceImpl.class);
		expect(r.getRequest()).andStubReturn(req);
		expect(r.getResponse()).andStubReturn(res);
		expect(r.getRequest(false)).andStubReturn(req);
		expect(r.transport()).andStubReturn(TRANSPORT.LONG_POLLING);
		replay(r);
		
		Action result = interceptor.inspect(r);
		Assert.assertEquals(Action.CONTINUE.type(), result.type());
	}

	@Test
	public void testInspectWebWithoutSecurityPollingWebsocket() {
		
		Subject subjectUnderTest = new Subject.Builder(getSecurityManager()).buildSubject();
		setSubject(subjectUnderTest);
		
		AtmosphereRequest req = AtmosphereRequest.wrap(new MockHttpServletRequest());
		AtmosphereResponse res = AtmosphereResponse.wrap(new MockHttpServletResponse());
		
		AtmosphereResourceImpl r = createMock(AtmosphereResourceImpl.class);
		expect(r.getRequest()).andStubReturn(req);
		expect(r.getResponse()).andStubReturn(res);
		expect(r.getRequest(false)).andStubReturn(req);
		expect(r.transport()).andStubReturn(TRANSPORT.WEBSOCKET);
		replay(r);
		
		Action result = interceptor.inspect(r);
		Assert.assertEquals(Action.CONTINUE.type(), result.type());
	}
}
