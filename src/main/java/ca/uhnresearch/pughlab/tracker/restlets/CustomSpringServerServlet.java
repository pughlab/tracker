package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.spring.SpringServerServlet;

public class CustomSpringServerServlet extends SpringServerServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @Override
    protected void init(Component component) {
        super.init(component);

        // Add the file protocol. We can't ever actually do this from the web.xml
        // because Spring means we have a real component to initialize from, and 
        // ServerServlet only uses the client settings when we are using an implicit
        // component. See: http://restlet.com/technical-resources/restlet-framework/javadocs/snapshot/jee/ext/org/restlet/ext/servlet/ServerServlet.html
        
        component.getClients().add(Protocol.FILE);
    }
}
