/**
 * $Id$
 * $URL$
 * EvalAdhocGroup.java - evaluation - Mar 4, 2008 1:53:13 PM - azeckoski
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
 * This represents an assigned user in an evaluation
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAssignUser implements java.io.Serializable {

    /**
     * STATUS: indicates the assignment is active and in use, this is the default state
     */
    public static String STATUS_ACTIVE = "active";
    /**
     * STATUS: indicates the assignment is removed and no longer will be used or considered
     */
    public static String STATUS_REMOVED = "removed";

    /**
     * TYPE: This is the user being evaluated (the evaluatee),
     * typically the instructor of a course or the leader of a group
     */
    public static String TYPE_EVALUATEE = "evaluatee";
    /**
     * TYPE: This is the assistant for a group, normally a teaching assistant but they
     * could be a lab assistant or some other kind of assistant
     */
    public static String TYPE_ASSISTANT = "assistant";
    /**
     * TYPE: This is the evaluator of a group (the user filling out the evaluation),
     * often this is the student or main participant in a group,
     * this is the default type
     */
    public static String TYPE_EVALUATOR = "evaluator";

    // Fields

    /**
     * The unique auto-assigned identifier for this record
     */
    protected Long id;
    /**
     * The last time this record was updated
     */
    protected Date lastModified;
    /**
     * The external identifier for this user assignment
     */
    protected String eid;
    /**
     * The creator of this user assignment
     */
    protected String owner;
    /**
     * The internal user id of the user belonging to this assignment record
     */
    protected String userId;
    /**
     * the eval group id which this assignment relates to
     */
    protected String evalGroupId;
    /**
     * the type of the assignment, will be like instructor, TA, or student depending on how this
     * assignment participates
     */
    protected String type;
    /**
     * the status of the assignment, active OR deleted for example, deleted assignments are ignored
     * for all processing and only kept for records
     */
    protected String status;

    // Constructors

    /** default constructor */
    public EvalAssignUser() {
    }

    /**
     * Minimal constructor, sets the type automatically to eval taker (student), 
     * all records are created with a default status of active
     * 
     * @param owner the owner of this assignment
     * @param userId the internal user id (not username)
     * @param evalGroupId the group id of the associated group
     */
    public EvalAssignUser(String owner, String userId, String evalGroupId) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.owner = owner;
        this.userId = userId;
        this.evalGroupId = evalGroupId;
        this.status = "active"; // TODO
    }

    /**
     * Full constructor
     * @param owner
     * @param userId
     * @param evalGroupId
     * @param type
     * @param status use a constant {@link #STATUS_ACTIVE} or {@link #STATUS_REMOVED}
     */
    public EvalAssignUser(String owner, String userId, String evalGroupId, String type, String status) {
        super();
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.owner = owner;
        this.userId = userId;
        this.evalGroupId = evalGroupId;
        try {
            validateStatus(status);
        } catch (IllegalArgumentException e) {
            type = STATUS_ACTIVE;
        }
        this.status = status;
        try {
            validateType(status);
        } catch (IllegalArgumentException e) {
            type = TYPE_EVALUATOR;
        }
        this.type = type;
    }

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

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        this.evalGroupId = evalGroupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        validateStatus(status);
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        validateType(type);
        this.type = type;
    }


    public static void validateStatus(String status) {
        if (status == null || "".equals(status)) {
            throw new IllegalArgumentException("status cannot be null or empty string");
        }
        if (! STATUS_ACTIVE.equals(status) && ! STATUS_REMOVED.equals(status) ) {
            throw new IllegalArgumentException("status must match one of the STATUS constants in EvalAssignUser");
        }
    }

    public static void validateType(String type) {
        if (type == null || "".equals(type)) {
            throw new IllegalArgumentException("type cannot be null or empty string");
        }
        if (! TYPE_ASSISTANT.equals(type) && ! TYPE_EVALUATEE.equals(type) && ! TYPE_EVALUATOR.equals(type) ) {
            throw new IllegalArgumentException("type must match one of the TYPE constants in EvalAssignUser");
        }
    }

}
