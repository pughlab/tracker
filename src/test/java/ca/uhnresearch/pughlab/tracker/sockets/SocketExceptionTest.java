package ca.uhnresearch.pughlab.tracker.sockets;

import org.junit.Assert;

import org.junit.Test;

public class SocketExceptionTest {

	@Test
	public void testNewException() {
		Exception e = new SocketException("Bad Socket");
		Assert.assertEquals("Bad Socket", e.getMessage());
	}

}
