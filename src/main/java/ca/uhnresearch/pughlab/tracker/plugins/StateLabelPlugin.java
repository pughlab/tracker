package ca.uhnresearch.pughlab.tracker.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public class StateLabelPlugin implements EventHandler {
	
	private StudyRepository repository;

	@Required
    public void setRepository(StudyRepository repository) {
        this.repository = repository;
    }
	
	public StudyRepository getRepository() {
		return repository;
	}

	/** 
	 * The main event handler detects a change to the study and 
	 * makes sure that all cases are correctly labelled, generating
	 * state change events when required. 
	 */
	@Override
	public void sendMessage(Event event) {
		if (! event.getType().equals(Event.EVENT_STUDY_CHANGE)) {
			return;
		}
		
		Study study = getRepository().getStudy(event.getScope());
		if (study == null) {
			return;
		}
		
		applyLabels(study);
	}
	
	private class StateRule {
		private String state;
		private String attribute;
		private JsonNode value;
		
		private StateRule(String state, String attribute, JsonNode value) {
			this.state = state;
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	private void applyLabels(Study study) {
		JsonNode stateRules = study.getOptions().get("stateRules");
		
		String userName = "system";
		
		Set<String> attributeNames = new HashSet<String>();
		List<StateRule> rules = new ArrayList<StateRule>();
		if (stateRules.isArray()) {
			int size = stateRules.size();
			for(int i = 0; i < size; i++) {
				JsonNode rule = stateRules.get(i);
				rules.add(new StateRule(rule.get("state").asText(), rule.get("attribute").asText(), rule.get("value")));
				attributeNames.add(rule.get("attribute").asText());
			}
		}
		
		StudyCaseQuery query = getRepository().newStudyCaseQuery(study);
		List<Attributes> attributes = getRepository().getStudyAttributes(study);
		List<Attributes> filtered = new ArrayList<Attributes>();
		Map<String, Attributes> attributeMap = new HashMap<String, Attributes>();
		for(Attributes a : attributes) {
			if (attributeNames.contains(a.getName())) {
				filtered.add(a);
				attributeMap.put(a.getName(), a);
			}
		}
		List<ObjectNode> cases = getRepository().getCaseData(query, filtered);
		
		// Now we can apply the rules and change the states of any that need to
		// change.
		
		for(ObjectNode c : cases) {
			String state = null;
			for(StateRule rule : rules) {
				JsonNode value = c.get(rule.attribute);
				if (value.equals(rule.value)) {
					state = rule.state;
				}
			}
			
			String oldState = c.get("$state").isTextual() ? c.get("$state").asText() : null;
			if (oldState == null && state == null) continue;
			if (oldState == null || ! oldState.equals(state)) {
				Cases selectedCase = getRepository().getStudyCase(study, c.get("id").asInt());
				getRepository().setStudyCaseState(study, selectedCase, userName, state);
			}
		}
	}
}
