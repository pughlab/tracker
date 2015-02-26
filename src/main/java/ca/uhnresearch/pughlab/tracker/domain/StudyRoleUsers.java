package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * StudyRoleUsers is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class StudyRoleUsers {

    private Integer id;

    private Integer studyRoleId;

    private Integer userId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudyRoleId() {
        return studyRoleId;
    }

    public void setStudyRoleId(Integer studyRoleId) {
        this.studyRoleId = studyRoleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}

