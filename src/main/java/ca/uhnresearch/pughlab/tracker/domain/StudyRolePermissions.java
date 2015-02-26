package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * StudyRolePermissions is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class StudyRolePermissions {

    private Integer id;

    private String permission;

    private String resource;

    private String resourceType;

    private Integer studyRoleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getStudyRoleId() {
        return studyRoleId;
    }

    public void setStudyRoleId(Integer studyRoleId) {
        this.studyRoleId = studyRoleId;
    }

}

