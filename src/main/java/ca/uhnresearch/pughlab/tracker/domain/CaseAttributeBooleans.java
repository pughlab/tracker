package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * CaseAttributeBooleans is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class CaseAttributeBooleans {

    private String attribute;

    private Integer caseId;

    private Integer id;

    private Boolean notAvailable;

    private String notes;

    private Boolean value;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(Boolean notAvailable) {
        this.notAvailable = notAvailable;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

}

