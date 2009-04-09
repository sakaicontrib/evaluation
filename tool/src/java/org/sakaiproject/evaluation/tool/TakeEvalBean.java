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

    private static Log log = LogFactory.getLog(TakeEvalBean.class);

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

    public String submitEvaluation() {
        log.debug("submit evaluation");
        try {
        	Map<String, String[]> selectionOptions = new HashMap<String, String[]>();
            if (selectioninstructorIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, selectioninstructorIds);
                }
            if (selectionassistantIds != null) {
                selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, selectionassistantIds); 
            }
            responseBeanLocator.saveAll(eval, evalGroupId, startDate, selectionOptions);
        } catch (ResponseSaveException e) {
            String messageKey = "unknown.caps";
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