package ca.uhnresearch.pughlab.tracker.security;

import org.junit.Assert;
import org.junit.Test;

public class AbsolutifyingOidcClientTest {

	@Test
	public void testConstructor() {
		AbsolutifyingOidcClient context = new AbsolutifyingOidcClient();
		Assert.assertNotNull(context);
	}

	@Test
	public void testGetSetClientID() {
		AbsolutifyingOidcClient context = new AbsolutifyingOidcClient();
		context.setClientID("flooby1234");
	}

	@Test
	public void testGetSetSecret() {
		AbsolutifyingOidcClient context = new AbsolutifyingOidcClient();
		context.setSecret("dhfdsuyfbnjkdsz.ndifszbfzs");
	}
}
