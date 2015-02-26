package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * ViewAttributes is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class ViewAttributes {

    private Integer attributeId;

    private String options;

    private Integer rank;

    private Integer viewId;

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
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

    public Integer getViewId() {
        return viewId;
    }

    public void setViewId(Integer viewId) {
        this.viewId = viewId;
    }

}

