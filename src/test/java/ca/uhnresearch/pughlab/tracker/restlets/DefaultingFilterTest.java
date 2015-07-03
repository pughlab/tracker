package ca.uhnresearch.pughlab.tracker.restlets;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

public class DefaultingFilterTest {
	
	/**
	 * Checks to see that if the next restlet doesn't produce a success status, then
	 * we try again after modifying the path.
	 * @throws Exception
	 */
	@Test
	public void testDefaultingFilterDefaultPath() throws Exception {
		
		Restlet next = createMock(Restlet.class);
		replay(next);

		DefaultingFilter filter = new DefaultingFilter(null, next);
		
		Assert.assertEquals("/index.html", filter.getDefaultPath());
		
	}

	/**
	 * Checks to see that if the next restlet does produce a success status, then
	 * we just let the handling continue.
	 * @throws Exception
	 */
	@Test
	public void testDefaultingFilterFound() throws Exception {
		
		Reference reference = createMock(Reference.class);
		expect(reference.getPath()).andStubReturn("/test");
		replay(reference);
		
		Request request = createMock(Request.class);
		expect(request.getResourceRef()).andStubReturn(reference);
		replay(request);
		
		Response response = createMock(Response.class);
		expect(response.getStatus()).andStubReturn(Status.SUCCESS_OK);
		replay(response);
		
		Restlet next = createMock(Restlet.class);
		next.start();
		expectLastCall();
		next.handle(request, response);
		expectLastCall();
		replay(next);

		Filter filter = new DefaultingFilter(null, next);
		
		filter.handle(request, response);

		verify(reference);
		verify(request);
		verify(response);
		verify(next);
	}
	
	/**
	 * Checks to see that if the next restlet doesn't produce a success status, then
	 * we try again after modifying the path, this timing using a specified rather
	 * than a default path.
	 * @throws Exception
	 */
	@Test
	public void testDefaultingFilterMissingPathSpecified() throws Exception {
		
		Reference reference = createMock(Reference.class);
		expect(reference.getPath()).andStubReturn("/test");
		reference.setPath("/random.html");
		expectLastCall();
		replay(reference);
		
		Request request = createMock(Request.class);
		expect(request.getResourceRef()).andStubReturn(reference);
		replay(request);
		
		Response response = createMock(Response.class);
		expect(response.getStatus()).andStubReturn(Status.CLIENT_ERROR_NOT_FOUND);
		response.setStatus(Status.SUCCESS_OK);
		expectLastCall();
		replay(response);
		
		Restlet next = createMock(Restlet.class);
		next.start();
		expectLastCall();
		next.handle(request, response);
		expectLastCall();
		next.handle(request, response);
		expectLastCall();
		replay(next);

		DefaultingFilter filter = new DefaultingFilter(null, next);
		filter.setDefaultPath("/random.html");
		
		filter.handle(request, response);

		verify(reference);
		verify(request);
		verify(response);
		verify(next);
	}

	/**
	 * Checks to see that if the next restlet doesn't produce a success status and this
	 * is the default path, we don't retry to avoid infinite retries. 
	 * @throws Exception
	 */
	@Test
	public void testDefaultingFilterMissingDefaultPath() throws Exception {
		
		Reference reference = createMock(Reference.class);
		expect(reference.getPath()).andStubReturn("/random.html");
		replay(reference);
		
		Request request = createMock(Request.class);
		expect(request.getResourceRef()).andStubReturn(reference);
		replay(request);
		
		Response response = createMock(Response.class);
		expect(response.getStatus()).andStubReturn(Status.CLIENT_ERROR_NOT_FOUND);
		replay(response);
		
		Restlet next = createMock(Restlet.class);
		next.start();
		expectLastCall();
		next.handle(request, response);
		expectLastCall();
		replay(next);

		DefaultingFilter filter = new DefaultingFilter(null, next);
		filter.setDefaultPath("/random.html");
		
		filter.handle(request, response);
		
		verify(reference);
		verify(request);
		verify(response);
		verify(next);
	}
}
