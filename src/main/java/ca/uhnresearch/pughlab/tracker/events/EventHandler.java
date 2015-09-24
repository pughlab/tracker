package ca.uhnresearch.pughlab.tracker.events;

public interface EventHandler {
	void sendMessage(Event event, String scope);
}
