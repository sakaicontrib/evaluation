/**
 * $Id$
 * $URL$
 * EvalEvaluationServiceImpl.java - evaluation - Jan 28, 2008 5:52:17 PM - azeckoski
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Implementation of the evals service,
 * this is a LOWER level service and should have dependencies on LOWER and BOTTOM level services only
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalEvaluationServiceImpl implements EvalEvaluationService {

    private static Log log = LogFactory.getLog(EvalEvaluationServiceImpl.class);

    // Event names cannot be over 32 chars long              // max-32:12345678901234567890123456789012
    protected final String EVENT_EVAL_STATE_START =                   "eval.evaluation.state.start";
    protected final String EVENT_EVAL_STATE_DUE =                     "eval.evaluation.state.due";
    protected final String EVENT_EVAL_STATE_STOP =                    "eval.evaluation.state.stop";
    protected final String EVENT_EVAL_STATE_VIEWABLE =                "eval.evaluation.state.viewable";

    private EvaluationDao dao;
    public void setDao(EvaluationDao dao) {
        this.dao = dao;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalSecurityChecksImpl securityChecks;
    public void setSecurityChecks(EvalSecurityChecksImpl securityChecks) {
        this.securityChecks = securityChecks;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluationById(java.lang.Long)
     */
    public EvalEvaluation getEvaluationById(Long evaluationId) {
        log.debug("evalId: " + evaluationId);
        EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
        fixupEvaluation(eval);
        return eval;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#checkEvaluationExists(java.lang.Long)
     */
    public boolean checkEvaluationExists(Long evaluationId) {
        if (evaluationId == null) {
            throw new NullPointerException("evaluationId cannot be null");
        }
        boolean exists = false;
        long count = dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId));
        exists = count > 0l;
        return exists;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluationByEid(java.lang.String)
     */
    public EvalEvaluation getEvaluationByEid(String eid) {
        EvalEvaluation evalEvaluation = null;
        if (eid != null) {
            evalEvaluation = dao.findOneBySearch(EvalEvaluation.class, new Search("eid", eid));
        }
        fixupEvaluation(evalEvaluation);
        return evalEvaluation;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#countEvaluationsByTemplateId(java.lang.Long)
     */
    public int countEvaluationsByTemplateId(Long templateId) {
        log.debug("templateId: " + templateId);
        Search search = makeSearchForEvalsByTemplate(templateId);
        int count = (int) dao.countBySearch(EvalEvaluation.class, search );
        return count;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluationsByTemplateId(java.lang.Long)
     */
    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
        log.debug("templateId: " + templateId);
        Search search = makeSearchForEvalsByTemplate(templateId);
        List<EvalEvaluation> evals = dao.findBySearch(EvalEvaluation.class, search);
        for (EvalEvaluation evaluation : evals) {
            fixupEvaluation(evaluation);
        }
        return evals;
    }

    /**
     * @param templateId unique id of a template (must be set or exception occurs)
     * @return the search which will find evals based on a template id
     */
    private Search makeSearchForEvalsByTemplate(Long templateId) {
        int count = (int) dao.countBySearch(EvalTemplate.class, new Search("id", templateId) );
        if (count <= 0) {
            throw new IllegalArgumentException("Cannot find template with id: " + templateId);
        }
        Search search = new Search(
                new Restriction[] {
                        new Restriction("template.id", templateId),
                        new Restriction("state", EvalConstants.EVALUATION_STATE_PARTIAL, Restriction.NOT_EQUALS),
                        new Restriction("state", EvalConstants.EVALUATION_STATE_DELETED, Restriction.NOT_EQUALS)
                }
        );
        return search;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#updateEvaluationState(java.lang.Long)
     */
    public String updateEvaluationState(Long evaluationId) {
        log.debug("evalId: " + evaluationId);
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        // fix the state of this eval if needed, save it, and return the state constant 
        return returnAndFixEvalState(eval, true);
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#returnAndFixEvalState(org.sakaiproject.evaluation.model.EvalEvaluation, boolean)
     */
    public String returnAndFixEvalState(EvalEvaluation evaluation, boolean saveState) {
        String trueState = EvalUtils.getEvaluationState(evaluation, true);
        // check state against stored state
        if (EvalConstants.EVALUATION_STATE_UNKNOWN.equals(trueState)) {
            log.warn("Evaluation ("+evaluation.getTitle()+") in UNKNOWN state");
        } else if ( EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState()) 
                || EvalConstants.EVALUATION_STATE_DELETED.equals(evaluation.getState()) ) {
            // never fix the state if it is currently in a special state
            trueState = evaluation.getState();
        } else {
            // compare state and set if not equal
            if (! trueState.equals(evaluation.getState()) ) {
                evaluation.setState(trueState);
                // will only save the state if this eval is already saved
                if ( (evaluation.getId() != null) && saveState) {
                    if ( EvalConstants.EVALUATION_STATE_ACTIVE.equals(trueState) ) {
                        commonLogic.registerEntityEvent(EVENT_EVAL_STATE_START, evaluation);
                    } else if ( EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(trueState) ) {
                        commonLogic.registerEntityEvent(EVENT_EVAL_STATE_DUE, evaluation);
                    } else if ( EvalConstants.EVALUATION_STATE_CLOSED.equals(trueState) ) {
                        commonLogic.registerEntityEvent(EVENT_EVAL_STATE_STOP, evaluation);
                    } else if ( EvalConstants.EVALUATION_STATE_VIEWABLE.equals(trueState) ) {
                        commonLogic.registerEntityEvent(EVENT_EVAL_STATE_VIEWABLE, evaluation);
                    }
                    dao.update(evaluation);
                }
            }
        }
        return trueState;
    }


    // USER ASSIGNMENTS

    /**
     * @deprecated use {@link #getParticipantsForEval(Long, String, String, String, String, String, String)}
     */
    public Set<String> getUserIdsTakingEvalInGroup(Long evaluationId, String evalGroupId,
            String includeConstant) {
        EvalUtils.validateEmailIncludeConstant(includeConstant);
        Set<String> userIds = null;
        if (EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant)) {
            // get all users who have NOT responded
            userIds = commonLogic.getUserIdsForEvalGroup(evalGroupId,
                    EvalConstants.PERM_TAKE_EVALUATION);
            Set<String> respondedUserIds = dao.getResponseUserIds(evaluationId,
                    new String[] { evalGroupId });
            // subtract responded users from the total list of users who can take to get the
            // non-responders
            userIds.removeAll(respondedUserIds);
        } else if (EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant)) {
            // get all users who have responded
            userIds = dao.getResponseUserIds(evaluationId, new String[] { evalGroupId });
        } else if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant)) {
            // get all users permitted to take the evaluation
            userIds = commonLogic.getUserIdsForEvalGroup(evalGroupId,
                    EvalConstants.PERM_TAKE_EVALUATION);
        }
        if (userIds == null) {
            userIds = new HashSet<String>();
        }
        return userIds;
    }

    public EvalAssignUser getAssignUserByEid(String eid) {
        EvalAssignUser eau = null;
        if (eid != null) {
            eau = dao.findOneBySearch(EvalAssignUser.class, new Search("eid", eid));
        }
        return eau;
    }

    public EvalAssignUser getAssignUserById(Long assignUserId) {
        EvalAssignUser eau = (EvalAssignUser) dao.findById(EvalAssignUser.class, assignUserId);
        return eau;
    }

    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId,
            String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, 
            String includeConstant, String evalStateConstant) {
        // validate arguments
        if (evaluationId == null && (userId == null || "".equals(userId)) ) {
            throw new IllegalArgumentException("At least one of the following must be set: evaluationId, userId");
        }
        /**
        // create the search
        Search search = new Search();
        if (evaluationId != null) {
            // getEvaluationOrFail(evaluationId); // took out eval fetch for now
            search.addRestriction( new Restriction("evaluation.id", evaluationId) );
        }
        if (evalStateConstant != null) {
            EvalUtils.validateStateConstant(evalStateConstant);
            search.addRestriction( new Restriction("evaluation.state", evalStateConstant) );
        }
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            search.addRestriction( new Restriction("evalGroupId", evalGroupIds) );
        }
        if (assignTypeConstant != null 
                && includeConstant == null) {
            EvalAssignUser.validateType(assignTypeConstant);
            // only set this if the includeConstant is not set
            search.addRestriction( new Restriction("type", assignTypeConstant) );
        }
        if (assignStatusConstant == null) {
            search.addRestriction( new Restriction("status", EvalAssignUser.STATUS_REMOVED, Restriction.NOT_EQUALS) );
        } else if (STATUS_ANY.equals(assignStatusConstant)) {
            // no restriction needed in this case
        } else {
            EvalAssignUser.validateStatus(assignStatusConstant);
            search.addRestriction( new Restriction("status", assignStatusConstant) );
        }
        if (userId != null && ! "".equals(userId)) {
            search.addRestriction( new Restriction("userId", userId) );
        }
        boolean includeFilterUsers = false;
        Set<String> userFilter = null;
        if (includeConstant != null) {
            EvalUtils.validateEmailIncludeConstant(includeConstant);
            String[] groupIds = new String[] {};
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                groupIds = evalGroupIds;
            }
            // force the results to only include eval takers
            search.addRestriction( new Restriction("type", EvalAssignUser.TYPE_EVALUATOR) );
            // now set up the filter
            if (EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant)) {
                // get all users who have NOT responded
                userFilter = dao.getResponseUserIds(evaluationId, groupIds);
                includeFilterUsers = false;
            } else if (EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant)) {
                // get all users who have responded
                userFilter = dao.getResponseUserIds(evaluationId, groupIds);
                includeFilterUsers = true;
            } else if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant)) {
                // do nothing
            } else {
                throw new IllegalArgumentException("Unknown includeConstant: " + includeConstant);
            }
        }
        // get the assignments based on the search
        List<EvalAssignUser> results = dao.findBySearch(EvalAssignUser.class, search);
        List<EvalAssignUser> assignments = new ArrayList<EvalAssignUser>( results );
        // This code is potentially expensive but there is not really a better way to handle it -AZ
        if (userFilter != null && ! userFilter.isEmpty()) {
            // filter the results based on the userFilter
            for (Iterator<EvalAssignUser> iterator = assignments.iterator(); iterator.hasNext();) {
                EvalAssignUser evalAssignUser = iterator.next();
                String uid = evalAssignUser.getUserId();
                if (includeFilterUsers) {
                    // only include users in the filter
                    if (! userFilter.contains(uid)) {
                        iterator.remove();
                    }
                } else {
                    // exclude all users in the filter
                    if (userFilter.contains(uid)) {
                        iterator.remove();
                    }
                }
            }
        }
        **/
        // this is handled in the DAO now
        List<EvalAssignUser> assignments = dao.getParticipantsForEval(evaluationId, userId, evalGroupIds, assignTypeConstant, assignStatusConstant, includeConstant, evalStateConstant);
        return assignments;
    }

    public int countParticipantsForEval(Long evaluationId, String[] evalGroupIds) {
        int totalEnrollments = 0;
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        // only counting if the eval is not anonymous, anon is always effectively 0
        if (! EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
            // count the participants
            Search search = new Search("evaluation.id", evaluationId);
            // only include evaluators which are not removed
            search.addRestriction( new Restriction("type", EvalAssignUser.TYPE_EVALUATOR) );
            search.addRestriction( new Restriction("status", EvalAssignUser.STATUS_REMOVED, Restriction.NOT_EQUALS) );
            // limit to a group if requested
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                search.addRestriction( new Restriction("evalGroupId", evalGroupIds) );
            }
            totalEnrollments = (int) dao.countBySearch(EvalAssignUser.class, search);
        }
        return totalEnrollments;
    }

    /**
     * JIRA EvalSys-588
     */
    public boolean isEvalGroupValidForEvaluation(String evalGroupId, Long evaluationId) {
        // grab the evaluation itself first
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        boolean valid = false;
        if (checkEvalStateValidForTaking(eval)) {
            valid = checkEvalGroupValidForEval(eval, evalGroupId);
        }
        return valid;
    }

    /**
     * Checks if the state of an evaluation is valid for taking it
     * @param eval an eval (cannot be null)
     * @return true if state is valid OR fale if not
     */
    private boolean checkEvalStateValidForTaking(EvalEvaluation eval) {
        boolean valid = false;
        // check the evaluation state
        if (eval != null) {
            String state = EvalUtils.getEvaluationState(eval, false);
            if ( ! EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) &&
                    ! EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(state) ) {
                //log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") when eval state is: " + state);
                valid = false;
            } else {
                valid = true;
            }
        }
        return valid;
    }

    /**
     * Checks that the group is valid for this evaluation
     * @param eval the evaluation
     * @param evalGroupId (OPTIONAL) the group id, if not set then do a check to make sure at least one group is set and valid
     * @param includeStateCheck if true then also check the state of the eval is valid for taking
     * @return true if the group is valid for this eval
     */
    private boolean checkEvalGroupValidForEval(EvalEvaluation eval, String evalGroupId) {
        if (eval == null) {
            throw new IllegalArgumentException("eval must be set and cannot be null");
        }
        boolean valid = false;
        String userId = commonLogic.getCurrentUserId();
        Long evaluationId = eval.getId();
        if ( commonLogic.isUserAdmin(userId) ) {
            // admin trumps being in a group
            valid = true;
        } else {
            Search search = new Search(
                    new Restriction[] {
                            new Restriction("evaluation.id", evaluationId),
                            new Restriction("instructorApproval", Boolean.TRUE)
                    });
            if (evalGroupId == null) {
                // no groupId is supplied so do a simpler check
                // make sure at least one group is valid for this eval
            } else {
                // check that the evalGroupId is valid for this evaluation
                search.addRestriction( new Restriction("evalGroupId", evalGroupId) );
            }
            // do the count based on the search
            long count = dao.countBySearch(EvalAssignGroup.class, search);
            if (count <= 0l) {
                // no valid groups
                valid = false;
            } else {
                valid = true;
            }
        }
        return valid;
    }


    // PERMISSIONS

    public boolean canTakeEvaluation(String userId, Long evaluationId, String evalGroupId) {
        log.debug("evalId: " + evaluationId + ", userId: " + userId + ", evalGroupId: " + evalGroupId);

        // grab the evaluation itself first
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        boolean allowed = false;
        if (checkEvalStateValidForTaking(eval)) {
            // valid state
            if (checkEvalGroupValidForEval(eval, evalGroupId) ) {
                // valid group (or at least some groups are valid for this eval)
                if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl()) ) {
                    // if this is anonymous then group membership does not matter
                    allowed = true;
                } else if ( EvalConstants.EVALUATION_AUTHCONTROL_KEY.equals(eval.getAuthControl()) ) {
                    // if this uses a key then only the key matters
                    // TODO add key check
                    allowed = false;
                } else if ( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ.equals(eval.getAuthControl()) ) {
                    if (commonLogic.isUserAdmin(userId) ) {
                        // short circuit the attempt to lookup every group in the system for the admin
                        allowed = true;
                    } else {
                        if (evalGroupId == null) {
                            // if no groupId is supplied then simply check to see if the user is in any of the groups assigned,
                            // hopefully this is faster than checking if the user has the right permission in every group -AZ
                            List<EvalAssignUser> userAssigns = getParticipantsForEval(evaluationId, userId, null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                            if (! userAssigns.isEmpty()) {
                                HashSet<String> egids = new HashSet<String>();
                                for (EvalAssignUser evalAssignUser : userAssigns) {
                                    if (evalAssignUser.getEvalGroupId() != null) {
                                        evalAssignUser.getEvalGroupId();
                                    }
                                }
                                String[] evalGroupIds = egids.toArray(new String[egids.size()]);
                                long count = dao.countBySearch(EvalAssignGroup.class, new Search(
                                        new Restriction[] {
                                                new Restriction("evaluation.id", evaluationId),
                                                new Restriction("instructorApproval", Boolean.TRUE),
                                                new Restriction("evalGroupId", evalGroupIds)
                                        }) );
                                if (count > 0l) {
                                    // ok if at least one group is approved and in the set of groups this user can take evals in for this eval id
                                    allowed = true;
                                } else {
                                    allowed = false;
                                }
                            }
                        } else {
                            // check the user permissions
                            List<EvalAssignUser> userAssigns = getParticipantsForEval(evaluationId, userId, null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                            if (userAssigns.isEmpty()) {
                                log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") without permission");
                                allowed = false;
                            } else {
                                // check if the eval allows multiple submissions
                                if (eval.getModifyResponsesAllowed() != null &&
                                        eval.getModifyResponsesAllowed() == Boolean.FALSE) {
                                    // cannot modify responses
                                    // check if the user already took this evaluation for this group
                                    EvalResponse response = getResponseForUserAndGroup(evaluationId, userId, evalGroupId);
                                    if (response != null) {
                                        // user already has a response saved for this evaluation and evalGroupId
                                        log.info("User (" + userId + ") cannot take evaluation (" + evaluationId 
                                                + ") again in this group (" + evalGroupId 
                                                + "), completed response exists ("+response.getId()+") from " 
                                                + response.getEndTime() + " and this evaluation does not allow multiple attempts");
                                        allowed = false;
                                    } else {
                                        // multiple responses ok
                                        allowed = true;
                                    }
                                } else {
                                    // multiple responses ok
                                    allowed = true;
                                }
                            }
                        } // evalgroupid null
                    } // is admin
                } // auth type
            } else {
                // invalid group
                if (evalGroupId == null) {
                    log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + "), there are no enabled groups assigned");
                } else {
                    log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") with group ("+evalGroupId+"), group disabled or user not a member");
                }
            }
        } else {
            // invalid state
            String state = EvalUtils.getEvaluationState(eval, false);
            log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") when eval state is: " + state);
        }
        return allowed;
    }

    public boolean canBeginEvaluation(String userId) {
        log.debug("Checking begin eval for: " + userId);
        boolean isAdmin = commonLogic.isUserAdmin(userId);
        if ( isAdmin && (dao.countAll(EvalTemplate.class) > 0) ) {
            // admin can access all templates and create an evaluation if 
            // there is at least one template
            return true;
        }
        Boolean instructorAllowedCreateEvals = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS);
        if (instructorAllowedCreateEvals != null && instructorAllowedCreateEvals) {
            if ( commonLogic.countEvalGroupsForUser(userId, EvalConstants.PERM_ASSIGN_EVALUATION) > 0 ) {
                log.debug("User has permission to assign evaluation in at least one group");
                /*
                 * TODO - this check needs to be more robust at some point
                 * currently we are ignoring shared and visible templates - AZ
                 */
                int count = dao.countSharedEntitiesForUser(EvalTemplate.class, userId, 
                        new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_PRIVATE}, 
                        null, null, null, new String[] {"notEmpty"});
                if (count > 0 ) {
                    // if they can access at least one template with an item then they can create an evaluation
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canControlEvaluation(String userId, Long evaluationId) {
        log.debug("evalId: " + evaluationId + ",userId: " + userId);
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        return securityChecks.canUserControlEvaluation(userId, eval);
    }

    public boolean canRemoveEvaluation(String userId, Long evaluationId) {
        log.debug("evalId: " + evaluationId + ",userId: " + userId);
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        boolean allowed = false;
        try {
            allowed = securityChecks.canUserRemoveEval(userId, eval);
        } catch (IllegalStateException e) {
            allowed = false;
        } catch (SecurityException e) {
            log.debug("User ("+userId+") cannot remove evalaution: " + eval.getId() + ", " + e.getMessage());
            allowed = false;
        }
        return allowed;
    }

    // EVAL AND ASSIGN GROUPS

    public int countEvaluationGroups(Long evaluationId, boolean includeUnApproved) {
        log.debug("evalId: " + evaluationId);

        Search search = new Search("evaluation.id", evaluationId);

        if (! includeUnApproved) {
            // only include those that are approved
            search.addRestriction( new Restriction("instructorApproval", Boolean.TRUE) );
        }

        int count = (int) dao.countBySearch(EvalAssignGroup.class, search);
        return count;
    }

    public EvalAssignGroup getAssignGroupByEid(String eid) {
        EvalAssignGroup eag = null;
        if (eid != null) {
            eag = dao.findOneBySearch(EvalAssignGroup.class, new Search("eid", eid));
        }
        return eag;
    }


    public EvalAssignGroup getAssignGroupById(Long assignGroupId) {
        log.debug("assignGroupId: " + assignGroupId);
        EvalAssignGroup eag = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroupId);
        return eag;
    }

    public Long getAssignGroupId(Long evaluationId, String evalGroupId) {
        log.debug("evaluationId: " + evaluationId + ", evalGroupId: " + evalGroupId);
        Long agid = null;
        EvalAssignGroup assignGroup = dao.findOneBySearch(EvalAssignGroup.class, new Search(
                new Restriction[] {
                        new Restriction("evaluation.id", evaluationId),
                        new Restriction("evalGroupId", evalGroupId)
                }) );
        if (assignGroup != null) {
            agid = assignGroup.getId();
        }
        return agid;
    }

    public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId) {
        List<EvalAssignHierarchy> l = dao.findBySearch(EvalAssignHierarchy.class, new Search(
                new Restriction[] {
                        new Restriction("evaluation.id", evaluationId),
                        new Restriction("nodeId", "", Restriction.NOT_NULL)
                }, new Order("id")) );
        return l;
    }


    public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId) {
        EvalAssignHierarchy eah = (EvalAssignHierarchy) dao.findById(EvalAssignHierarchy.class, assignHierarchyId);
        return eah;
    }


    public Map<Long, List<EvalAssignGroup>> getAssignGroupsForEvals(Long[] evaluationIds,
            boolean includeUnApproved, Boolean includeHierarchyGroups) {
        log.debug("evalIds: " + evaluationIds + ", includeUnApproved=" + includeUnApproved);
        Map<Long, List<EvalAssignGroup>> evals = new TreeMap<Long, List<EvalAssignGroup>>();

        // create the inner lists
        for (int i=0; i<evaluationIds.length; i++) {
            List<EvalAssignGroup> innerList = new ArrayList<EvalAssignGroup>();
            evals.put(evaluationIds[i], innerList);
        }

        Search search = new Search("evaluation.id", evaluationIds);

        if (! includeUnApproved) {
            // only include those that are approved
            search.addRestriction( new Restriction("instructorApproval", Boolean.TRUE) );
        }

        // include all groups unless this is not null and then we limit
        if (includeHierarchyGroups != null) {
            if (includeHierarchyGroups) {
                // only include those which were added via nodes
                search.addRestriction( new Restriction("nodeId", "", Restriction.NOT_NULL) );
            } else {
                // only include those which were added directly (i.e. nodeId = null)
                search.addRestriction( new Restriction("nodeId", "", Restriction.NULL) );
            }
        }

        // get all the groups for the given eval ids in one storage call
        search.addOrder( new Order("id") );
        List<EvalAssignGroup> l = dao.findBySearch(EvalAssignGroup.class, search );

        for (int i=0; i<l.size(); i++) {
            EvalAssignGroup eac = l.get(i);

            // put stuff in inner list
            Long evalId = eac.getEvaluation().getId();
            List<EvalAssignGroup> innerList = evals.get(evalId);
            innerList.add( eac );
        }
        return evals;
    }


    public Map<Long, List<EvalGroup>> getEvalGroupsForEval(Long[] evaluationIds,
            boolean includeUnApproved, Boolean includeHierarchyGroups) {
        log.debug("evalIds: " + evaluationIds + ", includeUnApproved=" + includeUnApproved);
        Map<Long, List<EvalGroup>> evals = new TreeMap<Long, List<EvalGroup>>();

        Map<Long, List<EvalAssignGroup>> evalAGs = 
            getAssignGroupsForEvals(evaluationIds, includeUnApproved, includeHierarchyGroups);

        // replace each assign group with an EvalGroup
        for (Iterator<Long> iter = evalAGs.keySet().iterator(); iter.hasNext();) {
            Long evalId = iter.next();
            List<EvalAssignGroup> innerList = evalAGs.get(evalId);
            List<EvalGroup> newList = new ArrayList<EvalGroup>();
            for (int i=0; i<innerList.size(); i++) {
                EvalAssignGroup eag = innerList.get(i);
                newList.add( commonLogic.makeEvalGroupObject( eag.getEvalGroupId() ) );
            }
            evals.put(evalId, newList);
        }
        return evals;
    }


    public boolean canCreateAssignEval(String userId, Long evaluationId) {
        log.debug("userId: " + userId + ", evaluationId: " + evaluationId);
        boolean allowed = false;

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        try {
            allowed = securityChecks.checkCreateAssignments(userId, eval);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    public boolean canDeleteAssignGroup(String userId, Long assignGroupId) {
        log.debug("userId: " + userId + ", assignGroupId: " + assignGroupId);
        boolean allowed = false;

        EvalAssignGroup assignGroup = getAssignGroupById(assignGroupId);
        if (assignGroup == null) {
            throw new IllegalArgumentException("Cannot find assign evalGroupId with this id: " + assignGroupId);
        }

        EvalEvaluation eval = getEvaluationOrFail(assignGroup.getEvaluation().getId());

        try {
            allowed = securityChecks.checkRemoveAssignments(userId, assignGroup, eval);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    // RESPONSES

    public EvalResponse getResponseById(Long responseId) {
        log.debug("responseId: " + responseId);
        EvalResponse response = (EvalResponse) dao.findById(EvalResponse.class, responseId);
        return response;
    }


    public EvalResponse getResponseForUserAndGroup(Long evaluationId, String userId, String evalGroupId) {
        if (! checkEvaluationExists(evaluationId) ) {
            throw new IllegalArgumentException("Invalid evaluation id, cannot find evaluation: " + evaluationId);
        }

        EvalResponse response = null;
        List<EvalResponse> responses = dao.findBySearch(EvalResponse.class, new Search(
                new Restriction[] {
                        new Restriction("owner", userId),
                        new Restriction("evaluation.id", evaluationId),
                        new Restriction("evalGroupId", evalGroupId)
                }) );
        if (responses.size() <= 0) {
            // do nothing, no response was found
        } else if (responses.size() == 1) {
            response = responses.get(0);
        } else {
            throw new IllegalStateException("Invalid responses state, this user ("+userId+") has more than 1 response " +
                    "for evaluation ("+evaluationId+") and evalGroupId ("+evalGroupId+")");
        }
        return response;
    }


    public List<Long> getResponseIds(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        log.debug("evaluationId: " + evaluationId);

        if (dao.countBySearch(EvalEvaluation.class, new Search("id", evaluationId)) <= 0l) {
            throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
        }

        // pass through to the dao method
        List<Long> rids = dao.getResponseIds(evaluationId, evalGroupIds, null, completed);
        return rids;
    }

    public List<EvalResponse> getResponses(String userId, Long[] evaluationIds,
            String[] evalGroupIds, Boolean completed) {

        Search search = new Search();

        makeResponsesSearchParams(userId, evaluationIds, evalGroupIds, completed, search);

        List<EvalResponse> responses = dao.findBySearch(EvalResponse.class, search);
        return responses;
    }


    public int countResponses(String userId, Long[] evaluationIds, String[] evalGroupIds,
            Boolean completed) {

        Search search = new Search();

        makeResponsesSearchParams(userId, evaluationIds, evalGroupIds, completed, search);

        int count = (int) dao.countBySearch(EvalResponse.class, search);
        return count;
    }

    /**
     * Setup the responses search parameters,
     * this is here to reduce code duplication
     * @param userId
     * @param evaluationIds
     * @param evalGroupIds
     * @param completed
     * @param props
     * @param values
     * @param comparisons
     */
    private void makeResponsesSearchParams(String userId, Long[] evaluationIds, String[] evalGroupIds, Boolean completed, 
            Search search) {
        if (evaluationIds == null || evaluationIds.length == 0) {
            throw new IllegalArgumentException("evaluationIds cannot be null or empty");
        }

        // basic search params
        search.addRestriction( new Restriction("evaluation.id", evaluationIds) );

        if (userId != null && userId.length() > 0) {
            // admin can see all responses
            if (! commonLogic.isUserAdmin(userId) ) {
                search.addRestriction( new Restriction("owner", userId) );
            }
        }

        if (evalGroupIds != null && evalGroupIds.length > 0) {
            search.addRestriction( new Restriction("evalGroupId", evalGroupIds) );
        }

        if (completed != null) {
            // if endTime is null then the response is incomplete, if not null then it is complete
            search.addRestriction( new Restriction("endTime", "", completed ? Restriction.NOT_NULL : Restriction.NULL) );
        }
    }


    public boolean canModifyResponse(String userId, Long responseId) {
        log.debug("userId: " + userId + ", responseId: " + responseId);
        // get the response by id
        EvalResponse response = getResponseById(responseId);
        if (response == null) {
            throw new IllegalArgumentException("Cannot find response with id: " + responseId);
        }

        EvalEvaluation eval = getEvaluationOrFail(response.getEvaluation().getId());

        // valid state, check perms and locked
        try {
            return securityChecks.checkUserModifyResponse(userId, response, eval);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;
    }


    // EMAIL TEMPLATES

    public List<EvalEmailTemplate> getEmailTemplatesForUser(String userId, String emailTemplateTypeConstant, Boolean includeDefaultsOnly) {

        Search search = new Search();

        if (emailTemplateTypeConstant != null) {
            search.addRestriction( new Restriction("type", emailTemplateTypeConstant) );
        }

        // admin can see all
        if (! commonLogic.isUserAdmin(userId) ) {
            search.addRestriction( new Restriction("owner", userId) );
        }

        if (includeDefaultsOnly != null) {
            search.addRestriction( new Restriction("defaultType", "", includeDefaultsOnly ? Restriction.NOT_NULL : Restriction.NULL) );
        }

        List<EvalEmailTemplate> templates = dao.findBySearch(EvalEmailTemplate.class, search);
        return templates;
    }

    public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateTypeConstant) {
        log.debug("emailTemplateTypeConstant: " + emailTemplateTypeConstant);

        if (emailTemplateTypeConstant == null) {
            throw new IllegalArgumentException("Invalid emailTemplateTypeConstant, cannot be null");
        }

        // fetch template by type
        List<EvalEmailTemplate> l = dao.findBySearch(EvalEmailTemplate.class, new Search("defaultType", emailTemplateTypeConstant) );
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Could not find any default template for type constant: "
                    + emailTemplateTypeConstant);
        }
        return (EvalEmailTemplate) l.get(0);
    }

    public EvalEmailTemplate getEmailTemplate(Long evaluationId, String emailTemplateTypeConstant) {
        // get evaluation
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        // check the type constant
        Long emailTemplateId = null;
        if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
            if (eval.getAvailableEmailTemplate() != null) {
                emailTemplateId = eval.getAvailableEmailTemplate().getId();
            }
        } else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
            if (eval.getReminderEmailTemplate() != null) {
                emailTemplateId = eval.getReminderEmailTemplate().getId();
            }
        } else {
            throw new IllegalArgumentException("Invalid emailTemplateTypeConstant: " + emailTemplateTypeConstant);
        }

        EvalEmailTemplate emailTemplate = null;
        if (emailTemplateId != null) {
            emailTemplate = (EvalEmailTemplate) dao.findById(EvalEmailTemplate.class, emailTemplateId);
        }

        if (emailTemplate == null || emailTemplate.getMessage() == null) {
            emailTemplate = getDefaultEmailTemplate(emailTemplateTypeConstant);
        }
        return emailTemplate;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEmailTemplate(java.lang.Long)
     */
    public EvalEmailTemplate getEmailTemplate(Long emailTemplateId) {
        EvalEmailTemplate emailTemplate = (EvalEmailTemplate) dao.findById(EvalEmailTemplate.class, emailTemplateId);
        return emailTemplate;
    }

    // PERMISSIONS

    public boolean canControlEmailTemplate(String userId, Long evaluationId, String emailTemplateTypeConstant) {
        log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateTypeConstant: "
                + emailTemplateTypeConstant);

        // get evaluation
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);

        // get the email template
        EvalEmailTemplate emailTemplate = getEmailTemplate(evaluationId, emailTemplateTypeConstant);

        // check the permissions and state
        try {
            return securityChecks.checkEvalTemplateControl(userId, eval, emailTemplate);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;
    }

    public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId) {
        log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateId: " + emailTemplateId);

        // get the email template
        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(emailTemplateId);

        // get evaluation
        EvalEvaluation eval = null;
        if (evaluationId != null) {
            eval = getEvaluationOrFail(evaluationId);

            // make sure this template is associated with this evaluation
            if (eval.getAvailableEmailTemplate() != null
                    && emailTemplate.getId().equals(eval.getAvailableEmailTemplate().getId())) {
                log.debug("template matches available template from eval (" + eval.getId() + ")");
            } else if (eval.getReminderEmailTemplate() != null
                    && emailTemplate.getId().equals(eval.getReminderEmailTemplate().getId())) {
                log.debug("template matches reminder template from eval (" + eval.getId() + ")");
            } else {
                throw new IllegalArgumentException("email template (" + emailTemplate.getId()
                        + ") does not match any template from eval (" + eval.getId() + ")");
            }
        }

        // check the permissions and state
        try {
            return securityChecks.checkEvalTemplateControl(userId, eval, emailTemplate);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;
    }


    // PRIVATE METHODS


    /**
     * @param emailTemplateId
     * @return
     */
    private EvalEmailTemplate getEmailTemplateOrFail(Long emailTemplateId) {
        EvalEmailTemplate emailTemplate = (EvalEmailTemplate) dao.findById(EvalEmailTemplate.class,
                emailTemplateId);
        if (emailTemplate == null) {
            throw new IllegalArgumentException("Cannot find email template with this id: " + emailTemplateId);
        }
        return emailTemplate;
    }

    /**
     * Gets the evaluation or throws exception,
     * reduce code duplication
     * @param evaluationId
     * @return eval for this id
     * @throws IllegalArgumentException if no eval exists
     */
    private EvalEvaluation getEvaluationOrFail(Long evaluationId) {
        EvalEvaluation eval = getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Cannot find evaluation with id: " + evaluationId);
        }
        return eval;
    }

    private void fixupEvaluation(EvalEvaluation evaluation) {
        if (evaluation != null) {
            // add in any needed checks or change storage that is needed here
        }
    }

}
