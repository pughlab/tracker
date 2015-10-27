package ca.uhnresearch.pughlab.tracker.application;

import static org.easymock.EasyMock.*;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;

public class TrackerApplicationTest {

	@Test
	public void testNewApplication() {
		
		Application app = new TrackerApplication();
		
		Context context = createMock(Context.class);
		replay(context);
		
		Restlet root = createMock(Restlet.class);
		expect(root.getContext()).andStubReturn(context);
		replay(root);
		
		app.setInboundRoot(root);
		
		Assert.assertEquals(root, app.getInboundRoot());
	}
}
