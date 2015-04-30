package ca.uhnresearch.pughlab.tracker.sockets;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;

public class SocketEventServiceTest {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private SocketEventService service;

	@Before
	public void initialize() {
		logger.info("Initializing");
		service = new SocketEventService();
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
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
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
		
	@Test
	public void testRegister() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.registerAtmosphereResource(resource1);

        verify(resource1);
	}

	@Test
	public void testUnregisterWithoutRegistering() {
        AtmosphereResource resource1 = getResource1();
        replay(resource1);

        service.unregisterAtmosphereResource(resource1);

        verify(resource1);
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

        UpdateEvent joinEvent = new UpdateEvent();
        joinEvent.setType(UpdateEvent.EVENT_JOIN);
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
        
        UpdateEvent joinEvent = new UpdateEvent();
        joinEvent.setType(UpdateEvent.EVENT_JOIN);
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
        
        UpdateEvent joinEvent = new UpdateEvent();
        joinEvent.setType(UpdateEvent.EVENT_JOIN);
        joinEvent.getData().setScope("TEST");
        
        service.receivedMessage(joinEvent, resource1);
        service.receivedMessage(joinEvent, resource2);
        service.unregisterAtmosphereResource(resource2);

        verify(resource1);
        verify(resource2);
	}
}