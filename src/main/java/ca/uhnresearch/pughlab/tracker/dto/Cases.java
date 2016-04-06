package ca.uhnresearch.pughlab.tracker.dto;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cases is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Cases {

    private Integer id;

    private String guid;

    private String state;

    private Integer studyId;

    private Integer order;

	@JsonProperty
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	@JsonProperty
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

	@JsonProperty
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

	@JsonProperty
    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

	/**
	 * @return the order
	 */
	@JsonProperty
	public Integer getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}
}

