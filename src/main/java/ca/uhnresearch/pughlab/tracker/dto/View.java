package ca.uhnresearch.pughlab.tracker.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class View {

	private static ObjectMapper mapper = new ObjectMapper();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String description;

    private Integer id;

    private String name;

    private Integer studyId;

    private JsonNode options;

	@JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	@JsonProperty
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	@JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Don't JSON the study identifier. We'll always have a study too
    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

	@JsonProperty
    public JsonNode getOptions() {
        return options;
    }

    public void setOptions(JsonNode options) {
        this.options = options;
    }

}

