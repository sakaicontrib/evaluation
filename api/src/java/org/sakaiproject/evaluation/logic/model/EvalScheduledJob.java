/**
 * $Id$
 * $URL$
 * EvalScheduledJob.java - evaluation - Mar 26, 2008 3:33:35 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.model;

import java.util.Date;

/**
 * A pea which represents a job (which will cause a spring bean method to be executed at a specific time and date),
 * this is a duplicate of the pea in org.sakaiproject.api.app.scheduler.DelayedInvocation.java
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalScheduledJob {

   public static final char SEPARATOR = '/';

   /**
    * The unique id for this job
    */
   public String uuid;
   /**
    * When should the job execute
    */
   public Date date;
   /**
    * The associated component for this job
    */
   public String componentId;
   /**
    * The context for this job
    */
   public String contextId;
   /**
    * The evaluation id related to this job
    */
   public Long evaluationId;
   /**
    * The job type for this job (from EvalConstants.JOB_TYPE_*
    */
   public String jobType;


   public EvalScheduledJob() { }

   /**
    * Basic constructor used when we know what the context (typically evalId + SEPARATOR + jobType) is
    */
   public EvalScheduledJob(String uuid, Date date, String componentId, String contextId) {
      this.uuid = uuid;
      this.date = date;
      this.componentId = componentId;
      setContextId(contextId);
   }

   /**
    * Constructor to use when we know what the evalId is
    */
   public EvalScheduledJob(String uuid, Date date, String componentId, Long evaluationId,
         String jobType) {
      super();
      this.uuid = uuid;
      this.date = date;
      this.componentId = componentId;
      setEvalAndType(evaluationId, jobType);
   }

   public String toString() {
      return("uuid: "+uuid+" date: "+date+" componentId: "+componentId+" contextId: "+contextId);
   }

   /**
    * Sets the contextId and decodes out the evalId and jobType
    * @param contextId
    */
   public void setContextId(String contextId) {
      this.contextId = contextId;
      EvalIdType eit = decodeContextId(contextId);
      if (eit.evaluationId != null) {
         this.evaluationId = eit.evaluationId;
         this.jobType = eit.jobType;
      }
   }

   /**
    * Set the evalId and jobType directly and will correctly set the contextId as well
    * 
    * @param evaluationId
    * @param jobType
    * @return the new contextId
    */
   public String setEvalAndType(Long evaluationId, String jobType) {
      this.evaluationId = evaluationId;
      this.jobType = jobType;
      this.contextId = encodeContextId(evaluationId, jobType);
      return this.contextId;
   }

   /**
    * Create a single context string from an evalId and jobType
    * 
    * @param evaluationId
    * @param jobType
    * @return the context string
    */
   public static String encodeContextId(Long evaluationId, String jobType) {
      return evaluationId.toString() + SEPARATOR + jobType;
   }

   /**
    * Decode a contextId in the evalId and jobType
    * 
    * @param contextId and encoded context string
    * @return an eval id type object
    */
   public static EvalIdType decodeContextId(String contextId) {
      EvalIdType rvalue = new EvalIdType();
      if (contextId.indexOf(SEPARATOR) != -1) {
         int index = contextId.indexOf(SEPARATOR);
         rvalue.evaluationId = new Long(contextId.substring(0, index));
         rvalue.jobType = contextId.substring(index + 1);
      }
      return rvalue;
   }

   public static class EvalIdType {
      public Long evaluationId;
      public String jobType;
   }

}