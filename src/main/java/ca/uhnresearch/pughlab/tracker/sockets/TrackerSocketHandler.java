package ca.uhnresearch.pughlab.tracker.sockets;

import org.apache.shiro.subject.Subject;
import org.atmosphere.config.service.DeliverTo;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.interceptor.ShiroInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

@Configurable
@ManagedService(path = "/events",
                interceptors = {
		AtmosphereResourceLifecycleInterceptor.class,
		HeartbeatInterceptor.class,
		SuspendTrackerInterceptor.class,
		ShiroInterceptor.class
})
public class TrackerSocketHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UpdateEventManager eventManager;


	@Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        logger.info("Heartbeat send by {}", event.getResource());
    }

    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     *
     * @param r
     */
    @Ready
    public void onReady(final AtmosphereResource r) {

        logger.info("Browser {} connected", r.uuid());
        
        Subject subject = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
        logger.info("Subject: {}", subject.getPrincipal());
        
        // When we are ready, we should actually send a welcome message to the client. This starts off
        // much of the protocol.
        
        UpdateEvent event = new UpdateEvent(null, UpdateEvent.WELCOME_EVENT);
        event.setRecipient(subject.getPrincipal().toString());
        
        ObjectMapper mapper = new ObjectMapper();

        try {
        	// Sends to just this one resource -- correct for a welcome event
			r.getBroadcaster().broadcast(mapper.writeValueAsString(event), r);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     *
     * @param event
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            logger.info("Browser {} unexpectedly disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            logger.info("Browser {} closed the connection", event.getResource().uuid());
        }
    }

    /**
	 * Handles a message from the client. This is where most of the actual logic goes here. 
	 * 
     * @param message an instance of {@link UpdateEvent}
     * @return
     * @throws IOException
     */
    @Message
    public String onMessage(AtmosphereResource r, String input) throws IOException {
    	
    	ObjectMapper mapper = new ObjectMapper();
    	UpdateEvent message = mapper.readValue(input, UpdateEvent.class);

        Subject subject = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
        logger.info("Subject: {}", subject.getPrincipal());
        logger.info("Event type: {}", message.getClass().getCanonicalName());
        logger.info("Event manager: {}", eventManager);

        logger.info("{} just sent {}", message.getSender(), message.getType());
                
        return input;
    }

	public UpdateEventManager getEventManager() {
		return eventManager;
	}

	@Inject
	@Named("trackerUpdateManager")
	public void setEventManager(UpdateEventManager eventManager) {
		logger.info("Setting eventManager to: {}", eventManager);
		this.eventManager = eventManager;
	}
}