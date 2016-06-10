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
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.model.EvalEvaluation;
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
    
    private EvalEvaluationService evalEvaluationService;
    public void setEvalEvaluationService(EvalEvaluationService evalEvaluationService) {
    this.evalEvaluationService = evalEvaluationService;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    /**
     * Handles the email sending action
     * @return 
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

        EvalEvaluation evaluation = evalEvaluationService.getEvaluationById(evaluationId);
        
        EvalEmailMessage emailMessage = emailsLogic.makeEmailMessage(message, subject, evaluation, null, null);
        String sent[] = emailsLogic.sendEmailMessages(emailMessage.message, emailMessage.subject, evaluationId, evalGroupIds, sendTo);

        messages.addMessage( new TargettedMessage("evalnotify.sent.mails",
                new Object[] { sent.length }, TargettedMessage.SEVERITY_INFO));
        return "success";
    }

}
