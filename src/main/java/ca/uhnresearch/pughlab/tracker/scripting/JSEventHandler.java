package ca.uhnresearch.pughlab.tracker.scripting;

import ca.uhnresearch.pughlab.tracker.events.Event;

@FunctionalInterface
public interface JSEventHandler {
	public abstract void run(Event event);
}
