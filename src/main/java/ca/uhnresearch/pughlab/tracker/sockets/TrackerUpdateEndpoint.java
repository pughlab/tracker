package ca.uhnresearch.pughlab.tracker.sockets;

import java.io.IOException;
import java.util.logging.Logger;
 
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.server.ServerEndpoint;

/**
 * Fairly standards-conforming Java Websocket API implementation, used to connect to 
 * the server for handling connections to and from the tracker core. How this is
 * integrated into the web container is to some extent open.
 */
@ServerEndpoint(value = "/events")
public class TrackerUpdateEndpoint {
 
    private Logger logger = Logger.getLogger(this.getClass().getName());
 
    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
    }
 
    @OnMessage
    public String onMessage(String message, Session session) {
    	if ("quit".equals(message)) {
            try {
                session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Request servlet session"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    	return message;
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }
}