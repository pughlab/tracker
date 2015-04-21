package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * CaseAttributeDates is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class CaseAttributeDates {

    private String attribute;

    private Integer caseId;

    private Integer id;

    private Boolean notAvailable;

    private String notes;

    private java.sql.Date value;

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

    public java.sql.Date getValue() {
        return value;
    }

    public void setValue(java.sql.Date value) {
        this.value = value;
    }

}

