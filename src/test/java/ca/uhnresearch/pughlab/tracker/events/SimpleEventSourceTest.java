package ca.uhnresearch.pughlab.tracker.events;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Test;
import org.junit.Assert;

public class SimpleEventSourceTest {

	/**
	 * Checks that a single event is passed properly.
	 */
	@Test
	public void testDoEvent() {
		EventSource source = new SimpleEventSource();
		
		Capture<Event> capturedEvent = newCapture(CaptureType.FIRST);
		EventHandler handler = createMock(EventHandler.class);
		handler.sendMessage(capture(capturedEvent));
		expectLastCall().once();
		replay(handler);
		
		List<EventHandler> handlers = new ArrayList<EventHandler>();
		handlers.add(handler);
		
		source.setHandlers(handlers);
		
		Event event = new Event(Event.EVENT_JOIN, "TEST");
		source.doEvent(event);

		Assert.assertEquals(event, capturedEvent.getValue());
	}
	
	private class MockEventHandler implements EventHandler {
		private List<Event> events = new ArrayList<Event>();
		
		private EventSource source;
		
		public MockEventHandler(EventSource source) {
			this.source = source;
		}
		
		public List<Event> getEvents() {
			return events;
		}

		@Override
		public void sendMessage(Event event) {
			if (event.getType().equals(Event.EVENT_JOIN)) {
				Event next = new Event(Event.EVENT_STATE, "TEST");
				source.doEvent(next);
			}
			
			// Add at the end, so that we can detect nesting by the order
			// in the list.
			events.add(event);
		}
	}

	/** 
	 * Checks that an event that generates a new event will trigger two
	 * calls rather than just one, in series.
	 */
	@Test
	public void testDoNestedEvents() {
		EventSource source = new SimpleEventSource();

		MockEventHandler handler = new MockEventHandler(source);
		
		List<EventHandler> handlers = new ArrayList<EventHandler>();
		handlers.add(handler);
		source.setHandlers(handlers);

		Event event = new Event(Event.EVENT_JOIN, "TEST");
		source.doEvent(event);
		
		Assert.assertEquals(2, handler.getEvents().size());
		Assert.assertEquals(Event.EVENT_JOIN, handler.getEvents().get(0).getType());
		Assert.assertEquals(Event.EVENT_STATE, handler.getEvents().get(1).getType());
	}
}
