package ca.uhnresearch.pughlab.tracker.audit;

import static org.easymock.EasyMock.*;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.RedactedJsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuditLogEventHandlerTest {

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	private AuditLogEventHandler handler;
	
	@Before
	public void setUp() {
		handler = new AuditLogEventHandler();
	}
	
	@Test
	public void testMinimal() {

		Event event = new Event(Event.EVENT_SET_FIELD);
		event.getData().setScope("TEST");
		event.getData().setUser("morag");
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode parameters = factory.objectNode();
		event.getData().setParameters(parameters);
		
		Capture<AuditLogRecord> capturedArgument = newCapture(CaptureType.FIRST);

		AuditLogRepository repository = createMock(AuditLogRepository.class);
		repository.writeAuditLogRecord(capture(capturedArgument));
		expectLastCall().once();
		replay(repository);
		
		handler.setRepository(repository);
		handler.sendMessage(event, event.getData().getScope());
		
		Assert.assertEquals("morag", capturedArgument.getValue().getEventUser());
		Assert.assertEquals("{}", capturedArgument.getValue().getEventArgs());
	}

	@Test
	public void testUnRedacted() {

		Event event = new Event(Event.EVENT_SET_FIELD);
		event.getData().setScope("TEST");
		event.getData().setUser("morag");
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode parameters = factory.objectNode();
		parameters.put("field", "attribute");
		parameters.put("case_id", 123);
		parameters.put("study_id", 456);
		parameters.replace("old", jsonNodeFactory.textNode("secret1"));
		parameters.replace("new", jsonNodeFactory.textNode("secret2"));
		event.getData().setParameters(parameters);
		
		Capture<AuditLogRecord> capturedArgument = newCapture(CaptureType.FIRST);

		AuditLogRepository repository = createMock(AuditLogRepository.class);
		repository.writeAuditLogRecord(capture(capturedArgument));
		expectLastCall().once();
		replay(repository);
		
		handler.setRepository(repository);
		handler.sendMessage(event, event.getData().getScope());
		
		Assert.assertEquals("morag", capturedArgument.getValue().getEventUser());
		Assert.assertEquals("{\"field\":\"attribute\",\"case_id\":123,\"study_id\":456,\"old\":\"secret1\",\"new\":\"secret2\"}", capturedArgument.getValue().getEventArgs());
	}

	@Test
	public void testRemoveRedaction() {

		Event event = new Event(Event.EVENT_SET_FIELD);
		event.getData().setScope("TEST");
		event.getData().setUser("morag");
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode parameters = factory.objectNode();
		parameters.put("field", "attribute");
		parameters.put("case_id", 123);
		parameters.put("study_id", 456);
		parameters.replace("old", new RedactedJsonNode(jsonNodeFactory.textNode("secret1")));
		parameters.replace("new", new RedactedJsonNode(jsonNodeFactory.textNode("secret2")));
		event.getData().setParameters(parameters);
		
		Capture<AuditLogRecord> capturedArgument = newCapture(CaptureType.FIRST);

		AuditLogRepository repository = createMock(AuditLogRepository.class);
		repository.writeAuditLogRecord(capture(capturedArgument));
		expectLastCall().once();
		replay(repository);
		
		handler.setRepository(repository);
		handler.sendMessage(event, event.getData().getScope());
		
		Assert.assertEquals("morag", capturedArgument.getValue().getEventUser());
		Assert.assertEquals("{\"field\":\"attribute\",\"case_id\":123,\"study_id\":456,\"old\":\"secret1\",\"new\":\"secret2\"}", capturedArgument.getValue().getEventArgs());
	}
}
