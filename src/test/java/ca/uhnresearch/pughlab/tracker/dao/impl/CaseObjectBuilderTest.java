package ca.uhnresearch.pughlab.tracker.dao.impl;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.Tuple;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class CaseObjectBuilderTest {

	CaseObjectBuilder builder;
	
	private final NumberPath<Integer> idPath = new NumberPath<Integer>(Integer.class, "id");
	
	private final StringPath stringPath = new StringPath("attribute");

	private final BooleanPath notAvailablePath = new BooleanPath("notAvailable");

	private final StringPath notesPath = new StringPath("notes");

	QTuple stringTuple = new QTuple(idPath, stringPath, new StringPath("value"), notAvailablePath, notesPath);
	
	QTuple dateTuple = new QTuple(idPath, stringPath, new DatePath<Date>(Date.class, "value"), notAvailablePath, notesPath);

	QTuple booleanTuple = new QTuple(idPath, stringPath, new BooleanPath("value"), notAvailablePath, notesPath);

	@Before
	public void initialize() {
		List<CaseInfo> cases = new ArrayList<CaseInfo>();
		CaseInfo c = new CaseInfo(1, "test");
		cases.add(c);
		builder = new CaseObjectBuilder(cases);
	}
	
	@Test
	public void testGetObjects() {
		assertNotNull(builder.getCaseObjects());
	}

	@Test
	public void testAddBasicStringTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		values.add(stringTuple.newInstance(1, "test", "value", false, null));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals("value", result.get(0).get("test").asText());
	}

	@Test
	public void testAddBasicDateTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		Date newDate = new Date(Instant.now().toEpochMilli());
		values.add(dateTuple.newInstance(1, "test", newDate, false, null));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals(newDate.toString(), result.get(0).get("test").asText());
	}

	@Test
	public void testAddBasicBooleanTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(booleanTuple.newInstance(1, "test", false, false, null));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals("false", result.get(0).get("test").asText());
	}

	@Test
	public void testAddNotAvailableTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(booleanTuple.newInstance(1, "test", null, true, null));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertTrue(result.get(0).get("test").isObject());
		assertTrue(result.get(0).get("test").has("$notAvailable"));
		assertTrue(result.get(0).get("test").get("$notAvailable").isBoolean());
		assertEquals(true, result.get(0).get("test").get("$notAvailable").asBoolean());
	}

	@Test
	public void testAddNotAvailableNull() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(booleanTuple.newInstance(1, "test", false, null, null));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals("false", result.get(0).get("test").asText());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAddInvalidTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(stringTuple.newInstance(1, "test", new Object(), false, null));
		
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Invalid attribute type"));
		builder.addTupleAttributes(values);
	}

	@Test
	public void testAddInvalidNotes() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(stringTuple.newInstance(1, "test", "value", false, "{"));
		
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Invalid JSON"));
		builder.addTupleAttributes(values);
	}

	@Test
	public void testAddValidNotes() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(booleanTuple.newInstance(1, "test", false, null, "{\"note\": \"testNote\"}"));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals("false", result.get(0).get("test").asText());
		
		assertTrue(result.get(0).has("$notes"));
		assertTrue(result.get(0).get("$notes").isObject());
		assertTrue(result.get(0).get("$notes").has("test"));
		assertTrue(result.get(0).get("$notes").get("test").isObject());
		assertTrue(result.get(0).get("$notes").get("test").has("note"));
		assertTrue(result.get(0).get("$notes").get("test").get("note").isTextual());
		assertEquals("testNote", result.get(0).get("$notes").get("test").get("note").asText());
	}

	@Test
	public void testMergeValidNotes() {
		List<Tuple> values = new ArrayList<Tuple>();
		
		values.add(stringTuple.newInstance(1, "test1", "value", false, "{\"note\": \"other\"}"));
		values.add(booleanTuple.newInstance(1, "test2", false, null, "{\"note\": \"testNote\"}"));
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		
		assertTrue(result.get(0).has("$notes"));
		assertTrue(result.get(0).get("$notes").isObject());
		assertTrue(result.get(0).get("$notes").has("test1"));
		assertTrue(result.get(0).get("$notes").get("test1").isObject());
		assertTrue(result.get(0).get("$notes").get("test1").has("note"));
		assertTrue(result.get(0).get("$notes").get("test1").get("note").isTextual());
		assertEquals("other", result.get(0).get("$notes").get("test1").get("note").asText());
		assertTrue(result.get(0).get("$notes").has("test2"));
		assertTrue(result.get(0).get("$notes").get("test2").isObject());
		assertTrue(result.get(0).get("$notes").get("test2").has("note"));
		assertTrue(result.get(0).get("$notes").get("test2").get("note").isTextual());
		assertEquals("testNote", result.get(0).get("$notes").get("test2").get("note").asText());
	}

	@Test
	public void testAddFilteredTuples() {
		List<Tuple> values = new ArrayList<Tuple>();
		values.add(stringTuple.newInstance(1, "test1", "value1", false, null));
		values.add(stringTuple.newInstance(1, "test2", "value2", false, null));
		
		List<Attributes> attributes = new ArrayList<Attributes>();
		Attributes filter = new Attributes();
		filter.setName("test1");
		attributes.add(filter);
		builder.setAttributeFilter(attributes);
		
		builder.addTupleAttributes(values);
		
		List<ObjectNode> result = builder.getCaseObjects();
		assertEquals(1, result.size());
		
		assertTrue(result.get(0).has("id"));
		assertEquals(1, result.get(0).get("id").asInt());
		assertEquals("value1", result.get(0).get("test1").asText());
		assertTrue(! result.get(0).has("test2"));
	}
}
