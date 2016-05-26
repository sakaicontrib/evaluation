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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
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

    private static final Log LOG = LogFactory.getLog(TakeEvalBean.class);

    public EvalEvaluation eval;
    public String evalGroupId;
    public Date startDate;
    /**
     * selection Id values to populate {@link TakeEvalBean.setSelectionOptions}
     */
    public String[] selectioninstructorIds;
    public String[] selectionassistantIds;

    private ResponseBeanLocator responseBeanLocator;
    public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
        this.responseBeanLocator = responseBeanLocator;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }
    
    public String saveEvaluationWithoutSubmit() {
        LOG.debug("save evaluation without submit");
        try {
            Map<String, String[]> selectionOptions = new HashMap<>();
            if (selectioninstructorIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, selectioninstructorIds);
                }
            if (selectionassistantIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, selectionassistantIds); 
            }
            responseBeanLocator.saveAll(eval, evalGroupId, startDate, selectionOptions, false);
        } catch (ResponseSaveException e) {
            String messageKey;
            if (ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS.equals(e.type)) {
                messageKey = "takeeval.user.must.answer.all.exception";
            } else if (ResponseSaveException.TYPE_BLANK_RESPONSE.equals(e.type)) {
                messageKey = "takeeval.user.blank.response.exception";
            } else if (ResponseSaveException.TYPE_CANNOT_TAKE_EVAL.equals(e.type)) {
                messageKey = "takeeval.user.cannot.take.now.exception";
            } else {
                messageKey = "takeeval.user.cannot.save.reponse";
            }
            messages.addMessage(new TargettedMessage(messageKey, e));
            return "failure";
        }
        messages.addMessage(new TargettedMessage("evaluations.save.no.submit.message", new Object[] {
                eval.getTitle(), commonLogic.getDisplayTitle(evalGroupId) },
                TargettedMessage.SEVERITY_INFO));
        return "success";
    }

    public String submitEvaluation() {
        LOG.debug("submit evaluation");
        try {
        	Map<String, String[]> selectionOptions = new HashMap<>();
            if (selectioninstructorIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, selectioninstructorIds);
                }
            if (selectionassistantIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, selectionassistantIds); 
            }
            responseBeanLocator.saveAll(eval, evalGroupId, startDate, selectionOptions);
        } catch (ResponseSaveException e) {
            String messageKey;
            if (ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS.equals(e.type)) {
                messageKey = "takeeval.user.must.answer.all.exception";
            } else if (ResponseSaveException.TYPE_BLANK_RESPONSE.equals(e.type)) {
                messageKey = "takeeval.user.blank.response.exception";
            } else if (ResponseSaveException.TYPE_CANNOT_TAKE_EVAL.equals(e.type)) {
                messageKey = "takeeval.user.cannot.take.now.exception";
            } else {
                messageKey = "takeeval.user.cannot.save.reponse";
            }
            messages.addMessage(new TargettedMessage(messageKey, e));
            return "failure";
        }
        messages.addMessage(new TargettedMessage("evaluations.take.message", new Object[] {
                eval.getTitle(), commonLogic.getDisplayTitle(evalGroupId) },
                TargettedMessage.SEVERITY_INFO));
        return "success";
    }

}