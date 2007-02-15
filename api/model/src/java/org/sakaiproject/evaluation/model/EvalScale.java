package org.sakaiproject.evaluation.model;

// Generated Feb 15, 2007 3:42:04 PM by Hibernate Tools 3.2.0.beta6a

import java.util.Date;

/**
 * EvalScale generated by hbm2java
 */
public class EvalScale implements java.io.Serializable {

	// Fields    

	private Long id;

	private Date lastModified;

	private String owner;

	private String title;

	private String sharing;

	private Boolean expert;

	private String expertDescription;

	private String ideal;

	private String[] options;

	private Boolean locked;

	// Constructors

	/** default constructor */
	public EvalScale() {
	}

	/** minimal constructor */
	public EvalScale(Date lastModified, String owner, String title, String sharing, Boolean expert) {
		this.lastModified = lastModified;
		this.owner = owner;
		this.title = title;
		this.sharing = sharing;
		this.expert = expert;
	}

	/** full constructor */
	public EvalScale(Date lastModified, String owner, String title, String sharing, Boolean expert,
			String expertDescription, String ideal, String[] options, Boolean locked) {
		this.lastModified = lastModified;
		this.owner = owner;
		this.title = title;
		this.sharing = sharing;
		this.expert = expert;
		this.expertDescription = expertDescription;
		this.ideal = ideal;
		this.options = options;
		this.locked = locked;
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

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSharing() {
		return this.sharing;
	}

	public void setSharing(String sharing) {
		this.sharing = sharing;
	}

	public Boolean getExpert() {
		return this.expert;
	}

	public void setExpert(Boolean expert) {
		this.expert = expert;
	}

	public String getExpertDescription() {
		return this.expertDescription;
	}

	public void setExpertDescription(String expertDescription) {
		this.expertDescription = expertDescription;
	}

	public String getIdeal() {
		return this.ideal;
	}

	public void setIdeal(String ideal) {
		this.ideal = ideal;
	}

	public String[] getOptions() {
		return this.options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public Boolean getLocked() {
		return this.locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

}
