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

	private Date lastModified;
	
	/**
	 * The name of the lock in the EVAL_LOCK table set for this email
	 */
	private String lock;

	/**
	 * The body of the email
	 */
	private String message;
	
	/**
	 * The subject of the email
	 */
	private String subject;
	
	/**
	 * 
	 */
	private String toAddress;
	
	// Constructors

	/** default constructor */
	public EvalQueuedEmail() {
	}

	/** full constructor */
	public EvalQueuedEmail(String lock, String message, String subject, String toAddress) {
		this.lastModified = new Date();
		this.lock = lock;
		this.message = message;
		this.subject = subject;
		this.toAddress = toAddress;
	}
	
	// Property accessors
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getLock() {
		return this.lock;
	}

	public void setLock(String lock) {
		this.lock = lock;
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
	
	@Override
	public String toString() {
		return "eval: ["+this.toAddress+"] "+ " ("+this.id+") subject="+this.subject+",message=" + this.message;
	};
}
