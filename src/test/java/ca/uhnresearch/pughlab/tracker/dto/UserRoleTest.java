package ca.uhnresearch.pughlab.tracker.dto;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.Matchers;

public class UserRoleTest {

	@Test
	public void testConstructor() {
		UserRole test = new UserRole();
		Assert.assertNull(test.getId());
	}

	@Test
	public void testGetSetId() {
		UserRole test = new UserRole();
		test.setId(123);
		Assert.assertThat(test.getId(), Matchers.equalTo(123));
	}

	@Test
	public void testGetSetRoleId() {
		UserRole test = new UserRole();
		test.setRoleId(456);
		Assert.assertThat(test.getRoleId(), Matchers.equalTo(456));
	}

	@Test
	public void testGetSetUsername() {
		UserRole test = new UserRole();
		test.setUsername("stuart");
		Assert.assertThat(test.getUsername(), Matchers.equalTo("stuart"));
	}
}
