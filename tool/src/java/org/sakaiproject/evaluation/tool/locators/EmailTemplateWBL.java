/**
 * $Id$
 * $URL$
 * EmailTemplateWBL.java - evaluation - July 25, 2007 4:08:52 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * OTP bean used to locate {@link EvalEmailTemplate}s
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EmailTemplateWBL implements WriteableBeanLocator {

   /**
    * Special case: must add the type of the email template as a suffix after the ":" character
    */
   public static final String NEW_PREFIX = "new";
   /**
    * Special case: must add the type of the email template as a suffix
    */
   public static String NEW_1 = NEW_PREFIX + "1:";

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }


   // keep track of all items that have been delivered during this request
   private Map<String, EvalEmailTemplate> delivered = new HashMap<String, EvalEmailTemplate>();

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
    */
   public Object locateBean(String name) {
      EvalEmailTemplate togo = delivered.get(name);
      if (togo == null) {
         if (name.startsWith(NEW_PREFIX)) {
            String emailTemplateTypeConstant = name.substring(name.indexOf(':') + 1);
            EvalEmailTemplate defaultTemplate = evaluationService.getDefaultEmailTemplate(emailTemplateTypeConstant);
            togo = new EvalEmailTemplate(commonLogic.getCurrentUserId(), emailTemplateTypeConstant,
                  defaultTemplate.getSubject(), defaultTemplate.getMessage());
         } else {
            Long emailTemplateId = Long.valueOf(name);
            togo = evaluationService.getEmailTemplate( emailTemplateId );
         }
         delivered.put(name, togo);
      }
      return togo;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.WriteableBeanLocator#remove(java.lang.String)
    */
   public boolean remove(String name) {
      Long emailTemplateId = Long.valueOf(name);
      evaluationSetupService.removeEmailTemplate(emailTemplateId, commonLogic.getCurrentUserId());
      delivered.remove(name);
      messages.addMessage( new TargettedMessage("controlemailtemplates.template.removed.message",
            new Object[] { emailTemplateId }, 
            TargettedMessage.SEVERITY_INFO));
      return true;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.WriteableBeanLocator#set(java.lang.String, java.lang.Object)
    */
   public void set(String beanname, Object toset) {
      throw new UnsupportedOperationException("Not implemented");
   }

   public void saveAll() {
      for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         EvalEmailTemplate emailTemplate = (EvalEmailTemplate) delivered.get(key);
         if (key.startsWith(NEW_PREFIX)) {
            // add in extra logic needed for new items here
         }
         evaluationSetupService.saveEmailTemplate(emailTemplate, commonLogic.getCurrentUserId());
         messages.addMessage( new TargettedMessage("controlemailtemplates.template.saved.message",
               new Object[] { emailTemplate.getType(), emailTemplate.getSubject() }, 
               TargettedMessage.SEVERITY_INFO));
      }
   }

   /**
    * @return the current email template we are working with or null if none
    */
   public EvalEmailTemplate getCurrentEmailTemplate() {
      EvalEmailTemplate emailTemplate = null;
      if (delivered.size() > 0) {
         emailTemplate = delivered.values().iterator().next();
      }
      return emailTemplate;
   }

}
