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

import java.util.Comparator;
import java.util.Date;

/**
 * This represents an assigned user in an evaluation,
 * It is effectively a read-only record once it has been persisted so the only way
 * to change it is to remove it and create a new one, the user/evaluation/group
 * cannot be changed after it has been saved <br/>
 * Only the status and type can be modified once this object has been persisted,
 * attempts to set the other fields will have no effect on the object <br/>
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
     * (just access evaluation.id to avoid lazy loading the entire evaluation),
     * this is properly set by the service methods so do not try to set this when you
     * create this object, it is best to leave it null or whatever it already is <br/>
     * cannot be changed once it is set
     */
    protected EvalEvaluation evaluation;
    /**
     * The internal user id of the user belonging to this assignment record,
     * cannot be changed once it is set
     */
    protected String userId;
    /**
     * the eval group id which this assignment relates to,
     * cannot be changed once it is set
     */
    protected String evalGroupId;

    /**
     * This is non-persistent, it will track when the status changes, true means changed
     */
    public boolean typeChanged = false;
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
     * This will be non-null if this assignment is linked to a group, it will be the id of the {@link EvalAssignGroup},
     * cannot be changed once it is set
     */
    protected Long assignGroupId;
    /**
     * This stores a specific ordering for this assignment when listing it,
     * note that by default the order is 0 which means list in id order,
     * use the {@link AssignComparatorByOrder} to order things
     */
    protected int listOrder;

    // Constructors

    /** default constructor */
    public EvalAssignUser() {
    }

    /**
     * Minimal constructor, sets the type automatically to eval taker (student), 
     * all records are created with a default status of active,
     * makes the current user the owner
     * 
     * @param userId the user which is being assigned, should be the internal id (not the username)
     * @param evalGroupId (OPTIONAL) the eval group this assignment is related to
     */
    public EvalAssignUser(String userId, String evalGroupId) {
        this(userId, evalGroupId, null, null, null);
    }

    /**
     * Full constructor
     * 
     * @param userId the user which is being assigned, should be the internal id (not the username)
     * @param evalGroupId (OPTIONAL) the eval group this assignment is related to
     * @param owner (OPTIONAL) will be assigned to the current user if not set
     * @param type (OPTIONAL) use a constant like {@link #TYPE_EVALUATEE} or {@value #TYPE_EVALUATOR}
     * @param status (OPTIONAL) use a constant {@link #STATUS_LINKED} or {@link #STATUS_REMOVED}
     */
    public EvalAssignUser(String userId, String evalGroupId, String owner, String type, String status) {
        super();
        this.lastModified = new Date();
        if (userId == null || "".equals(userId) ) {
            throw new IllegalArgumentException("userId must be set and not null");
        }
        this.userId = userId;
        this.evalGroupId = evalGroupId;
        this.owner = owner;
        try {
            validateStatus(status);
        } catch (IllegalArgumentException e) {
            status = STATUS_LINKED;
        }
        this.status = status;
        try {
            validateType(type);
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
        if (this.userId == null) {
            validateNotEmpty(userId, "userId");
            this.userId = userId;
        }
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        if (this.evalGroupId == null) {
            this.evalGroupId = evalGroupId;
        }
    }

    public EvalEvaluation getEvaluation() {
        return evaluation;
    }

    /**
     * @return the evaluation id OR null if no eval is associated yet,
     * this will avoid the lazy loading of the entire eval
     */
    public Long getEvaluationId() {
        return evaluation == null ? null : evaluation.getId();
    }

    public void setEvaluation(EvalEvaluation evaluation) {
        if (this.evaluation == null) {
            this.evaluation = evaluation;
        }
    }

    public Long getAssignGroupId() {
        return assignGroupId;
    }

    public void setAssignGroupId(Long assignGroupId) {
        if (this.assignGroupId == null) {
            this.assignGroupId = assignGroupId;
        }
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
        this.typeChanged = true;
    }

    public int getListOrder() {
        return listOrder;
    }

    public void setListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    // validation methods

    public static void validateNotEmpty(String str, String name) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException(name + " cannot be null or empty string");
        }
    }

    /**
     * Validate the status constant
     * @param status
     * @throws IllegalArgumentException if the status is null or not one of the valid constants
     */
    public static void validateStatus(String status) {
        validateNotEmpty(status, "status");
        if (! STATUS_LINKED.equals(status) && ! STATUS_UNLINKED.equals(status) && ! STATUS_REMOVED.equals(status) ) {
            throw new IllegalArgumentException("status must match one of the STATUS constants in EvalAssignUser");
        }
    }

    /**
     * Validate the type constant
     * @param type
     * @throws IllegalArgumentException if the type is null or not one of the valid constants
     */
    public static void validateType(String type) {
        validateNotEmpty(type, "type");
        if (! TYPE_ASSISTANT.equals(type) && ! TYPE_EVALUATEE.equals(type) && ! TYPE_EVALUATOR.equals(type) ) {
            throw new IllegalArgumentException("type must match one of the TYPE constants in EvalAssignUser");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (id == null) {
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            result = prime * result + ((evalGroupId == null) ? 0 : evalGroupId.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
        } else {
            result = prime * result + id.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvalAssignUser other = (EvalAssignUser) obj;
        if (id == null) {
            if ( userId.equals(other.userId)
                    && type.equals(other.type) 
                    && (evalGroupId == null ? other.evalGroupId == null : evalGroupId.equals(other.evalGroupId)) ) {
                return false;
            }
        } else {
            if (!id.equals(other.id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "id="+id+":eval="+getEvaluationId()+":user="+userId+":group="+evalGroupId+":type="+type+":status="+status;
    }

    /**
     * This comparator will put the user assignments in a list in the correct list order
     * using the list order operator, 0 (default) puts the item at the end of the list
     */
    public static class AssignComparatorByOrder implements Comparator<EvalAssignUser> {
        public static final long serialVersionUID = 31L;
        public int compare(EvalAssignUser item0, EvalAssignUser item1) {
            int compare = 0;
            if (item0.listOrder == item1.listOrder
                    || (item0.listOrder <= 0 && item1.listOrder <= 0) ) {
                // use id instead
                if (item0.id != null && item1.id != null) {
                    item0.id.compareTo(item1.id);
                } else {
                    // use userId
                    item0.userId.compareTo(item1.userId);
                }
            } else if (item0.listOrder > 0 && item1.listOrder > 0) {
                // ordering set for both
                compare = item0.listOrder - item1.listOrder;
            } else if (item0.listOrder <= 0) {
                // ordering set for one only
                compare = -1;
            } else if (item1.listOrder <= 0) {
                // ordering set for one only
                compare = 1;
            }
            return compare;
        }
    }

}
