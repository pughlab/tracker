package ca.uhnresearch.pughlab.tracker.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSEventHandlerList {
	
	private final Logger logger = LoggerFactory.getLogger(JSEventHandlerList.class);

	private Map<String, List<JSEventHandler>> handlers = new HashMap<String, List<JSEventHandler>>();
	
	public void on(String name, JSEventHandler function) {
		logger.info("Registering event handler: {}, {}", name, function);
		getEventHandlers(name).add(function);
	}
	
	public List<JSEventHandler> getEventHandlers(String name) {
		if (! handlers.containsKey(name)) {
			handlers.put(name, new ArrayList<JSEventHandler>());
		}
		return handlers.get(name);
	}
}
