package ca.uhnresearch.pughlab.tracker.security;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class LdapProfileTest {

	@Test
	public void testConstructor() {
		
		LdapProfile profile = new LdapProfile("stuart");
		Assert.assertEquals("stuart", profile.getName());
	}

	@Test
	public void testToString() {
		
		LdapProfile profile = new LdapProfile("stuart");
		Assert.assertEquals("stuart", profile.toString());
	}

	@Test
	public void testGetSetName() {
		
		LdapProfile profile = new LdapProfile("stuart");
		profile.setName("morag");
		Assert.assertEquals("morag", profile.getName());
	}

	@Test
	public void testGetSetDisplayName() {
		
		LdapProfile profile = new LdapProfile("stuart");
		profile.setDisplayName("Morag \"Tabs\" McKillop");
		Assert.assertEquals("Morag \"Tabs\" McKillop", profile.getDisplayName());
	}

	@Test
	public void testGetSetEmail() {
		
		LdapProfile profile = new LdapProfile("stuart");
		profile.setEmail("morag@example.com");
		Assert.assertEquals("morag@example.com", profile.getEmail());
	}
}
