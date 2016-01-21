package ca.uhnresearch.pughlab.tracker.plugins;

import org.junit.Test;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.events.Event;

public class StateLabelPluginTest {

	private StudyRepository repository = new MockStudyRepository();

	@Test
	public void test() {
		StateLabelPlugin plugin = new StateLabelPlugin();
		plugin.setRepository(repository);
		
		Event testEvent = new Event();
		testEvent.setType(Event.EVENT_STUDY_CHANGE);
		testEvent.setScope("DEMO");
		
		plugin.sendMessage(testEvent);
	}
}
