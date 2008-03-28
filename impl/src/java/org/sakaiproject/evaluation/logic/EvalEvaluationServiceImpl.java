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
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


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

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
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
      int count = dao.countByProperties(EvalEvaluation.class, 
            new String[] {"id"}, new Object[] {evaluationId});
      if (count > 0) exists = true;
      return exists;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluationByEid(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public EvalEvaluation getEvaluationByEid(String eid) {
      List<EvalEvaluation> evalEvaluations = new ArrayList<EvalEvaluation>();
      EvalEvaluation evalEvaluation = null;
      if(eid != null) {
         evalEvaluations = dao.findByProperties(EvalEvaluation.class, new String[] {"eid"}, new Object[] {eid});
         if(!evalEvaluations.isEmpty())
            evalEvaluation = (EvalEvaluation)evalEvaluations.get(0);
      }
      fixupEvaluation(evalEvaluation);
      return evalEvaluation;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#countEvaluationsByTemplateId(java.lang.Long)
    */
   public int countEvaluationsByTemplateId(Long templateId) {
      log.debug("templateId: " + templateId);
      int count = dao.countByProperties(EvalTemplate.class, new String[] {"id"}, new Object[] {templateId});
      if (count <= 0) {
         throw new IllegalArgumentException("Cannot find template with id: " + templateId);
      }
      count = dao.countByProperties(EvalEvaluation.class,
            new String[] {"template.id", "state", "state"}, 
            new Object[] {templateId, EvalConstants.EVALUATION_STATE_PARTIAL, EvalConstants.EVALUATION_STATE_DELETED},
            new int[] {EvaluationDao.EQUALS, EvaluationDao.NOT_EQUALS, EvaluationDao.NOT_EQUALS});
      return count;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluationsByTemplateId(java.lang.Long)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
      log.debug("templateId: " + templateId);
      int count = dao.countByProperties(EvalTemplate.class, 
            new String[] {"id"}, new Object[] {templateId});
      if (count <= 0) {
         throw new IllegalArgumentException("Cannot find template with id: " + templateId);
      }
      List<EvalEvaluation> evals = dao.findByProperties(EvalEvaluation.class,
            new String[] {"template.id", "state", "state"}, 
            new Object[] {templateId, EvalConstants.EVALUATION_STATE_PARTIAL, EvalConstants.EVALUATION_STATE_DELETED},
            new int[] {EvaluationDao.EQUALS, EvaluationDao.NOT_EQUALS, EvaluationDao.NOT_EQUALS});
      for (EvalEvaluation evaluation : evals) {
         fixupEvaluation(evaluation);
      }
      return evals;
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
                  externalLogic.registerEntityEvent(EVENT_EVAL_STATE_START, evaluation);
               } else if ( EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(trueState) ) {
                  externalLogic.registerEntityEvent(EVENT_EVAL_STATE_DUE, evaluation);
               } else if ( EvalConstants.EVALUATION_STATE_CLOSED.equals(trueState) ) {
                  externalLogic.registerEntityEvent(EVENT_EVAL_STATE_STOP, evaluation);
               } else if ( EvalConstants.EVALUATION_STATE_VIEWABLE.equals(trueState) ) {
                  externalLogic.registerEntityEvent(EVENT_EVAL_STATE_VIEWABLE, evaluation);
               }
               dao.update(evaluation);
            }
         }
      }
      return trueState;
   }

   public Set<String> getUserIdsTakingEvalInGroup(Long evaluationId, String evalGroupId,
         String includeConstant) {
      EvalUtils.validateEmailIncludeConstant(includeConstant);
      Set<String> userIds = null;
      if (EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant)) {
         // get all users who have NOT responded
         userIds = externalLogic.getUserIdsForEvalGroup(evalGroupId,
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
         userIds = externalLogic.getUserIdsForEvalGroup(evalGroupId,
               EvalConstants.PERM_TAKE_EVALUATION);
      }
      if (userIds == null) {
         userIds = new HashSet<String>();
      }
      return userIds;
   }

   public int countParticipantsForEval(Long evaluationId) {
      int totalEnrollments = 0;
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      if (! EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
         // only counting if the eval is not anonymous, anon is always 0 enrollments effectively
         Map<Long, List<EvalAssignGroup>> evalAssignGroups = getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
         List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
         for (int i=0; i<groups.size(); i++) {
            EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
            String evalGroupId = eac.getEvalGroupId();
            int enrollmentCount = externalLogic.countUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
            totalEnrollments = totalEnrollments + enrollmentCount;
         }
      }
      return totalEnrollments;
   }


   // PERMISSIONS

   public boolean canBeginEvaluation(String userId) {
      log.debug("Checking begin eval for: " + userId);
      boolean isAdmin = externalLogic.isUserAdmin(userId);
      if ( isAdmin && (dao.countAll(EvalTemplate.class) > 0) ) {
         // admin can access all templates and create an evaluation if 
         // there is at least one template
         return true;
      }
      Boolean instructorAllowedCreateEvals = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS);
      if (instructorAllowedCreateEvals != null && instructorAllowedCreateEvals) {
         if ( externalLogic.countEvalGroupsForUser(userId, EvalConstants.PERM_ASSIGN_EVALUATION) > 0 ) {
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

   @SuppressWarnings("unchecked")
   public boolean canTakeEvaluation(String userId, Long evaluationId, String evalGroupId) {
      log.debug("evalId: " + evaluationId + ", userId: " + userId + ", evalGroupId: " + evalGroupId);

      // grab the evaluation itself first
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);

      // check the evaluation state
      String state = EvalUtils.getEvaluationState(eval, false);
      if ( ! EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) &&
            ! EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(state) ) {
         log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") when eval state is: " + state);
         return false;
      }

      if (evalGroupId != null) {
         // check that the evalGroupId is valid for this evaluation
         List<EvalAssignGroup> ags = dao.findByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id", "evalGroupId"}, 
               new Object[] {evaluationId, evalGroupId});
         if (ags.size() <= 0) {
            log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") in this evalGroupId (" + evalGroupId + "), not assigned");
            return false;
         } else {
            // make sure instructor approval is true
            EvalAssignGroup eag = ags.get(0);
            if (! eag.getInstructorApproval().booleanValue() ) {
               log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") in this evalGroupId (" + evalGroupId + "), instructor has not approved");
               return false;
            }
         }
      } else {
         // no groupId is supplied so do a simpler check
         log.info("No evalGroupId supplied, doing abbreviated check for evalId=" + evaluationId + ", userId=" + userId);
         if ( externalLogic.isUserAdmin(userId) ) {
            // admin trumps being in a group
            log.info("ADMIN take eval permission override: User (" + userId + "), evaluation (" + evaluationId + "), evalGroupId (" + evalGroupId + ")");
            return true;
         }

         // make sure at least one group is valid for this eval
         int count = dao.countByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id", "instructorApproval"}, 
               new Object[] {evaluationId, Boolean.TRUE});
         if (count <= 0) {
            // no valid groups
            log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") in this evalGroupId (" + evalGroupId + "), there are no assigned groups which have been instructor approved");
            return false;
         }
      }

      if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl()) ) {
         // if this is anonymous then group membership does not matter
         return true;
      } else if ( EvalConstants.EVALUATION_AUTHCONTROL_KEY.equals(eval.getAuthControl()) ) {
         // if this uses a key then only the key matters
         // TODO add key check
         return false;
      } else if ( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ.equals(eval.getAuthControl()) ) {
         if (evalGroupId == null) {
            if (externalLogic.isUserAdmin(userId) ) {
               // short circuit the attempt to lookup every group in the system for the admin
               return true;
            }

            // if no groupId is supplied then simply check to see if the user is in any of the groups assigned,
            // hopefully this is faster than checking if the user has the right permission in every group -AZ
            List<EvalGroup> userEvalGroups = externalLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);
            if (userEvalGroups.size() > 0) {
               // only try to do this check if there is at least one userEvalGroup
               String[] evalGroupIds = new String[userEvalGroups.size()];
               for (int i=0; i<userEvalGroups.size(); i++) {
                  EvalGroup group = (EvalGroup) userEvalGroups.get(i);
                  evalGroupIds[i] = group.evalGroupId;
               }

               int count = dao.countByProperties(EvalAssignGroup.class, 
                     new String[] {"evaluation.id", "instructorApproval", "evalGroupId"}, 
                     new Object[] {evaluationId, Boolean.TRUE, evalGroupIds});
               if (count > 0) {
                  // ok if at least one group is approved and in the set of groups this user can take evals in for this eval id
                  return true;
               }
            }
         } else {
            // check the user permissions
            if ( externalLogic.isUserAdmin(userId) ) {
               // need to short circuit the checks below because the count is not the real count when does as an admin
               return true;
            } else {
               if (! externalLogic.isUserAllowedInEvalGroup(userId, EvalConstants.PERM_TAKE_EVALUATION, evalGroupId) ) {
                  log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") without permission");
                  return false;
               }
            }

            // check if the user already took this evaluation (count completed responses)
            int evalResponsesForUser = countResponses(userId, new Long[] {evaluationId}, new String[] {evalGroupId}, true);
            if (evalResponsesForUser > 0) {
               // user already has a response saved for this evaluation and evalGroupId
               if (eval.getModifyResponsesAllowed() == null || 
                     eval.getModifyResponsesAllowed().booleanValue() == false) {
                  // user cannot modify existing responses
                  EvalResponse response = getResponseForUserAndGroup(evaluationId, userId, evalGroupId);
                  if (response == null) response = new EvalResponse(); // avoid a null pointer exception
                  log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") again " +
                        "in this evalGroupId (" + evalGroupId + "), " +
                        "completed response exists ("+response.getId()+") from " + response.getEndTime() +
                  " and this evaluation does not allow multiple attempts");
                  return false;
               }
            }
            return true;
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

   public int countEvaluationGroups(Long evaluationId) {
      log.debug("evalId: " + evaluationId);
      int count = dao.countByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, 
            new Object[] {evaluationId});
      return count;
   }


   @SuppressWarnings("unchecked")
   public EvalAssignGroup getAssignGroupByEid(String eid) {
      List<EvalAssignGroup> evalAssignGroups = new ArrayList<EvalAssignGroup>();
      EvalAssignGroup group = null;
      if (eid != null) {
         evalAssignGroups = dao.findByProperties(EvalAssignGroup.class, 
               new String[] { "eid" },
               new Object[] { eid });
         if (! evalAssignGroups.isEmpty()) {
            group = evalAssignGroups.get(0);
         }
      }
      return group;
   }


   public EvalAssignGroup getAssignGroupById(Long assignGroupId) {
      log.debug("assignGroupId: " + assignGroupId);
      EvalAssignGroup eag = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroupId);
      return eag;
   }


   @SuppressWarnings("unchecked")
   public Long getAssignGroupId(Long evaluationId, String evalGroupId) {
      log.debug("evaluationId: " + evaluationId + ", evalGroupId: " + evalGroupId);
      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id", "evalGroupId"}, 
            new Object[] {evaluationId, evalGroupId} );
      if (l.size() == 1) {
         EvalAssignGroup assignGroup = (EvalAssignGroup) l.get(0);
         return assignGroup.getId();
      }
      return null;
   }


   @SuppressWarnings("unchecked")
   public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId) {
      List<EvalAssignHierarchy> l = dao.findByProperties(EvalAssignHierarchy.class,
            new String[] { "evaluation.id", "nodeId" }, 
            new Object[] { evaluationId, "" },
            new int[] {ByPropsFinder.EQUALS, ByPropsFinder.NOT_NULL},
            new String[] {"id"} );
      return l;
   }


   public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId) {
      EvalAssignHierarchy eah = (EvalAssignHierarchy) dao.findById(EvalAssignHierarchy.class, assignHierarchyId);
      return eah;
   }


   @SuppressWarnings("unchecked")
   public Map<Long, List<EvalAssignGroup>> getAssignGroupsForEvals(Long[] evaluationIds,
         boolean includeUnApproved, Boolean includeHierarchyGroups) {
      log.debug("evalIds: " + evaluationIds + ", includeUnApproved=" + includeUnApproved);
      Map<Long, List<EvalAssignGroup>> evals = new TreeMap<Long, List<EvalAssignGroup>>();

      // create the inner lists
      for (int i=0; i<evaluationIds.length; i++) {
         List<EvalAssignGroup> innerList = new ArrayList<EvalAssignGroup>();
         evals.put(evaluationIds[i], innerList);
      }

      List<String> props = new ArrayList<String>();
      List<Object> values = new ArrayList<Object>();
      List<Number> comparisons = new ArrayList<Number>();

      // basic search params
      props.add("evaluation.id");
      values.add(evaluationIds);
      comparisons.add(ByPropsFinder.EQUALS);

      if (! includeUnApproved) {
         // only include those that are approved
         props.add("instructorApproval");
         values.add(Boolean.TRUE);
         comparisons.add(ByPropsFinder.EQUALS);
      }

      // include all groups unless this is not null and then we limit
      if (includeHierarchyGroups != null) {
         if (includeHierarchyGroups) {
            // only include those which were added via nodes
            props.add("nodeId");
            values.add("");
            comparisons.add(ByPropsFinder.NOT_NULL);
         } else {
            // only include those which were added directly (i.e. nodeId = null)
            props.add("nodeId");
            values.add("");
            comparisons.add(ByPropsFinder.NULL);            
         }
      }

      // get all the groups for the given eval ids in one storage call
      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class, 
            props.toArray(new String[props.size()]), 
            values.toArray(new Object[values.size()]), 
            ArrayUtils.listToIntArray(comparisons), 
            new String[] { "id" });

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
            newList.add( externalLogic.makeEvalGroupObject( eag.getEvalGroupId() ) );
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
         allowed = securityChecks.checkCreateAssignGroup(userId, eval);
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
         allowed = securityChecks.checkRemoveAssignGroup(userId, assignGroup, eval);
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


   @SuppressWarnings("unchecked")
   public EvalResponse getResponseForUserAndGroup(Long evaluationId, String userId,
         String evalGroupId) {
      if (! checkEvaluationExists(evaluationId) ) {
         throw new IllegalArgumentException("Invalid evaluation id, cannot find evaluation: " + evaluationId);
      }

      EvalResponse response = null;
      List<EvalResponse> responses = dao.findByProperties(EvalResponse.class, 
            new String[] { "owner", "evaluation.id", "evalGroupId" }, 
            new Object[] { userId, evaluationId, evalGroupId }
      );
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

      if (dao.countByProperties(EvalEvaluation.class, new String[] { "id" }, new Object[] { evaluationId }) <= 0) {
         throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
      }

      // pass through to the dao method
      List<Long> rids = dao.getResponseIds(evaluationId, evalGroupIds, null, completed);
      return rids;
   }

   @SuppressWarnings("unchecked")
   public List<EvalResponse> getResponses(String userId, Long[] evaluationIds,
         String[] evalGroupIds, Boolean completed) {

      List<String> props = new ArrayList<String>();
      List<Object> values = new ArrayList<Object>();
      List<Number> comparisons = new ArrayList<Number>();

      makeResponsesSearchParams(userId, evaluationIds, evalGroupIds, completed, props, values, comparisons);

      List<EvalResponse> responses = dao.findByProperties(EvalResponse.class, 
            props.toArray(new String[props.size()]), 
            values.toArray(new Object[values.size()]), 
            ArrayUtils.listToIntArray(comparisons)
      );
      return responses;
   }


   public int countResponses(String userId, Long[] evaluationIds, String[] evalGroupIds,
         Boolean completed) {

      List<String> props = new ArrayList<String>();
      List<Object> values = new ArrayList<Object>();
      List<Number> comparisons = new ArrayList<Number>();

      makeResponsesSearchParams(userId, evaluationIds, evalGroupIds, completed, props, values, comparisons);

      int count = dao.countByProperties(EvalResponse.class, 
            props.toArray(new String[props.size()]), 
            values.toArray(new Object[values.size()]), 
            ArrayUtils.listToIntArray(comparisons)
      );
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
         List<String> props, List<Object> values, List<Number> comparisons) {
      if (evaluationIds == null || evaluationIds.length == 0) {
         throw new IllegalArgumentException("evaluationIds cannot be null or empty");
      }

      // basic search params
      props.add("evaluation.id");
      values.add(evaluationIds);
      comparisons.add(ByPropsFinder.EQUALS);

      if (userId != null && userId.length() > 0) {
         // admin can see all responses
         if (! externalLogic.isUserAdmin(userId) ) {
            props.add("owner");
            values.add(userId);
            comparisons.add(ByPropsFinder.EQUALS);
         }
      }

      if (evalGroupIds != null && evalGroupIds.length > 0) {
         props.add("evalGroupId");
         values.add(evalGroupIds);
         comparisons.add(ByPropsFinder.EQUALS);
      }

      if (completed != null) {
         // if endTime is null then the response is incomplete, if not null then it is complete
         props.add("endTime");
         values.add(""); // just need a placeholder
         if (completed) {
            comparisons.add(ByPropsFinder.NOT_NULL);
         } else {
            comparisons.add(ByPropsFinder.NULL);            
         }
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

   @SuppressWarnings("unchecked")
   public List<EvalEmailTemplate> getEmailTemplatesForUser(String userId, String emailTemplateTypeConstant, Boolean includeDefaultsOnly) {

      List<String> props = new ArrayList<String>();
      List<Object> values = new ArrayList<Object>();
      List<Number> comparisons = new ArrayList<Number>();

      if (emailTemplateTypeConstant != null) {
         props.add("type");
         values.add(emailTemplateTypeConstant);
         comparisons.add(EvaluationDao.EQUALS);
      }

      // admin can see all
      if (! externalLogic.isUserAdmin(userId) ) {
         props.add("owner");
         values.add(userId);
         comparisons.add(ByPropsFinder.EQUALS);
      }

      if (includeDefaultsOnly != null) {
         props.add("defaultType");
         values.add("");
         if (includeDefaultsOnly) {
            comparisons.add(EvaluationDao.NOT_NULL);
         } else {
            comparisons.add(EvaluationDao.NULL);            
         }
      }

      List<EvalEmailTemplate> templates = dao.findByProperties(EvalEmailTemplate.class, 
            props.toArray(new String[props.size()]), 
            values.toArray(new Object[values.size()]), 
            ArrayUtils.listToIntArray(comparisons) );
      return templates;
   }

   @SuppressWarnings("unchecked")
   public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateTypeConstant) {
      log.debug("emailTemplateTypeConstant: " + emailTemplateTypeConstant);

      if (emailTemplateTypeConstant == null) {
         throw new IllegalArgumentException("Invalid emailTemplateTypeConstant, cannot be null");
      }

      // fetch template by type
      List<EvalEmailTemplate> l = dao.findByProperties(EvalEmailTemplate.class, 
            new String[] { "defaultType" },
            new Object[] { emailTemplateTypeConstant });
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
