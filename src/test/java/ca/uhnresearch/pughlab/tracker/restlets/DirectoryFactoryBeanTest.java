package ca.uhnresearch.pughlab.tracker.restlets;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;

public class DirectoryFactoryBeanTest {

	@Test
	public void testFactoryBean() throws Exception {
		
		Restlet next = createMock(Restlet.class);
		replay(next);
		
		Context context = createMock(Context.class);
		replay(context);
		
		Component component = createMock(Component.class);
		expect(component.getContext()).andStubReturn(context);
		replay(component);
		
		DirectoryFactoryBean bean = new DirectoryFactoryBean();
		bean.setComponent(component);

		Assert.assertThat(bean.getComponent(), equalTo(component));

		bean.setResourceRoot("file:///test/");
		Assert.assertEquals("file:///test/", bean.getResourceRoot());
		
		Assert.assertTrue(bean.isSingleton());

		Assert.assertEquals(Directory.class, bean.getObjectType());
		
		Object result = bean.getObject();
		Assert.assertThat(result, IsInstanceOf.instanceOf(Directory.class));
		
		Directory dir = (Directory) result;
		Assert.assertEquals("file:///test/", dir.getRootRef().toString());
	}
}
