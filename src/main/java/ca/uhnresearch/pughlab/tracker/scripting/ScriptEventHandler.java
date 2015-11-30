package ca.uhnresearch.pughlab.tracker.scripting;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public class ScriptEventHandler implements EventHandler {
	
	private final Logger logger = LoggerFactory.getLogger(ScriptEventHandler.class);

	private ScriptManager scriptManager;
	
	private JSEventHandlerRoot handlerRoot;

	@Override
	public void sendMessage(Event event) {
		
		logger.info("Got an event: {}, {}", event, event.getScope());
		
		JSEventHandlerList handlerList = getHandlerRoot().get(event.getScope());
		List<JSEventHandler> handlers = handlerList.getEventHandlers(event.getType());
		
		logger.debug("Found handlers: {}, {}", event.getType(), handlers);
		
		for(JSEventHandler handler : handlers) {
			handler.run(event);
		}
	}

	/**
	 * @return the manager
	 */
	public ScriptManager getScriptManager() {
		return scriptManager;
	}

	/**
	 * @param manager the manager to set
	 */
	public void setScriptManager(ScriptManager manager) {
		this.scriptManager = manager;
	}

	/**
	 * @return the handlerRoot
	 */
	public JSEventHandlerRoot getHandlerRoot() {
		if (handlerRoot == null) {
			throw new RuntimeException("Missing handler root");
		}
		return handlerRoot;
	}

	/**
	 * @param handlerRoot the handlerRoot to set
	 */
	public void setHandlerRoot(JSEventHandlerRoot handlerRoot) {
		this.handlerRoot = handlerRoot;
	}
}
