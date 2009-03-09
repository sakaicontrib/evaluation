/**
 * TakeEvalBean.java - evaluation - 16 Jan 2007 11:35:56 AM - whumphri
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;


import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.locators.ResponseBeanLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This request-scope bean handles taking evaluations
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TakeEvalBean {

   private static Log log = LogFactory.getLog(TakeEvalBean.class);

   public EvalEvaluation eval;
   public String evalGroupId;
   public Date startDate;

   private ResponseBeanLocator responseBeanLocator;
   public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
      this.responseBeanLocator = responseBeanLocator;
   }

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }
   
   private EvalEmailsLogic emailsLogic;
   public void setEmailsLogic(EvalEmailsLogic emailsLogic) {
      this.emailsLogic = emailsLogic;
   }
   
   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   public String submitEvaluation() {
      log.debug("submit evaluation");
      try {
         responseBeanLocator.saveAll(eval, evalGroupId, startDate);
      } catch (ResponseSaveException e) {
         String messageKey = "unknown.caps";
         if (ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS.equals(e.type)) {
            messageKey = "takeeval.user.must.answer.all.exception";
         } else if (ResponseSaveException.TYPE_BLANK_RESPONSE.equals(e.type)) {
            messageKey = "takeeval.user.blank.response.exception";
         } else if (ResponseSaveException.TYPE_CANNOT_TAKE_EVAL.equals(e.type)) {
            messageKey = "takeeval.user.cannot.take.now.exception";
         }
         messages.addMessage( new TargettedMessage(messageKey, e) );
         return "failure";
      }
      messages.addMessage( new TargettedMessage("evaluations.take.message",
            new Object[] { eval.getTitle(), commonLogic.getDisplayTitle(evalGroupId) }, 
            TargettedMessage.SEVERITY_INFO));
      if(((Boolean) settings.get(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL)).booleanValue()) {
    	  emailsLogic.sendEvalSubmissionConfirmationEmail(eval.getId());
      }
      return "success";
   }

}