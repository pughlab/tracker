package ca.uhnresearch.pughlab.tracker.events;

/**
 * All classes capable of handling events should implement this class. 
 * 
 * @author stuartw
 */
public interface EventHandler {
	
	/**
	 * Handles an event for a given scope, which is typically a study name.
	 * @param event
	 * @param scope
	 */
	void sendMessage(Event event);
}
