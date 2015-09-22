package ca.uhnresearch.pughlab.tracker.dto;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.Matchers;

public class CasesTest {

	@Test
	public void testConstructor() {
		Cases test = new Cases();
		Assert.assertNull(test.getId());
	}

	@Test
	public void testGetSetId() {
		Cases test = new Cases();
		test.setId(123);
		Assert.assertThat(test.getId(), Matchers.equalTo(123));
	}

	@Test
	public void testGetSetStudyId() {
		Cases test = new Cases();
		test.setStudyId(66);
		Assert.assertThat(test.getStudyId(), Matchers.equalTo(66));
	}

	@Test
	public void testGetSetOrder() {
		Cases test = new Cases();
		test.setOrder(456);
		Assert.assertThat(test.getOrder(), Matchers.equalTo(456));
	}

	@Test
	public void testGetSetState() {
		Cases test = new Cases();
		test.setState("completed");
		Assert.assertThat(test.getState(), Matchers.equalTo("completed"));
	}
}
