package ca.uhnresearch.pughlab.tracker.scheduling;

import org.junit.Test;

public class JSLoggerTest {

	@Test
	public void testJSLogger() {
		JSLogger logger = new JSLogger();
		logger.log("This is a simple log");
	}

	@Test
	public void testJSLoggerMultipleObjects() {
		JSLogger logger = new JSLogger();
		logger.log("This is a complex log", 1, logger);
	}

	@Test
	public void testJSLoggerEmpty() {
		JSLogger logger = new JSLogger();
		logger.log();
	}
}
