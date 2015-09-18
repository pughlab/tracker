package ca.uhnresearch.pughlab.tracker.dto;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.Matchers;

public class RolePermissionTest {

	@Test
	public void testConstructor() {
		RolePermission test = new RolePermission();
		Assert.assertNull(test.getId());
	}

	@Test
	public void testGetSetId() {
		RolePermission test = new RolePermission();
		test.setId(123);
		Assert.assertThat(test.getId(), Matchers.equalTo(123));
	}

	@Test
	public void testGetSetRoleId() {
		RolePermission test = new RolePermission();
		test.setRoleId(456);
		Assert.assertThat(test.getRoleId(), Matchers.equalTo(456));
	}

	@Test
	public void testGetSetUsername() {
		RolePermission test = new RolePermission();
		test.setPermission("admin");
		Assert.assertThat(test.getPermission(), Matchers.equalTo("admin"));
	}

}
