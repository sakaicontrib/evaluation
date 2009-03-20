package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * Stores configuration settings for the overall eval system
 */
public class EvalConfig implements java.io.Serializable {

    // Fields    

    private Long id;

    private Date lastModified;

    private String name;

    private String value;

    // Constructors

    /** default constructor */
    public EvalConfig() {
    }

    /** full constructor */
    public EvalConfig(String name, String value) {
        this.lastModified = new Date();
        this.name = name;
        this.value = value;
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
