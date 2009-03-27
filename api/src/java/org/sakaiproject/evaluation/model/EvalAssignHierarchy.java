
package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * This is the assignment of a node to an evaluation and is how we know that the groups related to
 * the node should be taking an eval, it has to be expanded into the groups and they must be
 * assigned or it will have no real affect, this is mostly just allowing us to track the original
 * assignment actions<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAssignHierarchy implements java.io.Serializable {

    // Fields

    protected Long id;

    protected Date lastModified;

    protected String owner;

    /**
     * This will be set to the node id of the node which is assigned
     */
    protected String nodeId;

    protected Boolean instructorApproval;

    protected Boolean instructorsViewResults;

    protected Boolean studentsViewResults;

    protected transient EvalEvaluation evaluation;

    // NON-PERSISTENT

    /**
     * This is should be set but is not persistent,
     * however it will be used by the services to set the evaluation if it is not already set
     */
    private Long evalId;
    public void setEvaluationId(Long evalId) {
        // cannot set the evalId if it is already persisted
        if (this.evaluation == null) {
            this.evalId = evalId;
        } else {
            this.evalId = evaluation.getId();
        }
    }
    public Long getEvaluationId() {
        Long id = evalId;
        if (this.evaluation != null) {
            id = evaluation.getId();
            this.evalId = id;
        }
        return id;
    }


    // Constructors

    /** default constructor */
    public EvalAssignHierarchy() {
    }

    /**
     * BELOW min constructor<br/>
     * Must use a default setting method to set the Booleans setDefaults(EvalEvaluation eval,
     * EvalAssignHierarchy eah)
     */
    public EvalAssignHierarchy(String owner, String nodeId, EvalEvaluation evaluation) {
        this(owner, nodeId, evaluation, null, null, null, null, null);
    }

    /** 
     * full constructor
     */
    public EvalAssignHierarchy(String owner, String nodeId, EvalEvaluation evaluation,
            Boolean instructorApproval, Boolean instructorsViewResults, Boolean studentsViewResults, 
            String instructorSelection, String assistantSelection) {
        this.lastModified = new Date();
        this.owner = owner;
        this.nodeId = nodeId;
        this.instructorApproval = instructorApproval;
        this.instructorsViewResults = instructorsViewResults;
        this.studentsViewResults = studentsViewResults;
        setEvaluation(evaluation);
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getInstructorApproval() {
        return instructorApproval;
    }

    public void setInstructorApproval(Boolean instructorApproval) {
        this.instructorApproval = instructorApproval;
    }

    public Boolean getInstructorsViewResults() {
        return instructorsViewResults;
    }

    public void setInstructorsViewResults(Boolean instructorsViewResults) {
        this.instructorsViewResults = instructorsViewResults;
    }

    public Boolean getStudentsViewResults() {
        return studentsViewResults;
    }

    public void setStudentsViewResults(Boolean studentsViewResults) {
        this.studentsViewResults = studentsViewResults;
    }

    public EvalEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(EvalEvaluation evaluation) {
        this.evaluation = evaluation;
        if (evaluation != null) {
            this.evalId = evaluation.getId();
        }
    }

}
