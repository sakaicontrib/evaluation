/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.viewparams;

import org.sakaiproject.evaluation.constant.EvalConstants;

/**
 * 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EmailViewParameters extends TemplateViewParameters {
   /**
    * This takes on one of these values EvalConstants.EMAIL_TEMPLATE_AVAILABLE,
    *  EvalConstants.EMAIL_TEMPLATE_REMINDER or {@link EvalConstants.EMAIL_TEMPLATE_SUBMITTED}
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
