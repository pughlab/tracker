package ca.uhnresearch.pughlab.tracker.sockets;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.capture;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.FrameworkConfig;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

import com.fasterxml.jackson.databind.JsonMappingException;

public class TrackerSocketHandlerTest extends AbstractShiroTest {
	
	TrackerSocketHandler handler;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		handler = new TrackerSocketHandler();

		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
	}

	@After
	public void tearDown() {
		setSecurityManager(null);
		handler = null;
	}
	
	@Test
	public void testHeartbeat() {
		AtmosphereResourceEvent event = createMock(AtmosphereResourceEvent.class);
		replay(event);
		handler.onHeartbeat(event);
	}
	
	@Test
	public void testGetSetEventManager() {
		SocketEventService manager = createMock(SocketEventService.class);
		replay(manager);
		
		handler.setEventManager(manager);
		
		SocketEventService retrieved = handler.getEventManager();
		Assert.assertEquals(manager, retrieved);
	}
	
	@Test
	public void testinvalidMessage() throws IOException {
		AtmosphereResource resource = createMock(AtmosphereResource.class);
		replay(resource);

		thrown.expect(JsonMappingException.class);

		String input = "";
		handler.onMessage(resource, input);
	}
	
	@Test
	public void testDisconnect() {
		AtmosphereResource resource = createMock(AtmosphereResource.class);
		expect(resource.uuid()).andStubReturn("B37815E9-1ACE-4C7D-8741-1ADECC2B5532");
		replay(resource);
		
		SocketEventService manager = createMock(SocketEventService.class);
		manager.unregisterAtmosphereResource(resource);
		expectLastCall();
		replay(manager);
		
		handler.setEventManager(manager);
		
		AtmosphereResourceEvent event = createMock(AtmosphereResourceEvent.class);
		expect(event.isCancelled()).andStubReturn(true);
		expect(event.getResource()).andStubReturn(resource);
		replay(event);
		handler.onDisconnect(event);
		
		verify(manager);
		verify(resource);
	}
	
	@Test
	public void testDisconnectClosed() {
		AtmosphereResource resource = createMock(AtmosphereResource.class);
		expect(resource.uuid()).andStubReturn("B37815E9-1ACE-4C7D-8741-1ADECC2B5532");
		replay(resource);
		
		SocketEventService manager = createMock(SocketEventService.class);
		manager.unregisterAtmosphereResource(resource);
		expectLastCall();
		replay(manager);
		
		handler.setEventManager(manager);
		
		AtmosphereResourceEvent event = createMock(AtmosphereResourceEvent.class);
		expect(event.isCancelled()).andStubReturn(false);
		expect(event.isClosedByClient()).andStubReturn(true);
		expect(event.getResource()).andStubReturn(resource);
		replay(event);
		handler.onDisconnect(event);
		
		verify(manager);
		verify(resource);
	}
	
	@Test
	public void testDisconnectOther() {
		AtmosphereResource resource = createMock(AtmosphereResource.class);
		expect(resource.uuid()).andStubReturn("B37815E9-1ACE-4C7D-8741-1ADECC2B5532");
		replay(resource);
		
		SocketEventService manager = createMock(SocketEventService.class);
		manager.unregisterAtmosphereResource(resource);
		expectLastCall();
		replay(manager);
		
		handler.setEventManager(manager);
		
		AtmosphereResourceEvent event = createMock(AtmosphereResourceEvent.class);
		expect(event.isCancelled()).andStubReturn(false);
		expect(event.isClosedByClient()).andStubReturn(false);
		expect(event.getResource()).andStubReturn(resource);
		replay(event);
		handler.onDisconnect(event);
		
		verify(manager);
		verify(resource);
	}

	@Test
	public void testReady() {
		
		Subject subjectUnderTest = new Subject.Builder(getSecurityManager()).principals(new SimplePrincipalCollection("stuart", "mock")).buildSubject();
		setSubject(subjectUnderTest);

		AtmosphereRequest req = AtmosphereRequest.wrap(new MockHttpServletRequest());
		req.setAttribute(FrameworkConfig.SECURITY_SUBJECT, subjectUnderTest);

		AtmosphereResource resource = createMock(AtmosphereResource.class);
		expect(resource.uuid()).andStubReturn("B37815E9-1ACE-4C7D-8741-1ADECC2B5532");
		expect(resource.getRequest()).andStubReturn(req);
		replay(resource);
		
		Capture<UpdateEvent> capturedEvent = EasyMock.newCapture(CaptureType.FIRST);
		Capture<AtmosphereResource> capturedResource = EasyMock.newCapture(CaptureType.FIRST);

		SocketEventService manager = createMock(SocketEventService.class);
		manager.registerAtmosphereResource(resource);
		expectLastCall();
		manager.sendMessage(capture(capturedEvent), capture(capturedResource));
		replay(manager);
		
		handler.setEventManager(manager);

		handler.onReady(resource);

		verify(manager);
		verify(resource);
		
		Assert.assertEquals(resource, capturedResource.getValue());
		Assert.assertEquals(UpdateEvent.EVENT_WELCOME, capturedEvent.getValue().getType());
	}
	
	@Test
	public void testReadyWithoutPrincipal() {
		
		AtmosphereRequest req = AtmosphereRequest.wrap(new MockHttpServletRequest());

		AtmosphereResource resource = createMock(AtmosphereResource.class);
		expect(resource.uuid()).andStubReturn("B37815E9-1ACE-4C7D-8741-1ADECC2B5532");
		expect(resource.getRequest()).andStubReturn(req);
		replay(resource);

		SocketEventService manager = createMock(SocketEventService.class);
		manager.registerAtmosphereResource(resource);
		expectLastCall();
		replay(manager);

		handler.setEventManager(manager);

		handler.onReady(resource);

		verify(manager);
		verify(resource);
	}
}
