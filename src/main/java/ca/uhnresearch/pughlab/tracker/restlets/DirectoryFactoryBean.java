package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Component;
import org.restlet.resource.Directory;
import org.springframework.beans.factory.FactoryBean;

public class DirectoryFactoryBean implements FactoryBean<Directory> {
    private Component component;
    private String resourceRoot;

	public Directory getObject() throws Exception {
        Directory directory = new Directory(component.getContext(), getResourceRoot());
        directory.setListingAllowed(true);
        return directory;
    }

    public Class<?> getObjectType() {
        return Directory.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getResourceRoot() {
		return resourceRoot;
	}

	public void setResourceRoot(String resourceRoot) {
		this.resourceRoot = resourceRoot;
	}
}