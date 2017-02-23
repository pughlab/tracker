package ca.uhnresearch.pughlab.tracker.events;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Behaves mostly like a POJONode, and allows values to be embedded within a
 * renderable container, but designed not to render to JSON. All values get
 * rendered to a string REDACTED. Handy for use server-side with confidence
 * that nothing gets rendered too badly outside. 
 * 
 * @author stuartw
 */

public class RedactedJsonNode extends ValueNode {
	
	/**
	 * The underlying value node.
	 */
	protected final JsonNode _value;
	
	private static final String REDACTED_VALUE = "REDACTED";
	
	private static final JsonNodeFactory factory = JsonNodeFactory.instance;

	/**
	 * Constructs a redacted node from a value node.
	 * @param v the value node
	 */
	public RedactedJsonNode(JsonNode v) {
		_value = v;
	}

	/**
	 * Returns the node type as a JsonToken
	 * @return  the JsonToken for a value string
	 */
	@Override
	public JsonToken asToken() {
		return JsonToken.VALUE_STRING;
	}
	
	/**
	 * Returns the underlying redactable value.
	 * @return the value
	 */
	public JsonNode getValue() { 
		return _value;
	}

	/**
	 * Serializes this node to the redacted value, as if it is a string
	 * @param jgen
	 * @param provider
	 */
	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeString(REDACTED_VALUE);
	}

	/**
	 * Returns the node type. A redacted node always looks like a string.
	 */
	@Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.STRING;
    }

	/**
	 * Returns the node as text, always the redacted value.
	 */
	@Override
	public String asText() {
		return REDACTED_VALUE;
	}
	
	/**
	 * Maps the node to a string, always the redacted value.
	 */
	@Override
    public String toString() {
        int len = REDACTED_VALUE.length();
        len = len + 2 + (len >> 4);
        final StringBuilder sb = new StringBuilder(len);
        sb.append('"');
        CharTypes.appendQuoted(sb, REDACTED_VALUE);
        sb.append('"');
        return sb.toString();
    }

	/**
	 * Two redacted nodes are equal if and only if their underlying values are equal. 
	 * @param o the other value
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o instanceof RedactedJsonNode) {
			return ((RedactedJsonNode) o)._value.equals(_value);
		}
		return false;
	}
	
	/**
	 * Helper method to redact an entire object node in a single pass.
	 * @param data the node to redact
	 * @return the redacted node
	 */
	public static ObjectNode redactObjectNode(final ObjectNode data) {
		final ObjectNode redacted = factory.objectNode();
		final Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			final Entry<String, JsonNode> field = fields.next();
			redacted.replace(field.getKey(), new RedactedJsonNode(field.getValue()));
		}
		return redacted;
	}
	
	/**
	 * Helper method to transform an ObjectNode, recursively, into
	 * a tagged form which can be written. 
	 * @param data the input object
	 * @return the tagged node
	 */
	public static ObjectNode redactedToTagged(final ObjectNode data) {
		final ObjectNode tagged = factory.objectNode();
		final Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			final Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value instanceof RedactedJsonNode) {
				final ObjectNode tag = factory.objectNode();
				tag.replace("$r", ((RedactedJsonNode) value).getValue());
				value = tag;
			} else if (value instanceof ObjectNode) {
				value = redactedToTagged((ObjectNode) value);
			}
			tagged.replace(field.getKey(), value);
		}
		return tagged;
	}
	
	/**
	 * Helper method to transform an tagged ObjectNode, recursively, into
	 * a redacted form. 
	 * @param data the input tagged node
	 * @return the redacted object
	 */
	public static ObjectNode taggedToRedacted(final ObjectNode data) {
		final ObjectNode redacted = factory.objectNode();
		final Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			final Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value instanceof ObjectNode) {
				final ObjectNode obj = (ObjectNode) value;
				if (obj.has("$r")) {
					final JsonNode redactable = obj.get("$r");
					value = new RedactedJsonNode(redactable);
				} else {
					value = taggedToRedacted(obj);
				}
			}
			redacted.replace(field.getKey(), value);
		}
		return redacted;
	}
	
	/**
	 * Helper method to transform an ObjectNode, recursively, into
	 * a clear form. 
	 * @param data the input object
	 * @return a clear (unredacted) object
	 */
	public static ObjectNode redactedToClear(final ObjectNode data) {
		final ObjectNode tagged = factory.objectNode();
		final Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			final Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value instanceof RedactedJsonNode) {
				value = ((RedactedJsonNode) value).getValue();
			} else if (value instanceof ObjectNode) {
				value = redactedToClear((ObjectNode) value);
			}
			tagged.replace(field.getKey(), value);
		}
		return tagged;
	}
}
