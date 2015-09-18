package ca.uhnresearch.pughlab.tracker.restlets;

import java.util.Date;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;

public class DefaultingFilterFactoryBeanTest {

	@Test
	public void testFactoryBean() throws Exception {
		
		Restlet next = createMock(Restlet.class);
		replay(next);
		
		Context context = createMock(Context.class);
		replay(context);
		
		Component component = createMock(Component.class);
		expect(component.getContext()).andStubReturn(context);
		replay(component);
		
		DefaultingFilterFactoryBean bean = new DefaultingFilterFactoryBean();
		bean.setComponent(component);

		Assert.assertThat(bean.getComponent(), equalTo(component));

		bean.setDefaultPath("/test");
		Assert.assertEquals("/test", bean.getDefaultPath());

		bean.setNext(next);
		Assert.assertEquals(next, bean.getNext());

		Assert.assertEquals(DefaultingFilter.class, bean.getObjectType());
		
		Object result = bean.getObject();
		Assert.assertThat(result, IsInstanceOf.instanceOf(DefaultingFilter.class));
		
		DefaultingFilter filter = (DefaultingFilter) result;
		Assert.assertEquals("/test", filter.getDefaultPath());
		Assert.assertEquals(context, filter.getContext());
		Assert.assertEquals(next, filter.getNext());
	}
}
