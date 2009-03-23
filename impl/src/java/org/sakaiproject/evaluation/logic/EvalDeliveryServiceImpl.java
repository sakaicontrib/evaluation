/**
 * $Id$
 * $URL$
 * EvalDeliveryServiceImpl.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * Implementation for EvalDeliveryService
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalDeliveryServiceImpl implements EvalDeliveryService {

    private static Log log = LogFactory.getLog(EvalDeliveryServiceImpl.class);

    // Event names cannot be over 32 chars long              // max-32:12345678901234567890123456789012
    protected final String EVENT_RESPONSE_CREATED =                   "eval.response.created";
    protected final String EVENT_RESPONSE_UPDATED =                   "eval.response.updated";

    private EvaluationDao dao;
    public void setDao(EvaluationDao dao) {
        this.dao = dao;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings evalSettings) {
        this.settings = evalSettings;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }



    // INIT method
    public void init() {
        log.debug("Init");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalDeliveryService#saveResponse(org.sakaiproject.evaluation.model.EvalResponse, java.lang.String)
     */
    public void saveResponse(EvalResponse response, String userId) {
        log.debug("userId: " + userId + ", response: " + response.getId() + ", evalGroupId: " + response.getEvalGroupId());

        // set the date modified
        response.setLastModified(new Date());

        boolean newResponse = true;
        if (response.getId() != null) {
            newResponse = false;
            // TODO - existing response, don't allow change to any setting
            // except starttime, endtime, and answers
        }

        boolean responseComplete = response.getEndTime() != null;

        // fill in any default values and nulls here

        // check perms and evaluation state
        if (checkUserModifyResponse(userId, response)) {
            // make sure the user can take this evalaution
            Long evaluationId = response.getEvaluation().getId();
            String evalGroupId = response.getEvalGroupId();
            if (! evaluationService.canTakeEvaluation(userId, evaluationId, evalGroupId)) {
                throw new ResponseSaveException("User (" + userId + ") cannot take this evaluation (" + evaluationId
                        + ") in this evalGroupId (" + evalGroupId + ") right now", ResponseSaveException.TYPE_CANNOT_TAKE_EVAL);
            }

            // check to make sure answers are valid for this evaluation
            if (response.getAnswers() != null && !response.getAnswers().isEmpty()) {
                checkAnswersValidForEval(response, responseComplete);
            } else {
                // there are no answers
                if (response.getEndTime() != null) {
                    // the response is complete (submission of an evaluation) and not just creating the empty response
                    // check if answers are required to be filled in
                    Boolean unansweredAllowed = (Boolean)settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
                    if (unansweredAllowed == null) {
                        unansweredAllowed = response.getEvaluation().getBlankResponsesAllowed();
                    }
                    if (! unansweredAllowed) {
                        // all items must be completed so die if they are not
                        throw new ResponseSaveException("User submitted a blank response and there are required answers", 
                                ResponseSaveException.TYPE_BLANK_RESPONSE);
                    }
                }
                if (response.getAnswers() == null) {
                    response.setAnswers(new HashSet<EvalAnswer>());
                }
            }

            // save everything in one transaction

            // response has to be saved first
            Set<EvalResponse> responseSet = new HashSet<EvalResponse>();
            responseSet.add(response);

            Set<EvalAnswer> answersSet = response.getAnswers();

            try {
                dao.saveMixedSet(new Set[] {responseSet, answersSet});
            } catch (Exception e) {
                // failed to save so we should assume for now this is caused by the darn unique constraint
                log.warn("Unable to save response ("+response.getId()+") and answers for this evaluation (" 
                        + evaluationId + ") in this evalGroupId (" + evalGroupId + "): " + e.getMessage());
                // this will produce a nicer message
                throw new ResponseSaveException("User (" + userId + ") cannot save response for this evaluation (" + evaluationId
                        + ") in this evalGroupId (" + evalGroupId + ") right now", ResponseSaveException.TYPE_CANNOT_SAVE);
            }

            String completeMessage = ", response is incomplete";
            if (responseComplete) {
                /* the response is complete (submission of an evaluation) 
                 * and not just creating the empty response so lock related evaluation
                 */
                log.info("Locking evaluation (" + response.getEvaluation().getId() + ") and associated entities");
                EvalEvaluation evaluation = (EvalEvaluation) dao.findById(EvalEvaluation.class, response.getEvaluation().getId());
                dao.lockEvaluation(evaluation, true);
                completeMessage = ", response is complete";
            }

            if (newResponse) {
                commonLogic.registerEntityEvent(EVENT_RESPONSE_CREATED, response);
            } else {
                commonLogic.registerEntityEvent(EVENT_RESPONSE_UPDATED, response);            
            }
            int answerCount = response.getAnswers() == null ? 0 : response.getAnswers().size();
            log.info("User (" + userId + ") saved response (" + response.getId() + ") to" +
                    "evaluation ("+evaluationId+") for groupId (" + response.getEvalGroupId() + ") " +
                    " with " + answerCount + " answers" + completeMessage);
            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User (" + userId + ") could NOT save response (" + response.getId()
                + "), evalGroupId: " + response.getEvalGroupId());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalDeliveryService#getResponseById(java.lang.Long)
     */
    public EvalResponse getResponseById(Long responseId) {
        log.debug("responseId: " + responseId);
        // get the response by passing in id
        EvalResponse response = (EvalResponse) dao.findById(EvalResponse.class, responseId);
        return response;
    }

    public EvalResponse getEvaluationResponseForUserAndGroup(Long evaluationId, String userId, String evalGroupId) {
        if (evaluationId == null || userId == null || evalGroupId == null) {
            throw new IllegalArgumentException("inputs must all be set");
        }
        EvalEvaluation evaluation = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
        if (evaluation == null) {
            throw new IllegalArgumentException("Invalid evaluation, cannot find evaluation: " + evaluationId);
        }

        EvalResponse response = null;
        List<EvalResponse> responses = dao.findBySearch(EvalResponse.class, 
                new Search( new Restriction[] {
                        new Restriction("owner", userId),
                        new Restriction("evaluation.id", evaluationId),
                        new Restriction("evalGroupId", evalGroupId),
                }) );
        if (responses.isEmpty()) {
            // create a new response and save it
            response = new EvalResponse(userId, evalGroupId, evaluation, new Date());
            saveResponse(response, userId);
        } else {
            if (responses.size() == 1) {
                response = responses.get(0);
            } else {
                throw new IllegalStateException("Invalid responses state, this user ("+userId+") has more than 1 response " +
                        "for evaluation ("+evaluationId+") and evalGroupId ("+evalGroupId+")");
            }
        }

        return response;
    }

    public List<EvalResponse> getEvaluationResponsesForUser(String userId, Long[] evaluationIds, Boolean completed) {
        log.debug("userId: " + userId + ", evaluationIds: " + evaluationIds);

        if (evaluationIds.length <= 0) {
            throw new IllegalArgumentException("evaluationIds cannot be empty");
        }

        // check that the ids are actually valid
        int count = (int) dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationIds) );
        if (count != evaluationIds.length) {
            throw new IllegalArgumentException("One or more invalid evaluation ids in evaluationIds: " + evaluationIds);
        }

        Search search = new Search("evaluation.id", evaluationIds);

        // if user is admin then return all matching responses for this evaluation
        if (! commonLogic.isUserAdmin(userId)) {
            // not admin, only return the responses for this user
            search.addRestriction( new Restriction("owner", userId) );
        }

        handleCompleted(completed, search);
        search.addOrder( new Order("id") );

        return dao.findBySearch(EvalResponse.class, search);
    }

    public int countResponses(Long evaluationId, String evalGroupId, Boolean completed) {
        log.debug("evaluationId: " + evaluationId + ", evalGroupId: " + evalGroupId);

        if (dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId)) <= 0l) {
            throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
        }

        Search search = new Search("evaluation.id", evaluationId);

        /* returns count of all responses in all eval groups if evalGroupId is null
         * and returns count of responses in this evalGroupId only if set
         */
        if (evalGroupId != null) {
            search.addRestriction( new Restriction("evalGroupId", evalGroupId) );
        }

        handleCompleted(completed, search);

        return (int) dao.countBySearch(EvalResponse.class, search);
    }

    /**
     * Reduce code duplication be breaking out this common code
     * @param completed
     * @param props
     * @param values
     * @param comparisons
     */
    private void handleCompleted(Boolean completed, Search search) {
        if (completed != null) {
            // if endTime is null then the response is incomplete, if not null then it is complete
            search.addRestriction( new Restriction("endTime", "", completed ? Restriction.NOT_NULL : Restriction.NULL) );
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalDeliveryService#getEvalResponseIds(java.lang.Long, java.lang.String[], java.lang.Boolean)
     */
    public List<Long> getEvalResponseIds(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        log.debug("evaluationId: " + evaluationId);

        if (dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId)) <= 0l) {
            throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
        }

        return dao.getResponseIds(evaluationId, evalGroupIds, null, completed);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalDeliveryService#getEvaluationResponses(java.lang.Long, java.lang.String[], java.lang.Boolean)
     */
    public List<EvalResponse> getEvaluationResponses(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        log.debug("evaluationId: " + evaluationId);

        if (dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId)) <= 0l) {
            throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
        }

        Search search = new Search("evaluation.id", evaluationId);

        if (evalGroupIds != null && evalGroupIds.length > 0) {
            search.addRestriction( new Restriction("evalGroupId", evalGroupIds) );
        }

        handleCompleted(completed, search);
        search.addOrder( new Order("id") );

        return dao.findBySearch(EvalResponse.class, search);
    }



    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalDeliveryService#getAnswersForEval(java.lang.Long, java.lang.String[], java.lang.Long[])
     */
    public List<EvalAnswer> getAnswersForEval(Long evaluationId, String[] evalGroupIds, Long[] templateItemIds) {
        log.debug("evaluationId: " + evaluationId);

        if (dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId)) <= 0l) {
            throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
        }

        List<EvalAnswer> answers = dao.getAnswers(evaluationId, evalGroupIds, templateItemIds);

        for (EvalAnswer answer : answers) {
            // decode the stored answers into the int array
            answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
            // decode NA value
            EvalUtils.decodeAnswerNA(answer);
        }
        return answers;
    }



    // PERMISSIONS

    public boolean canModifyResponse(String userId, Long responseId) {
        log.debug("userId: " + userId + ", responseId: " + responseId);
        // get the response by id
        EvalResponse response = (EvalResponse) dao.findById(EvalResponse.class, responseId);
        if (response == null) {
            throw new IllegalArgumentException("Cannot find response with id: " + responseId);
        }

        // valid state, check perms and locked
        try {
            return checkUserModifyResponse(userId, response);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;
    }

    // INTERNAL METHODS

    /**
     * Check if user has permission to modify this response
     * 
     * @param userId
     * @param response
     * @return true if they do, exception otherwise
     */
    protected boolean checkUserModifyResponse(String userId, EvalResponse response) {
        log.debug("evalGroupId: " + response.getEvalGroupId() + ", userId: " + userId);

        String state = EvalUtils.getEvaluationState(response.getEvaluation(), false);
        if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) || EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
            // admin CAN save responses -AZ
            //       // check admin (admins can never save responses)
            //       if (external.isUserAdmin(userId)) {
            //       throw new IllegalArgumentException("Admin user (" + userId + ") cannot create response ("
            //       + response.getId() + "), admins can never save responses");
            //       }

            // check ownership
            if (response.getOwner().equals(userId)) {
                return true;
            } else {
                throw new SecurityException("User (" + userId + ") cannot modify response (" + response.getId()
                        + ") without permissions");
            }
        } else {
            throw new IllegalStateException("Evaluation state (" + state + ") not valid for modifying responses");
        }
    }

    /**
     * Checks the answers in the response for validity,
     * also will ensure that all answers are actually valid by fixing up the ones which are not quite right if
     * it can be done automaticaly, also optionally checks to make sure all required answers are filled in
     * 
     * @param response the response
     * @param checkRequiredAnswers if true then also check all the required answers have been completed,
     * if false then only check the validity of all current answers
     * @return true if all answers valid, exception otherwise
     */
    protected boolean checkAnswersValidForEval(EvalResponse response, boolean checkRequiredAnswers) {

        // get a list of the valid templateItems for this evaluation
        EvalEvaluation eval = response.getEvaluation();
        Long evaluationId = eval.getId();
        String evalGroupId = response.getEvalGroupId();

        // EVALSYS-618 - handle the special case of instructor/assistant selections
        boolean selectionsEnabled = (Boolean) settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION);
        HashMap<String, Set<String>> typeToIdsFilter = new HashMap<String, Set<String>>();
        if (selectionsEnabled) {
            // check the selections are valid for the response and handle required items in a special way
            EvalAssignGroup assignGroup = evaluationService.getAssignGroupByEvalAndGroupId(evaluationId, evalGroupId);
            Map<String, String> selectionOptions = assignGroup.getSelectionOptions();
            if (! selectionOptions.isEmpty()) {
                // validate the selections for the response against the assign group
                Map<String, String[]> selections = response.getSelections();
                if (selections.isEmpty()) {
                    throw new IllegalArgumentException("Selections are invalid for this evaluation ("+evaluationId+") and group ("+evalGroupId+"): "
                    		+ " no selections in the response, but this assign group ("+assignGroup+") requires them: " + selectionOptions);
                }
                if (! selectionOptions.keySet().equals(selections.keySet())) {
                    throw new IllegalArgumentException("Selections are invalid for this evaluation ("+evaluationId+") and group ("+evalGroupId+"): "
                            + " selections in the response ("+response+") do not match the selections in the assign group ("+assignGroup+"): "
                            + selectionOptions.keySet()+" != "+selections.keySet());
                }
                // expose the map of selection type => selection Ids so we can filter required items out
                for (Entry<String, String[]> entry : selections.entrySet()) {
                    HashSet<String> s = new HashSet<String>();
                    String[] ids = entry.getValue();
                    for (String id : ids) {
                        s.add(id);
                    }
                    typeToIdsFilter.put(entry.getKey(), s);
                }
            } else {
                // clear the selections code, they are not used for this assign group
                response.setSelectionsCode(null);
            }
        } else {
            // system setting disabled so clear them
            response.setSelectionsCode(null);
        }

        // see if this eval allows blank responses
        boolean requireNonBlankAnswerableItemsForEval = false; // default to skipping the check
        if (checkRequiredAnswers) {
            Boolean unansweredAllowed = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
            if (unansweredAllowed == null) {
                // configured per eval
                unansweredAllowed = eval.getBlankResponsesAllowed();
            }
            if (unansweredAllowed != null) {
                // convert to easier to understand boolean
                requireNonBlankAnswerableItemsForEval = ! unansweredAllowed;
            }
        }

        // make the TI data structure and get the flat list of DTIs
        TemplateItemDataList tidl = new TemplateItemDataList(evaluationId, evalGroupId, 
                evaluationService, authoringService, hierarchyLogic, null);

        List<DataTemplateItem> allDTIs = tidl.getFlatListOfDataTemplateItems(true);

        // create the set of answerable items
        Set<Long> answerableTemplateItemIds = new HashSet<Long>();
        Set<String> requiredAnswerKeys = new HashSet<String>();
        for (DataTemplateItem dti : allDTIs) {
            if (dti.isAnswerable()) {
                answerableTemplateItemIds.add(dti.templateItem.getId());
            }
            if (checkRequiredAnswers) {
                boolean required = false;
                if (requireNonBlankAnswerableItemsForEval) {
                    // all items which can be answered must be
                    required = dti.isRequireable();
                } else {
                    // only items marked as compulsory must be answered
                    required = dti.isCompulsory();
                }
                // if the item is required then add it to the required keys listing
                if (required) {
                    boolean filteredOut = false;
                    if (selectionsEnabled 
                            && typeToIdsFilter.size() > 0) {
                        /* Do a selection filter check here,
                         * Only include dtis where the associateType is not matched OR
                         * (the associateType is matched AND the id is also matched)
                         */
                        Set<String> ids = typeToIdsFilter.get(dti.associateType);
                        if (ids != null) {
                            // check the id as well
                            if (! ids.contains(dti.associateId)) {
                                filteredOut = true;
                            }
                        }
                    }
                    if (! filteredOut) {
                        requiredAnswerKeys.add(dti.getKey());
                    }
                }
            }
        }

        // check the validity of all answers (just making sure they are not invalid)
        Set<String> answeredAnswerKeys = new HashSet<String>();
        for (EvalAnswer answer : response.getAnswers()) {

            // check the answer for correctness
            if (answer.getNumeric() == null && answer.getText() == null && 
                    (answer.getMultiAnswerCode() == null || answer.getMultiAnswerCode().length() == 0) ) {
                throw new IllegalArgumentException("Cannot save blank answers: answer for templateItem: "
                        + answer.getTemplateItem().getId());
            }

            // make sure the item and template item are available for this answer
            if (answer.getTemplateItem() == null || answer.getTemplateItem().getItem() == null) {
                throw new IllegalArgumentException("NULL templateItem or templateItem.item for answer: " 
                        + "Answers must have the templateItem set, and that must have the item set");
            }

            // decode NA value
            EvalUtils.decodeAnswerNA(answer);

            // verify the base state of new answers
            if (answer.getId() == null) {
                // force the item to be set correctly
                answer.setItem(answer.getTemplateItem().getItem()); // LAZY LOAD

                // check that the associated id is filled in for associated items
                if (EvalConstants.ITEM_CATEGORY_COURSE.equals(answer.getTemplateItem().getCategory())) {
                    if (answer.getAssociatedId() != null) {
                        log.warn("Course answer (key="+ TemplateItemUtils.makeTemplateItemAnswerKey(answer.getTemplateItem().getId(), 
                                answer.getAssociatedType(), answer.getAssociatedId()) +") should have the associated id "
                                + "("+answer.getAssociatedId()+") field null, "
                                + "for templateItem (" + answer.getTemplateItem().getId() + "), setting associated id and type to null");
                    }
                    answer.setAssociatedId(null);
                    answer.setAssociatedType(null);
                } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(answer.getTemplateItem().getCategory())) {
                    if (answer.getAssociatedId() == null) {
                        throw new IllegalArgumentException(
                                "Instructor answers must have the associated field filled in with the instructor userId, for templateItem: "
                                + answer.getTemplateItem().getId());
                    }
                    answer.setAssociatedType(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
                } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(answer.getTemplateItem().getCategory())) {
                    if (answer.getAssociatedId() == null) {
                        throw new IllegalArgumentException(
                                "assistant answers must have the associated field filled in with the assistant userId, for templateItem: "
                                + answer.getTemplateItem().getId());
                    }
                    answer.setAssociatedType(EvalConstants.ITEM_CATEGORY_ASSISTANT);
                } else if (EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(answer.getTemplateItem().getCategory())) {
                    if (answer.getAssociatedId() == null) {
                        throw new IllegalArgumentException(
                                "Environment answers must have the associated field filled in with the unique environment id, for templateItem: "
                                + answer.getTemplateItem().getId());
                    }
                    answer.setAssociatedType(EvalConstants.ITEM_CATEGORY_ENVIRONMENT);
                } else {
                    throw new IllegalArgumentException("Do not know how to handle a templateItem category of ("
                            + answer.getTemplateItem().getCategory() + ") for templateItem: "
                            + answer.getTemplateItem().getId());
                }

                // make sure answer is associated with a valid templateItem for this evaluation
                if (! answerableTemplateItemIds.contains(answer.getTemplateItem().getId())) {
                    throw new IllegalArgumentException("This answer templateItem (" + answer.getTemplateItem().getId()
                            + ") is not part of this evaluation (" + response.getEvaluation().getTitle() + ")");
                }
            }

            // TODO - check if numerical answers are valid? (i.e. within the size of the scale)

            // add the unique answer key (TIId + assocType + assocId) for this answer to the answered keys set
            answeredAnswerKeys.add( TemplateItemUtils.makeTemplateItemAnswerKey(answer.getTemplateItem().getId(), 
                    answer.getAssociatedType(), answer.getAssociatedId()) );
        }

        if (checkRequiredAnswers) {
            // check if required answers are filled in
            if (! requiredAnswerKeys.isEmpty()) {
                // remove all the answered keys from the required keys and if everything was answered then there will be nothing left in the required keys set
                requiredAnswerKeys.removeAll(answeredAnswerKeys);
                if (requiredAnswerKeys.size() > 0) {
                    String[] reqAnsKeysArray = requiredAnswerKeys.toArray(new String[requiredAnswerKeys.size()]);
                    throw new ResponseSaveException("Missing " + requiredAnswerKeys.size() 
                            + " answers for required items (received "+answeredAnswerKeys.size()+" answers) for this evaluation"
                            + " response (" + response.getId() + ") for user (" + response.getOwner() + ")"
                            + " :: missing keys=" + ArrayUtils.arrayToString(reqAnsKeysArray) 
                            + " :: received keys=" + ArrayUtils.arrayToString(answeredAnswerKeys.toArray(new String[answeredAnswerKeys.size()])), 
                            reqAnsKeysArray );
                }
            }
        }

        return true;
    }

}
