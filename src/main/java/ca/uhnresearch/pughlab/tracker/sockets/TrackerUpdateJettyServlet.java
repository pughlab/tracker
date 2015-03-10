package ca.uhnresearch.pughlab.tracker.sockets;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * A Jetty servlet implementation, which handles the protocol upgrades needed to link 
 * the TrackerUpdateEndpoint into the main system, so we can handle persistent socket
 * connections alongside regular requests. Our endpoint should be pure JSR-356, so that
 * we can more easily connect into different containers in some way. This is a bridging
 * servlet that will only work with Jetty. It should, therefore, really be a separate
 * component that gets integrated later, and only for a Jetty war file deployment. 
 */
@SuppressWarnings("serial")
@WebServlet(name = "Tracker WebSocket Servlet", urlPatterns = { "/events" })
public class TrackerUpdateJettyServlet extends WebSocketServlet {

	/**
	 * Registers the TrackerUpdateEndpoint endpoint. 
	 */
	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(10000);
		factory.register(TrackerUpdateEndpoint.class);
	}
}
