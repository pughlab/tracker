package ca.uhnresearch.pughlab.tracker.scripting;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;

import ca.uhnresearch.pughlab.tracker.events.Event;

public class ScriptEventHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Checks reading and writing the handler root
	 */
	@Test
	public void testHandlerRoot() {
		
		ScriptEventHandler handler = new ScriptEventHandler();
		JSEventHandlerRoot handlerRoot = new JSEventHandlerRoot();
		
		handler.setHandlerRoot(handlerRoot);
		
		Assert.assertEquals(handlerRoot, handler.getHandlerRoot());
	}

	/**
	 * Checks reading and writing the script manager
	 */
	@Test
	public void testScriptManager() {
		
		ScriptEventHandler handler = new ScriptEventHandler();
		ScriptManager manager = createMock(ScriptManager.class);
		replay(manager);
		
		handler.setScriptManager(manager);
		
		Assert.assertEquals(manager, handler.getScriptManager());

		verify();
	}

	/**
	 * Checks sending a message without a handler root
	 */
	@Test
	public void testSendMessageWithoutHandlerRoot() {
		
		ScriptEventHandler handler = new ScriptEventHandler();
		
		Event event = new Event(Event.EVENT_WELCOME, "DEMO");
		
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Missing handler root"));

		handler.sendMessage(event);
	}

	/**
	 * Checks sending a message without a handler root
	 */
	@Test
	public void testSendMessage() {
		
		ScriptEventHandler handler = new ScriptEventHandler();
		
		ScriptManager manager = createMock(ScriptManager.class);
		replay(manager);		
		handler.setScriptManager(manager);
		
		JSEventHandlerRoot handlerRoot = new JSEventHandlerRoot();		
		handler.setHandlerRoot(handlerRoot);
		
		Event event = new Event(Event.EVENT_WELCOME, "DEMO");
		
		JSEventHandler eventFunction = createMock(JSEventHandler.class);
		eventFunction.run(eq(event));
		expectLastCall().once();
		replay(eventFunction);
		
		handlerRoot.get("DEMO").on(Event.EVENT_WELCOME, eventFunction);

		handler.sendMessage(event);
		
		verify();
	}
}
