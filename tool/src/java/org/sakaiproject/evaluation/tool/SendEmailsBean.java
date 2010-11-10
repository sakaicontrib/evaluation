/**
 * $Id$
 * $URL$
 * SetupEvalBean.java - evaluation - Mar 18, 2008 4:38:20 PM - azeckoski
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

import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * This action bean helps with the sending evaluation emails manually
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SendEmailsBean {

    /**
     * This should be set to the evalId we are currently working with
     */
    public Long evaluationId;
    public String evalGroupId;
    public String subject;
    public String message;
    public String sendTo;

    private EvalEmailsLogic emailsLogic;
    public void setEmailsLogic(EvalEmailsLogic emailsLogic) {
        this.emailsLogic = emailsLogic;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    /**
     * Handles the email sending action
     */
    public String sendEmailAction() {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }

        if (EvalUtils.isBlank(subject)
                || EvalUtils.isBlank(message)
                || EvalUtils.isBlank(sendTo)) {
            messages.addMessage( new TargettedMessage("evalnotify.all.required",
                    new Object[] {}, TargettedMessage.SEVERITY_ERROR));
            return "failure";
        }

        String[] evalGroupIds = null;
        if (evalGroupId != null) {
            evalGroupIds = new String[] {evalGroupId};
        }
        String[] sent = emailsLogic.sendEmailMessages(message, subject, evaluationId, evalGroupIds, sendTo);

        messages.addMessage( new TargettedMessage("evalnotify.sent.mails",
                new Object[] { sent.length }, TargettedMessage.SEVERITY_INFO));
        return "success";
    }

}
