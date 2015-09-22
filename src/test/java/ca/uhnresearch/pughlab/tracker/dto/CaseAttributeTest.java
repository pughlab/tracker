package ca.uhnresearch.pughlab.tracker.dto;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;

public class CaseAttributeTest {

	@Test
	public void testConstructor() {
		CaseAttribute test = new CaseAttribute();
		Assert.assertNull(test.getId());
	}

	@Test
	public void testGetSetAttributeId() {
		CaseAttribute test = new CaseAttribute();
		test.setAttributeId(123);
		Assert.assertThat(test.getAttributeId(), Matchers.equalTo(123));
	}

	@Test
	public void testGetSetCaseId() {
		CaseAttribute test = new CaseAttribute();
		test.setCaseId(456);
		Assert.assertThat(test.getCaseId(), Matchers.equalTo(456));
	}

	@Test
	public void testGetSetNotAvailable() {
		CaseAttribute test = new CaseAttribute();
		test.setNotAvailable(true);
		Assert.assertThat(test.getNotAvailable(), Matchers.equalTo(true));
	}

	@Test
	public void testGetSetNotes() {
		CaseAttribute test = new CaseAttribute();
		test.setNotes("Whoo, a note");
		Assert.assertThat(test.getNotes(), Matchers.equalTo("Whoo, a note"));
	}

	@Test
	public void testGetSetValue() {
		CaseAttribute test = new CaseAttribute();
		Date date = new Date();
		test.setValue(date);
		Assert.assertThat(test.getValue(), IsInstanceOf.instanceOf(Date.class));
		Assert.assertThat((Date) test.getValue(), Matchers.equalTo(date));
	}
}
