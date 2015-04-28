package ca.uhnresearch.pughlab.tracker.events.impl;

import ca.uhnresearch.pughlab.tracker.events.UpdateEvent;
import ca.uhnresearch.pughlab.tracker.events.UpdateEventManager;
import ca.uhnresearch.pughlab.tracker.sockets.SocketEventServer;

public class SocketUpdateEventManager implements UpdateEventManager {
	
	private SocketEventServer server;

	@Override
	public void sendMessage(UpdateEvent event) {
		server.sendMessage(event, event.getData().getScope());
	}

	/**
	 * @return the eventManager
	 */
	public SocketEventServer getSocketEventServer() {
		return server;
	}

	/**
	 * @param eventManager the eventManager to set
	 */
	public void setSocketEventServer(SocketEventServer eventManager) {
		this.server = eventManager;
	}
	
}
