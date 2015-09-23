package ca.uhnresearch.pughlab.tracker.sockets;

import static org.easymock.EasyMock.*;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.events.Event;

public class SocketEventHandlerTest {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private SocketEventHandler service;

	@Before
	public void initialize() {
		service = new SocketEventHandler();
	}
	
	private AtmosphereResource createMockedResource(String uuid, AtmosphereRequest request) {
        AtmosphereResource resource = createMock(AtmosphereResource.class);
        expect(resource.uuid()).andStubReturn(uuid);
        expect(resource.getRequest()).andStubReturn(request);
        return resource;
	}
	
	private AtmosphereRequest createMockedRequest(Subject subject) {
		AtmosphereRequest request = createMock(AtmosphereRequest.class);
		expect(request.getAttribute(FrameworkConfig.SECURITY_SUBJECT)).andStubReturn(subject);
        replay(request);
        return request;
	}
	
	private Subject createMockedSubject(String name) {
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.getPrincipals()).andStubReturn(new SimplePrincipalCollection("stuart", "mock"));
        replay(subjectUnderTest);
        return subjectUnderTest;
	}
	
	private AtmosphereResource getResource1() {
        Subject subject = createMockedSubject("stuart");
		AtmosphereRequest request = createMockedRequest(subject);
        return createMockedResource("736097A6-1EE1-421C-809C-C31522E0A3EC", request);
	}
		
	private AtmosphereResource getResource2() {
        Subject subject = createMockedSubject("morag");
		AtmosphereRequest request = createMockedRequest(subject);
        return createMockedResource("0B53896A-35CD-4C6A-8531-99A47E398D04", request);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testRegister() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.registerAtmosphereResource(resource1);

        verify(resource1);
	}

	@Test
	public void testRegisterInvalid() {
        AtmosphereResource resource = createMock(AtmosphereResource.class);
        expect(resource.uuid()).andStubReturn(null);
        replay(resource);

		thrown.expect(IllegalArgumentException.class);

        service.registerAtmosphereResource(resource);
	}

	@Test
	public void testUnregisterWithoutRegistering() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.unregisterAtmosphereResource(resource1);

        verify(resource1);
	}

	@Test
	public void testUnregisterInvalid() {
        AtmosphereResource resource = createMock(AtmosphereResource.class);
        expect(resource.uuid()).andStubReturn(null);
        replay(resource);

		thrown.expect(IllegalArgumentException.class);

        service.unregisterAtmosphereResource(resource);
	}

	@Test
	public void testUnregisterAfterRegistering() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.registerAtmosphereResource(resource1);
        service.unregisterAtmosphereResource(resource1);

        verify(resource1);
	}
	
	@Test
	public void testJoin() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.registerAtmosphereResource(resource1);

        Event joinEvent = new Event();
        joinEvent.setType(Event.EVENT_JOIN);
        joinEvent.getData().setScope("TEST");
        
        service.receivedMessage(joinEvent, resource1);

        verify(resource1);
}

	@Test
	public void testJoinNotify() {
        AtmosphereResource resource1 = getResource1();
        expect(resource1.write(EasyMock.find("\"type\":\"userconnect\""))).andReturn(resource1);
        replay(resource1);

        AtmosphereResource resource2 = getResource2();
        expect(resource2.write(EasyMock.find("\"type\":\"userconnect\""))).andReturn(resource1);
        replay(resource2);
        
        service.registerAtmosphereResource(resource1);
        service.registerAtmosphereResource(resource2);
        
        Event joinEvent = new Event();
        joinEvent.setType(Event.EVENT_JOIN);
        joinEvent.getData().setScope("TEST");
        
        service.receivedMessage(joinEvent, resource1);
        service.receivedMessage(joinEvent, resource2);
        
        verify(resource1);
        verify(resource2);
	}

	@Test
	public void testJoinAndLeaveNotify() {
        AtmosphereResource resource1 = getResource1();
        expect(resource1.write(EasyMock.find("\"type\":\"userconnect\""))).andReturn(resource1);
        expect(resource1.write(EasyMock.find("\"type\":\"userdisconnect\""))).andReturn(resource1);
        replay(resource1);

        AtmosphereResource resource2 = getResource2();
        expect(resource2.write(EasyMock.find("\"type\":\"userconnect\""))).andReturn(resource2);
        replay(resource2);
        
        service.registerAtmosphereResource(resource1);
        service.registerAtmosphereResource(resource2);
        
        Event joinEvent = new Event();
        joinEvent.setType(Event.EVENT_JOIN);
        joinEvent.getData().setScope("TEST");
        
        service.receivedMessage(joinEvent, resource1);
        service.receivedMessage(joinEvent, resource2);
        service.unregisterAtmosphereResource(resource2);

        verify(resource1);
        verify(resource2);
	}
	
	/**
	 * Regression test for #76. Replicates a resource being removed inconsistently, and 
	 * still getting events sent incorrectly. 
	 */
	@Test
	public void testSendMessageSocketException() {
        AtmosphereResource resource = getResource1();
        expect(resource.write(anyObject(String.class))).andStubReturn(resource);
        replay(resource);
        
        service.registerAtmosphereResource(resource);
        
        Event joinEvent = new Event();
        joinEvent.setType(Event.EVENT_JOIN);
        joinEvent.getData().setScope("TEST");
        
        Event otherEvent = new Event();
        otherEvent.setType(Event.EVENT_JOIN);
        otherEvent.getData().setScope("OTHER");

        Event notifyEvent = new Event();
        notifyEvent.setType(Event.EVENT_SET_FIELD);

        service.receivedMessage(joinEvent, resource);
        service.receivedMessage(otherEvent, resource);
        service.receivedMessage(joinEvent, resource);

        service.unregisterAtmosphereResource(resource);
        service.sendMessage(notifyEvent, "TEST");
   	}
}
