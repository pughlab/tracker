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
	
	protected final JsonNode _value;
	
	private static final String REDACTED_VALUE = "REDACTED";
	
	private static final JsonNodeFactory factory = JsonNodeFactory.instance;

	public RedactedJsonNode(JsonNode v) {
		_value = v;
	}

	@Override
	public JsonToken asToken() {
		return JsonToken.VALUE_STRING;
	}
	
	public JsonNode getValue() { 
		return _value;
	}

	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeString(REDACTED_VALUE);
	}

	@Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.STRING;
    }

	@Override
	public String asText() {
		return REDACTED_VALUE;
	}
	
	@Override
    public String toString() {
        int len = REDACTED_VALUE.length();
        len = len + 2 + (len >> 4);
        StringBuilder sb = new StringBuilder(len);
        sb.append('"');
        CharTypes.appendQuoted(sb, REDACTED_VALUE);
        sb.append('"');
        return sb.toString();
    }

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
		ObjectNode redacted = factory.objectNode();
		Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			redacted.replace(field.getKey(), new RedactedJsonNode(field.getValue()));
		}
		return redacted;
	}
	
	/**
	 * Helper method to transform an ObjectNode, recursively, into
	 * a tagged form which can be written. 
	 */
	public static ObjectNode redactedToTagged(final ObjectNode data) {
		ObjectNode tagged = factory.objectNode();
		Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value instanceof RedactedJsonNode) {
				ObjectNode tag = factory.objectNode();
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
	 */
	public static ObjectNode taggedToRedacted(final ObjectNode data) {
		ObjectNode redacted = factory.objectNode();
		Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value instanceof ObjectNode) {
				ObjectNode obj = (ObjectNode) value;
				if (obj.has("$r")) {
					JsonNode redactable = obj.get("$r");
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
	 */
	public static ObjectNode redactedToClear(final ObjectNode data) {
		ObjectNode tagged = factory.objectNode();
		Iterator<Entry<String, JsonNode>> fields = data.fields();
		while(fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
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
