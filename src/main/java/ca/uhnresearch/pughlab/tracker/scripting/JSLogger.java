package ca.uhnresearch.pughlab.tracker.scripting;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSLogger {
	private final Logger logger = LoggerFactory.getLogger(JSLogger.class);

	public void log(Object... strings) {
		List<Object> list = Arrays.asList(strings);
		StringBuilder message = new StringBuilder();
		for(Object element : list) {
			if (element == null) {
				message.append("null");
			} else {
				message.append(element.toString());
			}
			message.append(" ");
		}
		int length = message.length();
		if (length > 0) {
			message.deleteCharAt(length - 1);
		}
		
		logger.info(message.toString());
	}
}
