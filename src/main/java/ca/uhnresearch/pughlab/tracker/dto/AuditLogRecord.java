package ca.uhnresearch.pughlab.tracker.dto;

import java.sql.Timestamp;

import javax.annotation.Generated;

/**
 * AuditLog is a Querydsl bean type
 */
@Generated("com.mysema.query.codegen.BeanSerializer")
public class AuditLogRecord {
	
    private Integer id;

    private Integer studyId;

    private Integer caseId;

    private String attribute;

    private Timestamp eventTime;

    private String eventUser;

    private String eventType;

    private String eventArgs;

	/**
	 * @return the id
	 */
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
	 * @return the studyId
	 */
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
	 * @return the caseId
	 */
	public Integer getCaseId() {
		return caseId;
	}

	/**
	 * @param caseId the caseId to set
	 */
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}

	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the eventTime
	 */
	public Timestamp getEventTime() {
		return eventTime;
	}

	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	/**
	 * @return the eventUser
	 */
	public String getEventUser() {
		return eventUser;
	}

	/**
	 * @param eventUser the eventUser to set
	 */
	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the eventArgs
	 */
	public String getEventArgs() {
		return eventArgs;
	}

	/**
	 * @param eventArgs the eventArgs to set
	 */
	public void setEventArgs(String eventArgs) {
		this.eventArgs = eventArgs;
	}
}
