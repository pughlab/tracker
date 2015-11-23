package ca.uhnresearch.pughlab.tracker.events;

import java.util.ArrayList;
import java.util.List;

/**
 * A special type of EventHandler that dispatches to multiple other event handlers.
 * This is important in configuration, as it enables a number of event handlers to
 * listen to important changes at the same time. 
 * 
 * @author stuartw
 */
public class MultipleEventDispatcher implements EventHandler {
	
	private List<EventHandler> handlers = new ArrayList<EventHandler>();

	/**
	 * Send the message to all the event handlers
	 * @param event
	 * @param scope
	 */
	@Override
	public void sendMessage(Event event, String scope) {
		for(EventHandler handler : handlers) {
			handler.sendMessage(event, scope);
		}
	}

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
}
