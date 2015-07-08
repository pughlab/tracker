package ca.uhnresearch.pughlab.tracker.restlets;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.capture;

import org.apache.shiro.subject.Subject;
import org.easymock.Capture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class CustomSpringSessionAuthenticatorTest extends AbstractShiroTest {

	private CustomSpringSessionAuthenticator authenticator;

	private Request request;
	private Response response;
	private Context context;
	private Subject subjectUnderTest;

	@Before
	public void initialize() {
		
		Restlet parent = createMock(Restlet.class);
		context = createMock(Context.class);
		expect(context.getDefaultEnroler()).andStubReturn(null);
		replay(context);
		
		expect(parent.getContext()).andStubReturn(context);
		replay(parent);
		
        request = createMock(Request.class);
        replay(request);
        response = createMock(Response.class);
        replay(response);
        
        subjectUnderTest = createMock(Subject.class);
        setSubject(subjectUnderTest);
        
		authenticator = new CustomSpringSessionAuthenticator(parent);
	}
	
	@Test
	public void testContext() {
		Assert.assertEquals(context, authenticator.getContext());
	}

	@Test
	public void testAuthenticateWithoutPrincipal() {
		String principal = null;
		expect(subjectUnderTest.getPrincipal()).andStubReturn(principal);
		replay(subjectUnderTest);
		
		Assert.assertFalse( authenticator.authenticate(request, response) );
		
		verify(subjectUnderTest);
	}

	@Test
	public void testAuthenticateWithPrincipal() {
		String principal = "admin";
		expect(subjectUnderTest.getPrincipal()).andStubReturn(principal);
		replay(subjectUnderTest);
		
		ClientInfo clientInfo = new ClientInfo();
		
		Capture<ChallengeResponse> capturedArgument = new Capture<ChallengeResponse>();
		
		reset(request);
		expect(request.getClientInfo()).andStubReturn(clientInfo);
		request.setChallengeResponse(capture(capturedArgument));
		expectLastCall();		
		replay(request);
		
		Assert.assertTrue( authenticator.authenticate(request, response) );
		Assert.assertEquals("admin", capturedArgument.getValue().getIdentifier());
		
		verify(subjectUnderTest);
		verify(request);
	}
}
