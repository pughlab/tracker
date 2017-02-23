package ca.uhnresearch.pughlab.tracker.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public class StateLabelPlugin extends AbstractRepositoryPlugin implements EventHandler {
	
	/** 
	 * The main event handler detects a change to the study and 
	 * makes sure that all cases are correctly labelled, generating
	 * state change events when required. 
	 */
	@Override
	public void sendMessage(Event event) {
		
		// No study, no states
		final Study study = getRepository().getStudy(event.getScope());
		if (study == null) {
			return;
		}
		
		// Handle a study update
		if (event.getType().equals(Event.EVENT_STUDY_CHANGE)) {
			applyLabels(study);
			
		// Handle a field update
		} else if (event.getType().equals(Event.EVENT_SET_FIELD)) {
			final ObjectNode parameters = event.getData().getParameters();
			applyCaseLabelRules(study, parameters);
		}
	}
	
	private class StateRule {
		private String state;
		private String attribute;
		private String value;
		
		private StateRule(String state, String attribute, String value) {
			this.state = state;
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	private JsonNode getStateRuleData(Study study) {
		return study.getOptions().get("stateRules");
	}
	
	private Set<String> getAttributeNames(Study study) {
		final JsonNode stateRules = getStateRuleData(study);
		
		final Set<String> attributeNames = new HashSet<String>();
		if (stateRules != null && stateRules.isArray()) {
			final int size = stateRules.size();
			for(int i = 0; i < size; i++) {
				final JsonNode rule = stateRules.get(i);
				attributeNames.add(rule.get("attribute").asText());
			}
		}

		return attributeNames;
	}
	
	private List<StateRule> getStateRules(Study study) {
		final JsonNode stateRules = getStateRuleData(study);
		
		final List<StateRule> rules = new ArrayList<StateRule>();
		if (stateRules != null && stateRules.isArray()) {
			final int size = stateRules.size();
			for(int i = 0; i < size; i++) {
				JsonNode rule = stateRules.get(i);
				rules.add(new StateRule(rule.get("state").asText(), rule.get("attribute").asText(), rule.get("value").asText()));
			}
		}

		return rules;
	}
	
	private List<Attributes> getStudyFilteredAttributes(Study study, Set<String> attributeNames) {
		final List<Attributes> attributes = getRepository().getStudyAttributes(study);
		
		final List<Attributes> filtered = new ArrayList<Attributes>();
		for(Attributes a : attributes) {
			if (attributeNames.contains(a.getName())) {
				filtered.add(a);
			}
		}

		return filtered;
	}
	
	private boolean matchesNode(JsonNode value, String criterion) {
		if (value instanceof TextNode) {
			return ((TextNode) value).asText().equals(criterion);
		} else {
			return value.toString().equals(criterion);
		}
	}
	
	private void applyLabelsToCases(Study study, List<ObjectNode> cases, List<StateRule> rules) {
		
		final String userName = "system";

		// Now we can apply the rules and change the states of any that need to
		// change. Note that the rule value might need to be decoded depending on the
		// attribute and its type. 
		
		for(ObjectNode c : cases) {
			String state = null;
			for(StateRule rule : rules) {
				final JsonNode value = c.get(rule.attribute);
				if (value == null) {
					// Do nothing, state can't be affected by a null/empty value
				} else if (value.isObject() && value.has("$notAvailable") && "N/A".equals(rule.value)) {
					state = rule.state;
				} else if (matchesNode(value, rule.value)) {
					state = rule.state;
				}
			}
			
			final JsonNode stateField =  c.get("$state");
			String oldState = (stateField != null && c.get("$state").isTextual()) ? c.get("$state").asText() : null;
			if (oldState == null && state == null) continue;
			
			final JsonNode identifierValue = c.get("id");
			if (identifierValue == null || ! identifierValue.isInt()) continue;
			
			if (oldState == null || ! oldState.equals(state)) {
				Cases selectedCase = getRepository().getStudyCase(study, identifierValue.asInt());
				getRepository().setStudyCaseState(study, selectedCase, userName, state);
			}
		}

	}

	private void applyLabels(Study study) {

		final Set<String> attributeNames = getAttributeNames(study);
		final List<StateRule> rules = getStateRules(study);
		
		final StudyCaseQuery query = getRepository().newStudyCaseQuery(study);
		final List<Attributes> filtered = getStudyFilteredAttributes(study, attributeNames);
		final List<ObjectNode> cases = getRepository().getCaseData(query, filtered);
		
		applyLabelsToCases(study, cases, rules);
	}
	
	private void applyCaseLabelRules(Study study, ObjectNode parameters) {
		
		final Set<String> attributeNames = getAttributeNames(study);
		final List<StateRule> rules = getStateRules(study);

		// The rules are as follows: if we've changed anything in the rule
		// attributes, we should reapply the rules to the case, and then if 
		// the new state differs from the old one, generate the state change.
		//
		// Optimization: skip if the attribute isn't relevant
		
		final String attribute = parameters.get("field").asText();
		if (! attributeNames.contains(attribute)) return;
		
		// So now we are in a position to apply the rules. Start assuming 
		// nothing about the state.
		
		StudyCaseQuery query = getRepository().newStudyCaseQuery(study);
		query = getRepository().addStudyCaseSelector(query, parameters.get("case_id").asInt());
		final List<Attributes> filtered = getStudyFilteredAttributes(study, attributeNames);
		final List<ObjectNode> cases = getRepository().getCaseData(query, filtered);
		
		applyLabelsToCases(study, cases, rules);
	}
}
