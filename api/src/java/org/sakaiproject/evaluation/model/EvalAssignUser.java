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
     * STATUS: indicates the assignment is active and in use and should be synchronized if the related
     * assign groups/hierarchy nodes change, all assignments related to groups are created in this state, 
     * this is the default state
     */
    public static String STATUS_LINKED = "linked";
    /**
     * STATUS: indicates the assignment was changed and should no longer be synchronized,
     * all "adhoc" assignments should be created in this state
     */
    public static String STATUS_UNLINKED = "unlinked";
    /**
     * STATUS: indicates the assignment is removed and no longer will be used or considered,
     * no synchronization happens when this is the state 
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
     * The evaluation that this user is assigned to
     * (just access evaluation.id to avoid lazy loading the entire evaluation)
     */
    protected EvalEvaluation evaluation;
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
     * assignment participates, uses the constants {@link #TYPE_ASSISTANT}, {@link #TYPE_EVALUATEE}, {@link #TYPE_EVALUATOR}
     */
    protected String type;
    /**
     * the status of the assignment, active OR deleted for example, deleted assignments are ignored
     * for all processing and only kept for records, uses the constants {@link #STATUS_LINKED}, {@link #STATUS_REMOVED}, {@link #STATUS_UNLINKED}
     */
    protected String status;
    /**
     * This will be non-null if this assignment is linked to a group, it will be the id of the {@link EvalAssignGroup}
     */
    protected Long assignGroupId;
    /**
     * This will be non-null if this assignment is linked to a hierarchy node, it will be the id of the {@link EvalAssignHierarchy}
     */
    protected Long assignHierarchyId;

    // Constructors

    /** default constructor */
    public EvalAssignUser() {
    }

    /**
     * Minimal constructor, sets the type automatically to eval taker (student), 
     * all records are created with a default status of active,
     * makes the current user the owner
     * 
     * @param evaluation the evaluation to create the assignment for
     * @param userId the user which is being assigned, should be the internal id (not the username)
     * @param evalGroupId (OPTIONAL) the eval group this assignment is related to
     */
    public EvalAssignUser(EvalEvaluation evaluation, String userId, String evalGroupId) {
        this(evaluation, userId, evalGroupId, null, null, null);
    }

    /**
     * Full constructor
     * 
     * @param evaluation the evaluation to create the assignment for
     * @param userId the user which is being assigned, should be the internal id (not the username)
     * @param evalGroupId (OPTIONAL) the eval group this assignment is related to
     * @param owner (OPTIONAL) will be assigned to the current user if not set
     * @param type (OPTIONAL) use a constant like {@link #TYPE_EVALUATEE} or {@value #TYPE_EVALUATOR}
     * @param status (OPTIONAL) use a constant {@link #STATUS_LINKED} or {@link #STATUS_REMOVED}
     */
    public EvalAssignUser(EvalEvaluation evaluation, String userId, String evalGroupId, String owner, String type, String status) {
        super();
        if (userId == null || "".equals(userId)
                || evalGroupId == null || "".equals(evalGroupId)
                || evaluation == null) {
            throw new IllegalArgumentException("userId, evalGroupId, and evaluation all must be set and not null");
        }
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.userId = userId;
        this.evalGroupId = evalGroupId;
        this.evaluation = evaluation;
        this.owner = owner;
        try {
            validateStatus(status);
        } catch (IllegalArgumentException e) {
            type = STATUS_LINKED;
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
        validateNotEmpty(owner, "owner");
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
        validateNotEmpty(userId, "userId");
        this.userId = userId;
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        validateNotEmpty(evalGroupId, "evalGroupId");
        this.evalGroupId = evalGroupId;
    }

    public EvalEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(EvalEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public Long getAssignGroupId() {
        return assignGroupId;
    }

    public void setAssignGroupId(Long assignGroupId) {
        this.assignGroupId = assignGroupId;
    }

    public Long getAssignHierarchyId() {
        return assignHierarchyId;
    }

    public void setAssignHierarchyId(Long assignHierarchyId) {
        this.assignHierarchyId = assignHierarchyId;
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

    // validation methods

    public static void validateNotEmpty(String str, String name) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException(name + " cannot be null or empty string");
        }
    }

    public static void validateStatus(String status) {
        validateNotEmpty(status, "status");
        if (! STATUS_LINKED.equals(status) && ! STATUS_UNLINKED.equals(status) && ! STATUS_REMOVED.equals(status) ) {
            throw new IllegalArgumentException("status must match one of the STATUS constants in EvalAssignUser");
        }
    }

    public static void validateType(String type) {
        validateNotEmpty(type, "type");
        if (! TYPE_ASSISTANT.equals(type) && ! TYPE_EVALUATEE.equals(type) && ! TYPE_EVALUATOR.equals(type) ) {
            throw new IllegalArgumentException("type must match one of the TYPE constants in EvalAssignUser");
        }
    }

}
