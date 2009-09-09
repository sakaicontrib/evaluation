package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * This defines an assign group for which single email notification is due
 * 
 * @author rwellis
 *
 */
public class EvalQueuedGroup {
	private Long id;

	private Date creationDate;
	
	/**
	 * EvalConstants.SINGLE_EMAIL_AVAILABLE or EvalConstants.SINGLE_EMAIL_REMINDER
	 */
	private String emailType;
	
	/**
	 * The assigned group identity
	 */
	private String groupId;
	
	/**
	 * The identity of the evaluation assigned to the group
	 */
	private Long evaluationId;
	
	/**
	 * Task Status stream Id
	 */
	private Long TSStreamId;
	
	/**
	 * Flag set to true when group member email has been built
	 */
	private Boolean emailBuilt;
	
	/**
	 * The name of the lock in the EVAL_LOCK table set for this group
	 */
	private String groupLock;
	
	// Constructors
	
	/** default constructor */
	public EvalQueuedGroup() {
	}
	
	/** full constructor */
	public  EvalQueuedGroup(String groupId, String emailType, Long evaluationId, String groupLock, Long TSStreamId) {
		this.creationDate = new Date();
		this.evaluationId = evaluationId;
		this.groupId = groupId;
		this.emailType = emailType;
		this.groupLock = groupLock;
		this.TSStreamId = TSStreamId;
		this.emailBuilt = new Boolean(false);
	}
	
	// Property accessors
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getGroupId() {
		return this.groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public Long getEvaluationId() {
		return this.evaluationId;
	}
	
	public void setEvaluationId(Long evaluationId) {
		this.evaluationId = evaluationId;
	}
	
	public String getEmailType() {
		return this.emailType;
	}
	
	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}
	
	public String getGroupLock() {
		return this.groupLock;
	}

	public void setGroupLock(String groupLock) {
		this.groupLock = groupLock;
	}
	
	public Long getTSStreamId() {
		return TSStreamId;
	}
	
	public void setTSStreamId(Long TSStreamId) {
		this.TSStreamId = TSStreamId;
	}
	
	public Boolean getEmailBuilt() {
		return this.emailBuilt;
	}
	
	public void setEmailBuilt(Boolean emailBuilt) {
		this.emailBuilt = emailBuilt;
	}
}
