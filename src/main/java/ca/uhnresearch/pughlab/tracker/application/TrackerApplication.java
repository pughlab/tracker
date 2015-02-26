package ca.uhnresearch.pughlab.tracker.application;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

public class TrackerApplication extends Application {
	
	@Override
    public Restlet createInboundRoot() {
		
		Router router = new Router(getContext());
		
		Restlet account = new Restlet() {
		    @Override
		    public void handle(Request request, Response response) {
		        // Print the requested URI path
		        String message = "Account of user \""
		                + request.getAttributes().get("user") + "\"";
		        response.setEntity(message, MediaType.TEXT_PLAIN);
		    }
		};
		
		router.attach("/users/{user}", account);
		
		return router;
    }
}
