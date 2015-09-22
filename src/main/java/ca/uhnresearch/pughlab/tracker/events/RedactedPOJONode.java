package ca.uhnresearch.pughlab.tracker.events;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Behaves mostly like a POJONode, and allows values to be embedded within a
 * renderable container, but designed not to render to JSON. All values get
 * rendered to a string REDACTED. Handy for use server-side with confidence
 * that nothing gets rendered too badly outside. 
 * 
 * @author stuartw
 */

public class RedactedPOJONode extends ValueNode {
	
	protected final Object _value;
	
	private final String REDACTED_VALUE = "REDACTED";

	public RedactedPOJONode(Object v) {
		_value = v;
	}

	@Override
	public JsonToken asToken() {
		return JsonToken.VALUE_STRING;
	}
	
	public Object getPojo() { 
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
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o instanceof RedactedPOJONode) {
			return ((RedactedPOJONode) o)._value.equals(_value);
		}
		return false;
	}
}
