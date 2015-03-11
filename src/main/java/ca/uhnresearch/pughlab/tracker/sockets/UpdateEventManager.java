package ca.uhnresearch.pughlab.tracker.sockets;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateEventManager {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void handleEvent(Subject subject, UpdateEvent event) {
        logger.info("Handling an event, maybe...");
	}
}
