package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ViewAttributes extends Attributes {

    private JsonNode viewOptions;

    public JsonNode getViewOptions() {
        return viewOptions;
    }

    public void setViewOptions(JsonNode options) {
        this.viewOptions = options;
    }
}
