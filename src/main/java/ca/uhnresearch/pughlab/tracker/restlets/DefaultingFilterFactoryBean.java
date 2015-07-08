package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Component;
import org.restlet.Restlet;
import org.springframework.beans.factory.FactoryBean;

public class DefaultingFilterFactoryBean implements FactoryBean<DefaultingFilter> {

    private Component component;
    private String defaultPath;
    private Restlet next;

	@Override
	public DefaultingFilter getObject() throws Exception {
		DefaultingFilter filter = new DefaultingFilter(component.getContext(), next);
		filter.setDefaultPath(defaultPath);
        return filter;
	}

	@Override
	public Class<?> getObjectType() {
		return DefaultingFilter.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

	/**
	 * @return the next
	 */
	public Restlet getNext() {
		return next;
	}

	/**
	 * @param next the next to set
	 */
	public void setNext(Restlet next) {
		this.next = next;
	}

	/**
	 * @return the defaultPath
	 */
	public String getDefaultPath() {
		return defaultPath;
	}

	/**
	 * @param defaultPath the defaultPath to set
	 */
	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}


}
