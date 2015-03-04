package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.resource.Directory;
import org.springframework.beans.factory.FactoryBean;

public class DirectoryFactoryBean implements FactoryBean<Directory> {
    private Component component;

    public Directory getObject() throws Exception {
        Directory directory = new Directory(component.getContext(), "war:///") {
            @Override
            public Context getContext() {
                // Display if the client dispatcher is correctly set!
                System.out.println(">> getContext().getClientDispatcher() = "+super.getContext().getClientDispatcher());

                return super.getContext();
            }
        };
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
}