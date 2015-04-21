package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * Attributes is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Attributes {
	
	public static final String ATTRIBUTE_TYPE_STRING = "strings";

	public static final String ATTRIBUTE_TYPE_BOOLEAN = "booleans";

	public static final String ATTRIBUTE_TYPE_DATE = "dates";

	private String description;

    private Integer id;

    private String label;

    private String name;

    private String options;

    private Integer rank;

    private Integer studyId;

    private String type;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}

