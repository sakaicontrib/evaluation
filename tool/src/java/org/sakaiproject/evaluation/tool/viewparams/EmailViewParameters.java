/*
 * Created on 18 Feb 2007
 */
package org.sakaiproject.evaluation.tool.viewparams;

public class EmailViewParameters extends TemplateViewParameters {
   /**
    * This takes on one of the two values EvalConstants.EMAIL_TEMPLATE_AVAILABLE
    * or EvalConstants.EMAIL_TEMPLATE_REMINDER
    */
   public String emailType;
   public boolean inEval = false;

   public EmailViewParameters() {
   }

   public EmailViewParameters(String viewID, Long templateId, String emailType) {
      this.viewID = viewID;
      this.templateId = templateId;
      this.emailType = emailType;
   }

   public EmailViewParameters(String viewID, Long templateId, String emailType, boolean inEval) {
      super(viewID, templateId);
      this.emailType = emailType;
      this.inEval = inEval;
   }
  
}
