
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

    public static final String SELECTION_ALL = "all";
    public static final String SELECTION_ONE = "one";
    public static final String SELECTION_MULTIPLE = "multiple";

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

    protected EvalEvaluation evaluation;

    /**
     * The key which determines which way to render instructors items from this group assignment,
     * use the SELECTION_* constants like {@link #SELECTION_ONE}, default is null (equivalent to all)
     */
    protected String instructorSelection;
    /**
     * The key which determines which way to render assistants items from this group assignment, use
     * the SELECTION_* constants like {@link #SELECTION_ONE}, default is null (equivalent to all)
     */
    protected String assistantSelection;

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
        this.evaluation = evaluation;
        this.instructorSelection = instructorSelection;
        this.assistantSelection = assistantSelection;
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
    }

    /**
     * @return the instructor selection constant OR null if not set (null is equivalent to {@link #SELECTION_ALL})
     */
    public String getInstructorSelection() {
        return instructorSelection;
    }

    public void setInstructorSelection(String instructorSelection) {
        this.instructorSelection = instructorSelection;
    }

    /**
     * @return the assistant selection constant OR null if not set (null is equivalent to {@link #SELECTION_ALL})
     */
    public String getAssistantSelection() {
        return assistantSelection;
    }

    public void setAssistantSelection(String assistantSelection) {
        this.assistantSelection = assistantSelection;
    }

}
