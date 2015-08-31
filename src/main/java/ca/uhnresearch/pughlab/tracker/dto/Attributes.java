package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Attributes {
	
	public static final String ATTRIBUTE_TYPE_STRING = "string";

	public static final String ATTRIBUTE_TYPE_BOOLEAN = "boolean";

	public static final String ATTRIBUTE_TYPE_DATE = "date";

	public static final String ATTRIBUTE_TYPE_OPTION = "option";

	public static final String ATTRIBUTE_TYPE_NUMBER = "number";
	
	private String description;

    private Integer id;

    private String label;

    private String name;

    private JsonNode options;

    private Integer rank;

    private Integer studyId;

    private String type;

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
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

	@JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@JsonProperty
    public JsonNode getOptions() {
        return options;
    }

    public void setOptions(JsonNode options) {
        this.options = options;
    }

	@JsonProperty
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

	@JsonProperty
    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

	@JsonProperty
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

