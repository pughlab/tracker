package ca.uhnresearch.pughlab.tracker.scripting;

import org.junit.Test;

import ca.uhnresearch.pughlab.tracker.scripting.JSLogger;

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

	@Test
	public void testJSLoggerNull() {
		JSLogger logger = new JSLogger();
		logger.log((String)null);
	}
}
