package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * Studies is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Studies {

    private String description;

    private Integer id;

    private Integer identifierAttributeId;

    private String name;

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

    public Integer getIdentifierAttributeId() {
        return identifierAttributeId;
    }

    public void setIdentifierAttributeId(Integer identifierAttributeId) {
        this.identifierAttributeId = identifierAttributeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

