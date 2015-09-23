package ca.uhnresearch.pughlab.tracker.events;

import java.util.ArrayList;
import java.util.List;

public class MultipleEventDispatcher implements EventHandler {
	
	private List<EventHandler> handlers = new ArrayList<EventHandler>();

	/**
	 * Send the message to all the event handlers
	 */
	@Override
	public void sendMessage(Event event, String scope) {
		for(EventHandler handler : handlers) {
			handler.sendMessage(event, scope);
		}
	}

	/**
	 * @return the handlers
	 */
	public List<EventHandler> getHandlers() {
		return handlers;
	}

	/**
	 * @param handlers the handlers to set
	 */
	public void setHandlers(List<EventHandler> handlers) {
		this.handlers = handlers;
	}

}
