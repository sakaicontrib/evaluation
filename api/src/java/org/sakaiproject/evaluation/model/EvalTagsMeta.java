/**
 * $Id$
 * $URL$
 * EvalTagsMeta.java - evaluation - Apr 2, 2008 9:21:35 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.model;

import java.util.Date;


/**
 * Stores meta data related to tags (including autoUseTags)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalTagsMeta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date lastModified;
    private String owner;
    private String tag;
    private String title;
    private String description;

    public EvalTagsMeta() {}

    /**
     * MINIMAL constructor
     * 
     * @param tag
     * @param owner internal userId of the creator of this tag (the person who can change the meta data)
     * @param title
     */
    public EvalTagsMeta(String tag, String owner, String title) {
        if (this.lastModified == null) { this.lastModified = new Date(); }
        this.owner = owner;
        this.tag = tag;
        this.title = title;
    }

    /**
     * FULL constructor
     * 
     * @param tag
     * @param owner internal userId of the creator of this tag (the person who can change the meta data)
     * @param title
     * @param description
     */
    public EvalTagsMeta(String tag, String owner, String title, String description) {
        if (this.lastModified == null) { this.lastModified = new Date(); }
        this.owner = owner;
        this.tag = tag;
        this.title = title;
        this.description = description;
    }

    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
