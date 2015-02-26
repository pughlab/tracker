package ca.uhnresearch.pughlab.tracker.domain;

import javax.annotation.Generated;

/**
 * Users is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class Users {

    private String apikey;

    private String email;

    private java.sql.Timestamp expires;

    private Boolean forcePasswordChange;

    private String hash;

    private Integer id;

    private Boolean locked;

    private String username;

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public java.sql.Timestamp getExpires() {
        return expires;
    }

    public void setExpires(java.sql.Timestamp expires) {
        this.expires = expires;
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

