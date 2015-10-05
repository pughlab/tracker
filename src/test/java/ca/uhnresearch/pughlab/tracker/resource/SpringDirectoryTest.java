package ca.uhnresearch.pughlab.tracker.resource;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.junit.Assert;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Reference;

public class SpringDirectoryTest {

	/**
	 * Check initialization with a Context
	 */
	@Test
	public void testContext() {
		Context context = createMock(Context.class);
		replay(context);
		
		Reference reference = new Reference("file:///directory");
		
		SpringDirectory directory = new SpringDirectory(context, reference);
		Assert.assertNotNull(directory);
		
		Assert.assertEquals(context, directory.getContext());
	}

	/**
	 * Check initialization with a Restlet
	 */
	@Test
	public void testRestlet() {
		
		Context context = createMock(Context.class);
		replay(context);
		
		Restlet restlet = createMock(Restlet.class);
		expect(restlet.getContext()).andStubReturn(context);
		replay(restlet);
		
		Reference reference = new Reference("file:///directory");
		
		SpringDirectory directory = new SpringDirectory(restlet, reference);
		Assert.assertNotNull(directory);
	}

	/**
	 * Check we get the root reference, note the trailing slash
	 */
	@Test
	public void testRootRef() {
		Context context = createMock(Context.class);
		replay(context);
		
		Reference reference = new Reference("file:///directory");
		
		SpringDirectory directory = new SpringDirectory(context, reference);
		
		Reference rootRef = directory.getRootRef();
		Assert.assertNotNull(rootRef);
		
		Assert.assertEquals("file:///directory/", rootRef.toString());
	}

}
