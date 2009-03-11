
package org.sakaiproject.evaluation.model;

// Generated Mar 20, 2007 10:08:13 AM by Hibernate Tools 3.2.0.beta6a

import java.util.Date;

/**
 * This is the assignment of a group to an evaluation and is how we know that the group should be
 * taking an eval<br/>
 * <b>NOTE:</b> the nodeId will be set to the id of the node which caused this group to be added if
 * it was added that way, it will be null if the group was assigned directly
 */
public class EvalAssignGroup extends EvalAssignHierarchy implements java.io.Serializable {

    // Fields

    private String eid;

    private String evalGroupId;

    private String evalGroupType;

    // Constructors

    /** default constructor */
    public EvalAssignGroup() {
    }

    /**
     * BELOW minimal constructor, need to run this through a default setter method (setDefaults) to
     * set the Booleans before saving, setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah)
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            EvalEvaluation evaluation) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.owner = owner;
        this.evalGroupId = evalGroupId;
        this.evalGroupType = evalGroupType;
        this.evaluation = evaluation;
    }

    /**
     * REQUIRED constructor
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            Boolean instructorApproval, Boolean instructorsViewResults,
            Boolean studentsViewResults, EvalEvaluation evaluation) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.owner = owner;
        this.evalGroupId = evalGroupId;
        this.evalGroupType = evalGroupType;
        this.instructorApproval = instructorApproval;
        this.instructorsViewResults = instructorsViewResults;
        this.studentsViewResults = studentsViewResults;
        this.evaluation = evaluation;
    }

    /**
     * full constructor
     */
    public EvalAssignGroup(String owner, String evalGroupId, String evalGroupType,
            Boolean instructorApproval, Boolean instructorsViewResults,
            Boolean studentsViewResults, EvalEvaluation evaluation, String nodeId,
            String instructorSelection, String assistantSelection) {
        if (this.lastModified == null) {
            lastModified = new Date();
        }
        this.owner = owner;
        this.evalGroupId = evalGroupId;
        this.evalGroupType = evalGroupType;
        this.instructorApproval = instructorApproval;
        this.instructorsViewResults = instructorsViewResults;
        this.studentsViewResults = studentsViewResults;
        this.evaluation = evaluation;
        this.nodeId = nodeId;
        this.instructorSelection = instructorSelection;
        this.assistantSelection = assistantSelection;
    }

    public String getEid() {
        return this.eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEvalGroupId() {
        return evalGroupId;
    }

    public void setEvalGroupId(String evalGroupId) {
        this.evalGroupId = evalGroupId;
    }

    public String getEvalGroupType() {
        return evalGroupType;
    }

    public void setEvalGroupType(String evalGroupType) {
        this.evalGroupType = evalGroupType;
    }

}
