package ca.uhnresearch.pughlab.tracker.scripting;

import java.util.HashMap;
import java.util.Map;

public class JSEventHandlerRoot {
	
	private Map<String, JSEventHandlerList> handlers = new HashMap<String, JSEventHandlerList>();
	
	public JSEventHandlerList get(String name) {
		if (! handlers.containsKey(name)) {
			handlers.put(name, new JSEventHandlerList());
		}
		return handlers.get(name);
	}
}
