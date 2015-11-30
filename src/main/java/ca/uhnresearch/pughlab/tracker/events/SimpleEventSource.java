package ca.uhnresearch.pughlab.tracker.events;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleEventSource implements EventSource {

	private final Logger logger = LoggerFactory.getLogger(SimpleEventSource.class);

	private List<EventHandler> handlers = new ArrayList<EventHandler>();
	
	private List<Event> events = new ArrayList<Event>();
	
	private boolean handlingEvents = false;

	/**
	 * Returns the list of handlers.
	 * @return the handlers
	 */
	public List<EventHandler> getHandlers() {
		return handlers;
	}

	/**
	 * Sets the list of handlers.
	 * @param handlers the handlers to set
	 */
	public void setHandlers(List<EventHandler> handlers) {
		this.handlers = handlers;
	}

	/**
	 * Send the message to all the event handlers
	 * @param event
	 * @param scope
	 */
	@Override
	public void doEvent(Event event) {
		events.add(event);
		
		// If we're handling events, already, then simply return. Otherwise
		// we can send the queued events. 
		if (! handlingEvents) {
			sendQueuedEvents();
		}
	}
	
	private void sendQueuedEvents() {
		handlingEvents = true;
		while(! events.isEmpty()) {
			Event next = events.remove(0);
			sendQueuedEvent(next);
		}
		handlingEvents = false;
	}
	
	private void sendQueuedEvent(Event event) {
		for(EventHandler handler : handlers) {
			try {
				handler.sendMessage(event);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}
}
