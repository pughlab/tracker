package ca.uhnresearch.pughlab.tracker.sockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles socket update events for the tracker web application. This should provide an
 * event context for the whole application, and should be used as a singleton for the
 * application. 
 */
public class SocketEventServer {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, AtmosphereResource> resources = new HashMap<String, AtmosphereResource>();
	
	private Map<String, List<String>> watcherListByScope = new HashMap<String, List<String>>();
	private Map<String, String> scopeByWatcher = new HashMap<String, String>();
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Sends a message to a connected resource. This includes rendering the message to JSON and
	 * sending it to the connected resource.
	 * @param event
	 * @param r
	 */
	public void sendMessage(UpdateEvent event, AtmosphereResource r) {
        try {
        	String messageBody = mapper.writeValueAsString(event);
        	logger.info("Sending to: {}, {}", r.uuid(), messageBody);
        	// Sends to just this one resource -- correct for a welcome event
        	r.write(messageBody);
			
		} catch (JsonProcessingException e) {
			logger.error("Can't send: {}", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to all connected resources with a given scope.
	 */
	public void sendMessage(UpdateEvent event, String scope) {
		assert scope != null;
		logger.info("Sending message to everyone watching: {}", scope);
		List<String> resourceKeys = watcherListByScope.get(scope);
		if (resourceKeys != null) {
			for (String key : resourceKeys) {
				AtmosphereResource r = resources.get(key);
				if (r == null) {
					logger.error("Can't send message to missing resource: {}", key);
				} else {
					logger.info("Sending message to resource: {}", r.uuid());
					sendMessage(event, r);
				}
			}
		}
	}
	
	/**
	 * Handles an event from a connected resource. 
	 * @param message
	 * @param r
	 */
	public void receivedMessage(UpdateEvent message, AtmosphereResource r) {
        Subject subject = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
        try {
			logger.info("{} just sent {}", subject.getPrincipal(), mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			logger.error("Can't send: {}", e);
			e.printStackTrace();
		}
        
        if (message.getType().equals(UpdateEvent.EVENT_JOIN)) {
        	// We're joining a study, add that to our associations
        	String resourceKey = r.uuid();
        	String scope = message.getData().getScope();
        	
        	logger.info("Connecting to scope: {}", scope);
        	scopeByWatcher.put(resourceKey,scope);
        	if (! watcherListByScope.containsKey(scope)) {
        		watcherListByScope.put(scope, new ArrayList<String>());
        	} else {
        		// Before we record this user, tell everyone else someone new has connected
        		
        		UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_USER_CONNECTED);
        		event.getData().setUser(subject.getPrincipal().toString());
        		event.getData().setScope(scope);
        		sendMessage(event, scope);
        		
        		// Also, on a join, we want to tell the newly connected resource about
        		// everyone already connected.
        		List<String> uuids = watcherListByScope.get(scope);
        		for (String uuid : uuids) {        			
            		AtmosphereResource other = resources.get(uuid);
            		UpdateEvent otherEvent = new UpdateEvent(UpdateEvent.EVENT_USER_CONNECTED);
            		Subject otherSubject = (Subject) other.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
            		otherEvent.getData().setUser(otherSubject.getPrincipal().toString());
            		otherEvent.getData().setScope(scope);
            		sendMessage(otherEvent, r);
        		}
        	}
        	
        	watcherListByScope.get(scope).add(resourceKey);
        	logger.info("Now watching: {}: {}", resourceKey, scope);
        }
	}
	
	/**
	 * Adds a new association between a UUID and an AtmosphereResource, which allows us to
	 * notify when we get an event. Messages can be sent to a single AtmosphereResource
	 * (with a UUID) or to all of them. 
	 * 
	 * @param uuid
	 * @param resource
	 */
	public void registerAtmosphereResource(AtmosphereResource resource) {
		String uuid = resource.uuid();
		logger.info("Registering AtmosphereResource: {}", uuid);
		resources.put(uuid, resource);
	}
	
	/**
	 * Removes a registered UUID and AtmosphereResource association.
	 * @param uuid
	 */
	public void unregisterAtmosphereResource(AtmosphereResource resource) {
		String uuid = resource.uuid();
		logger.info("Unregistering AtmosphereResource: {}", uuid);
		
		String scope = scopeByWatcher.get(uuid);
		if (scope != null) {
			logger.info("Found scope being watched: {}", scope);
			
			List<String> watchers = watcherListByScope.get(scope);
			assert watchers.remove(uuid);
			
			scopeByWatcher.remove(uuid);
		}
		
		resources.remove(uuid);
		
		// And after we have disconnected, tell everyone else we are gone.
        Subject subject = (Subject) resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
		UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_USER_DISCONNECTED);
		event.getData().setUser(subject.getPrincipal().toString());
		event.getData().setScope(scope);
		sendMessage(event, scope);
	}
}
