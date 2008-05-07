/**
 * $Id$
 * $URL$
 * EmailViewParameters.java - evaluation - Mar 20, 2008 10:12:38 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.viewparams;

/**
 * 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EmailViewParameters extends TemplateViewParameters {
   /**
    * This takes on one of the two values EvalConstants.EMAIL_TEMPLATE_AVAILABLE
    * or EvalConstants.EMAIL_TEMPLATE_REMINDER
    */
   public String emailType;
   /**
    * if we are working with an evaluation email template then this should be set 
    */
   public Long evaluationId;

   public EmailViewParameters() {
   }

   public EmailViewParameters(String viewID, Long templateId, String emailType) {
      this.viewID = viewID;
      this.templateId = templateId;
      this.emailType = emailType;
   }

   public EmailViewParameters(String viewID, Long templateId, String emailType, Long evaluationId) {
      super(viewID, templateId);
      this.emailType = emailType;
      this.evaluationId = evaluationId;
   }
  
}
