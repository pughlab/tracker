package ca.uhnresearch.pughlab.tracker.dto;

import javax.annotation.Generated;

/**
 * Cases is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Cases {

    private Integer id;

    private String state;

    private Integer studyId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

}

