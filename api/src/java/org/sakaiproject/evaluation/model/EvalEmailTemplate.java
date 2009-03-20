package org.sakaiproject.evaluation.model;

import java.util.Date;

import org.sakaiproject.evaluation.constant.EvalConstants;

/**
 * This defines the email templates used in the system,
 * it includes the default templates and the custom ones
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalEmailTemplate implements java.io.Serializable {

    // Fields    

    private Long id;

    private Date lastModified;

    private String owner;

    /**
     * This is a constant which indicates the type of the template:
     * EvalConstants#EMAIL_TEMPLATE_*: Example: {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}
     */
    private String type;

    private String subject;

    private String message;

    /**
     * If this is set then it indicates that this is the default email template of the type in {@link #type},
     * null indicates this is a custom template, default templates cannot be removed
     */
    private String defaultType;

    // Constructors

    /** default constructor */
    public EvalEmailTemplate() {
    }

    /** 
     * minimal constructor 
     */
    public EvalEmailTemplate(String owner, String type, String subject, String message) {
        this(owner, type, subject, message, null);
    }

    /** 
     * full constructor 
     */
    public EvalEmailTemplate(String owner, String type, String subject, String message, String defaultType) {
        this.lastModified = new Date();
        this.owner = owner;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.defaultType = defaultType;
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

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDefaultType() {
        return this.defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
