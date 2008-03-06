
package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * EvalAssignHierarchy
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAssignHierarchy implements java.io.Serializable {

   // Fields

   protected Long id;

   protected Date lastModified;

   protected String owner;

   protected String nodeId;

   protected Boolean instructorApproval;

   protected Boolean instructorsViewResults;

   protected Boolean studentsViewResults;

   protected EvalEvaluation evaluation;

   // Constructors

   /** default constructor */
   public EvalAssignHierarchy() {
   }

   /** 
    * BELOW min constructor<br/>
    * Must use a default setting method to set the Booleans
    * setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah)
    */
   public EvalAssignHierarchy(String owner, String nodeId, EvalEvaluation evaluation) {
      if (this.lastModified == null) { this.lastModified = new Date(); }
      this.owner = owner;
      this.nodeId = nodeId;
      this.evaluation = evaluation;
   }

   /** full constructor */
   public EvalAssignHierarchy(String owner, String nodeId, Boolean instructorApproval,
         Boolean instructorsViewResults, Boolean studentsViewResults,
         EvalEvaluation evaluation) {
      if (this.lastModified == null) { this.lastModified = new Date(); }
      this.owner = owner;
      this.nodeId = nodeId;
      this.instructorApproval = instructorApproval;
      this.instructorsViewResults = instructorsViewResults;
      this.studentsViewResults = studentsViewResults;
      this.evaluation = evaluation;
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

}
