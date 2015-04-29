package ca.uhnresearch.pughlab.tracker.sockets;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.junit.Before;
import org.junit.Test;

public class SocketEventServiceTest {
	
	private SocketEventService service;

	@Before
	public void initialize() {
		service = new SocketEventService();
	}
		
	@Test
	public void testRegister() {
        AtmosphereResource resource1 = createMock(AtmosphereResource.class);
        expect(resource1.uuid()).andStubReturn("736097A6-1EE1-421C-809C-C31522E0A3EC");
        replay(resource1);
        
        service.registerAtmosphereResource(resource1);
	}

	@Test
	public void testUnregister() {
		
        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.getPrincipal()).andStubReturn("stuart");
        replay(subjectUnderTest);

		AtmosphereRequest request1 = createMock(AtmosphereRequest.class);
		expect(request1.getAttribute(FrameworkConfig.SECURITY_SUBJECT)).andStubReturn(subjectUnderTest);
        replay(request1);
		
        AtmosphereResource resource1 = createMock(AtmosphereResource.class);
        expect(resource1.uuid()).andStubReturn("736097A6-1EE1-421C-809C-C31522E0A3EC");
        expect(resource1.getRequest()).andStubReturn(request1);
        replay(resource1);
        
        service.unregisterAtmosphereResource(resource1);
	}
}
