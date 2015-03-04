package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.ext.spring.SpringServerServlet;
import org.restlet.util.ClientList;

public class CustomSpringServerServlet extends SpringServerServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void init(Application application) {
        super.init(application);
    }

    @Override
    protected void init(Component component) {
        super.init(component);

        ClientList list = component.getClients();
        for (Client client : list) {
            System.out.println(">> client = "+client);
        }
    }

}
