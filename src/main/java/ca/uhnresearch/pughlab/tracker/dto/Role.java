package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

    private Integer id;
    
    private Integer studyId;

    private String studyName;

	private String name;
	
	private List<String> users = new ArrayList<String>();
	
	private List<String> permissions = new ArrayList<String>();

	public Role() { }

	public Role(String name) {
		setName(name);
	}

	/**
	 * @return the id
	 */
	@JsonProperty
    public Integer getId() {
        return id;
    }

	/**
	 * @param id the id to set
	 */
    public void setId(Integer id) {
        this.id = id;
    }

	/**
	 * @return the name
	 */
	@JsonProperty
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the studyId
	 */
	@JsonProperty
	public Integer getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the studyName
	 */
	@JsonProperty
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * @return the users
	 */
	@JsonProperty
	public List<String> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(List<String> users) {
		this.users = users;
	}

	/**
	 * @return the permissions
	 */
	@JsonProperty
	public List<String> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
