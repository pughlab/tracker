package ca.uhnresearch.pughlab.tracker.scripting;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSEventHandlerList {
	
	private final Logger logger = LoggerFactory.getLogger(JSEventHandlerList.class);

	private Map<String, Object> handlers = new HashMap<String, Object>();
	
	public void on(String name, Object function) {
		logger.info("Registering event handler: {}, {}", name, function);
		handlers.put(name, function);
	}
}
