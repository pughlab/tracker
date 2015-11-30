package ca.uhnresearch.pughlab.tracker.events;

import java.util.List;

/**
 * The EventSource is a bit different to an event handler. It typically receives events
 * from a system and sends them to a set of handlers, but any subsequent events generated
 * are queued while one event is being processed, so that there is no need for the handlers
 * to worry about re-entrancy. Subsequent events are forwarded after the initial event
 * has been concluded. 
 */
public interface EventSource {
	
	public void setHandlers(List<EventHandler> handlers);
	
	public void doEvent(Event event);
}
