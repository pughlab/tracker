package ca.uhnresearch.pughlab.tracker.events.impl;

import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventService;
import ca.uhnresearch.pughlab.tracker.sockets.SocketEventHandler;

public class SocketEventService implements EventService {
	
	private SocketEventHandler handler;

	@Override
	public void sendMessage(Event event) {
		handler.sendMessage(event, event.getData().getScope());
	}

	/**
	 * @return the eventManager
	 */
	public SocketEventHandler getSocketEventHandler() {
		return handler;
	}

	/**
	 * @param eventManager the eventManager to set
	 */
	public void setSocketEventHandler(SocketEventHandler eventManager) {
		this.handler = eventManager;
	}
	
}
