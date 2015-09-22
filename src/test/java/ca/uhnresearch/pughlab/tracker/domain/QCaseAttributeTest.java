package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Date;

import org.junit.Assert;
import org.junit.Test;

import com.mysema.query.types.Order;

public class QCaseAttributeTest {

	@Test
	public void testBooleans() {
		QCaseAttributeBooleans booleans = QCaseAttributeBooleans.caseAttributes;
		Assert.assertEquals("value", booleans.getValuePath(Boolean.class).getMetadata().getName());
	}

	@Test
	public void testBooleansAsc() {
		QCaseAttributeBooleans booleans = QCaseAttributeBooleans.caseAttributes;
		Assert.assertEquals(Order.ASC, booleans.getValueOrderSpecifier(true).getOrder());
	}

	@Test
	public void testBooleansDesc() {
		QCaseAttributeBooleans booleans = QCaseAttributeBooleans.caseAttributes;
		Assert.assertEquals(Order.DESC, booleans.getValueOrderSpecifier(false).getOrder());
	}

	@Test
	public void testStrings() {
		QCaseAttributeStrings strings = QCaseAttributeStrings.caseAttributes;
		Assert.assertEquals("value", strings.getValuePath(String.class).getMetadata().getName());
	}

	@Test
	public void testStringsAsc() {
		QCaseAttributeStrings strings = QCaseAttributeStrings.caseAttributes;
		Assert.assertEquals(Order.ASC, strings.getValueOrderSpecifier(true).getOrder());
	}

	@Test
	public void testStringsDesc() {
		QCaseAttributeStrings strings = QCaseAttributeStrings.caseAttributes;
		Assert.assertEquals(Order.DESC, strings.getValueOrderSpecifier(false).getOrder());
	}

	@Test
	public void testDates() {
		QCaseAttributeDates dates = QCaseAttributeDates.caseAttributes;
		Assert.assertEquals("value", dates.getValuePath(Date.class).getMetadata().getName());
	}

	@Test
	public void testDatesAsc() {
		QCaseAttributeDates dates = QCaseAttributeDates.caseAttributes;
		Assert.assertEquals(Order.ASC, dates.getValueOrderSpecifier(true).getOrder());
	}

	@Test
	public void testDatesDesc() {
		QCaseAttributeDates dates = QCaseAttributeDates.caseAttributes;
		Assert.assertEquals(Order.DESC, dates.getValueOrderSpecifier(false).getOrder());
	}

	@Test
	public void testNumbers() {
		QCaseAttributeNumbers numbers = QCaseAttributeNumbers.caseAttributes;
		Assert.assertEquals("value", numbers.getValuePath(Double.class).getMetadata().getName());
	}

	@Test
	public void testNumbersAsc() {
		QCaseAttributeNumbers numbers = QCaseAttributeNumbers.caseAttributes;
		Assert.assertEquals(Order.ASC, numbers.getValueOrderSpecifier(true).getOrder());
	}

	@Test
	public void testNumbersDesc() {
		QCaseAttributeNumbers numbers = QCaseAttributeNumbers.caseAttributes;
		Assert.assertEquals(Order.DESC, numbers.getValueOrderSpecifier(false).getOrder());
	}

}
