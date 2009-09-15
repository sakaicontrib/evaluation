package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * This defines a complete outgoing email notification pending delivery
 * 
 * @author rwellis
 *
 */
public class EvalQueuedEmail {
	private Long id;

	private Date creationDate;
	
	/**
	 * The name of the lock in the EVAL_LOCK table set for this email
	 */
	private String emailLock;

	/**
	 * The body of the email
	 */
	private String message;
	
	/**
	 * The subject of the email
	 */
	private String subject;
	
	/**
	 * The identifier of the email template used to generate the message
	 */
	private Long emailTemplateId;
	
	/**
	 * Task Status stream Id
	 */
	private Long TSStreamId;
	
	/**
	 * Flag set to true when email has been sent
	 */
	private Boolean emailSent;
	
	/**
	 * 
	 */
	private String toAddress;
	
	// Constructors

	/** default constructor */
	public EvalQueuedEmail() {
	}

	/** full constructor */
	public EvalQueuedEmail(String emailLock, String message, String subject, String toAddress, Long emailTemplateId, Long TSStreamId) {
		this.creationDate = new Date();
		this.emailLock = emailLock;
		this.message = message;
		this.subject = subject;
		this.toAddress = toAddress;
		this.emailTemplateId = emailTemplateId;
		this.TSStreamId = TSStreamId;
		this.emailSent = new Boolean(false);
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
	
	public String getEmailLock() {
		return this.emailLock;
	}

	public void setEmailLock(String emailLock) {
		this.emailLock = emailLock;
	}
	
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getToAddress() {
		return this.toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	
	public Long getEmailTemplateId() {
		return emailTemplateId;
	}
	
	public void setEmailTemplateId(Long emailTemplateId) {
		this.emailTemplateId = emailTemplateId;
	}
	
	public Long getTSStreamId() {
		return TSStreamId;
	}
	
	public void setTSStreamId(Long TSStreamId) {
		this.TSStreamId = TSStreamId;
	}
	
	public Boolean getEmailSent() {
		return this.emailSent;
	}
	
	public void setEmailSent(Boolean emailSent) {
		this.emailSent = emailSent;
	}
	
	@Override
	public String toString() {
		return "eval: ["+this.toAddress+"] "+ " ("+this.id+") subject="+this.subject+",message=" + this.message;
	};
}
