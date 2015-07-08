package ca.uhnresearch.pughlab.tracker.events.impl;

import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;
import ca.uhnresearch.pughlab.tracker.events.UpdateEventService;
import ca.uhnresearch.pughlab.tracker.sockets.SocketEventService;

public class SocketUpdateEventService implements UpdateEventService {
	
	private SocketEventService server;

	@Override
	public void sendMessage(UpdateEvent event) {
		server.sendMessage(event, event.getData().getScope());
	}

	/**
	 * @return the eventManager
	 */
	public SocketEventService getSocketEventService() {
		return server;
	}

	/**
	 * @param eventManager the eventManager to set
	 */
	public void setSocketEventService(SocketEventService eventManager) {
		this.server = eventManager;
	}
	
}
