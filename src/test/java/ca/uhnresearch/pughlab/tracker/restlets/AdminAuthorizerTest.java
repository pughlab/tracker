package ca.uhnresearch.pughlab.tracker.restlets;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;

import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class AdminAuthorizerTest extends AbstractShiroTest {
	
	private AdminAuthorizer authorizer;
	private Request request;
	private Response response;
	private Subject subjectUnderTest;

	@Before
	public void initialize() {
		authorizer = new AdminAuthorizer();

        subjectUnderTest = createMock(Subject.class);
        request = createMock(Request.class);
        response = createMock(Response.class);
        
        replay(request);
        replay(response);
	}
	
	@Test
	public void testAllowed() {

        expect( subjectUnderTest.isPermitted("admin") ).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

        assertTrue( authorizer.authorize(request, response) );

	}

	@Test
	public void testDenied() {

        expect( subjectUnderTest.isPermitted("admin") ).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);
        
        assertFalse( authorizer.authorize(request, response) );

	}
}
