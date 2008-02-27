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
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.locators.ResponseBeanLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This request-scope bean handles taking evaluations
 * 
 * @author Will Humphries (whumphri@vt.edu)
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

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   public String submitEvaluation() {
      log.debug("submit evaluation");
      try {
         responseBeanLocator.saveAll(eval, evalGroupId, startDate);
      } catch (IllegalStateException e) {
         // TODO - find a better way to do this, using this as the way to tell that the submission was incomplete, this is not really ideal -AZ
         messages.addMessage( new TargettedMessage("takeeval.user.must.answer.all.exception", new Object[] {},
               TargettedMessage.SEVERITY_ERROR));
         return "failure";
      }
      messages.addMessage( new TargettedMessage("evaluations.take.message",
            new Object[] { eval.getTitle(), externalLogic.getDisplayTitle(evalGroupId) }, 
            TargettedMessage.SEVERITY_INFO));
      return "success";
   }

}