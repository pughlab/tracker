package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * Views is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Views {

    private String description;

    private Integer id;

    private String name;

    private Integer studyId;

    private String options;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

}

