package ca.uhnresearch.pughlab.tracker.dto;

public class CaseAttribute {

    private Integer attributeId;

    private Integer caseId;

    private Integer id;

    private Object value;

    private Boolean notAvailable = false;

    private String notes;

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
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

}
