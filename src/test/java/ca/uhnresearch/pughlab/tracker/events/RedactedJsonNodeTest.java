package ca.uhnresearch.pughlab.tracker.events;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests RedactedPOJONode
 * 
 * @author stuartw
 */
public class RedactedJsonNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	/**
	 * Check that an object containing a RedactedPOJONode appears as a string 
	 * REDACTED. 
	 * @throws JsonProcessingException
	 */
	@Test
	public void testRedaction() throws JsonProcessingException {
		
		ObjectNode parent = jsonNodeFactory.objectNode();
		parent.set("field1", jsonNodeFactory.textNode("value1"));
		parent.set("field2", new RedactedJsonNode(jsonNodeFactory.textNode("secret")));
		
		String output = mapper.writeValueAsString(parent);
		Assert.assertEquals("{\"field1\":\"value1\",\"field2\":\"REDACTED\"}", output);
	}

	/**
	 * Checks the original value can still be retrieved
	 * @throws JsonProcessingException
	 */
	@Test
	public void testAccess() throws JsonProcessingException {
		
		ObjectNode parent = jsonNodeFactory.objectNode();
		parent.set("field1", jsonNodeFactory.textNode("value1"));
		parent.set("field2", new RedactedJsonNode(jsonNodeFactory.textNode("secret")));
		
		JsonNode output = parent.get("field2");
		Assert.assertTrue(output instanceof RedactedJsonNode);
		RedactedJsonNode outputNode = (RedactedJsonNode) output;
		Assert.assertEquals("secret", outputNode.getValue().asText());
	}

	/**
	 * Checks the original value can still be retrieved
	 * @throws JsonProcessingException
	 */
	@Test
	public void testToString() throws JsonProcessingException {
		
		ObjectNode parent = jsonNodeFactory.objectNode();
		parent.set("field1", jsonNodeFactory.textNode("value1"));
		parent.set("field2", new RedactedJsonNode(jsonNodeFactory.textNode("secret")));
		
		String output = parent.toString();
		Assert.assertEquals("{\"field1\":\"value1\",\"field2\":\"REDACTED\"}", output);
	}
}
