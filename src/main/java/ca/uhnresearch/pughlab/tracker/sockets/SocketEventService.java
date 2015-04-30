package ca.uhnresearch.pughlab.tracker.sockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class SocketEventService {
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
		if (scope == null) {
			throw new IllegalArgumentException("Can't send to a null scope");
		}
		
		logger.info("Sending message to everyone watching: {}", scope);
		List<String> resourceKeys = watcherListByScope.get(scope);
		if (resourceKeys != null) {
			for (String uuid : resourceKeys) {
				AtmosphereResource r = resources.get(uuid);
        		if (r == null) {
        			logger.error("Whoa! Something removed a resource: {}, {}", uuid, r);
        			resources.remove(uuid);
        			throw new RuntimeException("Whoa! Something removed a resource: " + uuid);
        		}
        		if (r.getRequest() == null) {
        			logger.error("Whoa! Something removed a resource request for: {}, {}", uuid, r);
        			resources.remove(uuid);
        			throw new RuntimeException("Whoa! Something removed a resource request: " + uuid);
        		}
				sendMessage(event, r);
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
        		
        		logger.info("Sending to scope watchers");
        		UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_USER_CONNECTED);
        		event.getData().setUser(subject.getPrincipal().toString());
        		event.getData().setScope(scope);
        		sendMessage(event, scope);
        		
        		// And now, before we add the new user, we need to tell them about everyone
        		// else. 
        		List<String> resourceKeys = watcherListByScope.get(scope);
        		logger.info("Existing: {}", resourceKeys);
        		if (resourceKeys != null) {
        			for (String uuid : resourceKeys) {
        				AtmosphereResource other = resources.get(uuid);
                		if (other == null) {
                			logger.error("Whoa! Something removed a resource: {}, {}", uuid, other);
                			resources.remove(uuid);
                			throw new RuntimeException("Whoa! Something removed a resource: " + uuid);
                		}
                		if (other.getRequest() == null) {
                			logger.error("Whoa! Something removed a resource request for: {}, {}", uuid, other);
                			resources.remove(uuid);
                			throw new RuntimeException("Whoa! Something removed a resource request: " + uuid);
                		}

                		// Get the other user
                		Subject otherSubject = (Subject) other.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
                		String otherUser = otherSubject.getPrincipal().toString();
                		event.getData().setUser(otherUser);
                		sendMessage(event, r);
        			}
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
		if (uuid == null) {
			throw new IllegalArgumentException("Can't register a resource without a UUID");
		}
		
		logger.info("Registering AtmosphereResource: {}", uuid);
		resources.put(uuid, resource);
	}
	
	/**
	 * Removes a registered UUID and AtmosphereResource association.
	 * @param uuid
	 */
	public void unregisterAtmosphereResource(AtmosphereResource resource) {
		String uuid = resource.uuid();
		if (uuid == null) {
			throw new IllegalArgumentException("Can't register a resource without a UUID");
		}

		logger.info("Unregistering AtmosphereResource: {}", uuid);
		
		String scope = scopeByWatcher.get(uuid);
		if (scope != null) {
			logger.info("Found scope being watched: {}", scope);
			
			List<String> watchers = watcherListByScope.get(scope);
			if (! watchers.remove(uuid)) {
				throw new RuntimeException("Failed to remove watcher: " + uuid);
			}
			
			scopeByWatcher.remove(uuid);
		}
		
		resources.remove(uuid);
		
		logger.info("After removal: registered resources");
		for(Entry<String, AtmosphereResource> entry : resources.entrySet()) {
			logger.info("Found: {} => {}", entry.getKey(), entry.getValue());
		}
		
		// And after we have disconnected, tell everyone else we are gone.
		if (scope != null) {
	        Subject subject = (Subject) resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
			UpdateEvent event = new UpdateEvent(UpdateEvent.EVENT_USER_DISCONNECTED);
			event.getData().setUser(subject.getPrincipal().toString());
			event.getData().setScope(scope);
			sendMessage(event, scope);
		}
	}
}
