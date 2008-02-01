/**
 * $Id: EvalEvaluationSetupServiceImpl.java 1000 Dec 25, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalEvaluationSetupServiceImpl.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.impl.interceptors.EvaluationModificationRegistry;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Implementation for EvalEvaluationSetupService
 * (Note for developers - do not modify this without permission from the author)<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationSetupServiceImpl implements EvalEvaluationSetupService {

   private static Log log = LogFactory.getLog(EvalEvaluationSetupServiceImpl.class);

   private final String EVENT_EVAL_CREATE = "eval.evaluation.created";
   private final String EVENT_EVAL_UPDATE = "eval.evaluation.updated";
   //private final String EVENT_EVAL_STATE_CHANGE = "eval.evaluation.state.change";
   private final String EVENT_EVAL_DELETE = "eval.evaluation.deleted";

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private EvalExternalLogic external;
   public void setExternalLogic(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalSecurityChecks securityChecks;
   public void setSecurityChecks(EvalSecurityChecks securityChecks) {
      this.securityChecks = securityChecks;
   }

   // potentially causing circular dependency
   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }


   // INIT method
   public void init() {
      log.debug("Init");
   }


   /**
    * Non-API method used from interceptor<br/>
    * Semantics - throws exception on forbidden operation<br/>
    * Do NOT use this method outside the logic layer or even outside the interceptor!
    * 
    * @param evaluation an evaluation persistent object
    * @param method a method being called on the evaluation persistent object
    */
   public void modifyEvaluation(EvalEvaluation evaluation, String method) {
      // we only care to do these checks when object is modified (set is called)
      if (method.startsWith("set")) {
         String state = EvalUtils.getEvaluationState(evaluation);        
         // strip off the "set" part and fix case at beginning to get the name of the property we are setting
         String property = Character.toLowerCase(method.charAt(3)) + method.substring(4);
         // check if this property can be changed while evaluation is in this state
         if (!EvaluationModificationRegistry.isPermittedModification(state, property)) {
            throw new IllegalArgumentException("Cannot change state of evaluation with " +
                  method + " when it is in state " + state);
         }

         // check the user control permissions
         // This check is more expensive than the others so we do those first -AZ
         String userId = external.getCurrentUserId();
         if (! securityChecks.canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }
      }
   }

   // EVALUATIONS

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#saveEvaluation(edu.vt.sakai.evaluation.model.EvalEvaluation, java.lang.String)
    */
   public void saveEvaluation(EvalEvaluation evaluation, String userId) {
      log.debug("evalId: " + evaluation.getId() + ",userId: " + userId);

      boolean newEvaluation = false;

      // set the date modified
      evaluation.setLastModified( new Date() );

      // test date ordering first
      if (evaluation.getStartDate().compareTo(evaluation.getDueDate()) >= 0 ) {
         throw new IllegalArgumentException(
               "due date (" + evaluation.getDueDate() +
               ") must occur after start date (" + 
               evaluation.getStartDate() + "), can occur on the same date but not at the same time");
      } else if (evaluation.getDueDate().compareTo(evaluation.getStopDate()) > 0 ) {
         throw new IllegalArgumentException(
               "stop date (" + evaluation.getStopDate() +
               ") must occur on or after due date (" + 
               evaluation.getDueDate() + "), can be identical");
      } else if (evaluation.getViewDate().compareTo(evaluation.getStopDate()) <= 0 ) {
         throw new IllegalArgumentException(
               "view date (" + evaluation.getViewDate() +
               ") must occur after stop date (" + 
               evaluation.getStopDate() + "), can occur on the same date but not at the same time");
      }

      // now perform checks depending on whether this is new or existing
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.add(Calendar.MINUTE, -15); // put today a bit in the past (15 minutes)
      Date today = calendar.getTime();
      if (evaluation.getId() == null) { // creating new evaluation

         newEvaluation = true;

         // test if new evaluation occurs in the past
         if (evaluation.getStartDate().before(today)) {
            throw new IllegalArgumentException(
                  "start date (" + evaluation.getStartDate() +
            ") cannot occur in the past for new evaluations");
         } else if (evaluation.getDueDate().before(today)) {
            throw new IllegalArgumentException(
                  "due date (" + evaluation.getDueDate() +
            ") cannot occur in the past for new evaluations");
         } else if (evaluation.getStopDate().before(today)) {
            throw new IllegalArgumentException(
                  "stop date (" + evaluation.getStopDate() +
            ") cannot occur in the past for new evaluations");
         }

         // make sure the state is set correctly
         evaluationService.returnAndFixEvalState(evaluation, false);

         // check user permissions (uses public method)
         if (! canBeginEvaluation(userId) ) {
            throw new SecurityException("User ("+userId+") attempted to create evaluation without permissions");
         }

      } else { // updating existing evaluation

         // make sure the state is set correctly
         evaluationService.returnAndFixEvalState(evaluation, false);

         if (! securityChecks.canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }

         // All other checks have been moved to interceptor
      }

      // make sure we are not using a blank template here
      if (evaluation.getTemplate() == null ||
            evaluation.getTemplate().getTemplateItems() == null ||
            evaluation.getTemplate().getTemplateItems().size() <= 0) {
         throw new IllegalArgumentException("Evaluations must include a template and the template must have at least one item in it");
      } else if (! EvalConstants.TEMPLATE_TYPE_STANDARD.equals(evaluation.getTemplate().getType())) {
         throw new IllegalArgumentException("Evaluations cannot use templates of type: " + evaluation.getTemplate().getType() + 
         " as the primary template");			
      }

      // check the added template for type
      if (evaluation.getAddedTemplate() != null &&
            ! EvalConstants.TEMPLATE_TYPE_ADDED.equals(evaluation.getTemplate().getType()) ) {
         throw new IllegalArgumentException("Evaluations cannot use templates of type: " + evaluation.getTemplate().getType() + 
               " as the added template, must be " + EvalConstants.TEMPLATE_TYPE_ADDED);			
      }

      // fill in any default values and nulls here
      if (evaluation.getLocked() == null) {
         evaluation.setLocked( Boolean.FALSE );
      }
      if (evaluation.getResultsPrivate() == null) {
         evaluation.setResultsPrivate( Boolean.FALSE );
      }
      if (evaluation.getAuthControl() == null) {
         evaluation.setAuthControl( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ );
      }

      if (evaluation.getEvalCategory() != null && evaluation.getEvalCategory().length() <= 0) {
         evaluation.setEvalCategory(null);
      }

      // system setting checks for things like allowing users to modify responses
      Boolean systemModifyResponses = (Boolean) settings.get( EvalSettings.STUDENT_MODIFY_RESPONSES );
      if ( systemModifyResponses == null ) {
         if ( evaluation.getModifyResponsesAllowed() == null ) {
            evaluation.setModifyResponsesAllowed( Boolean.FALSE );
         }
      } else {
         evaluation.setBlankResponsesAllowed( systemModifyResponses );
      }
      Boolean systemBlankResponses = (Boolean) settings.get( EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED );
      if ( systemBlankResponses == null ) {
         if (evaluation.getBlankResponsesAllowed() == null) {
            evaluation.setBlankResponsesAllowed( Boolean.FALSE );
         }
      } else {
         evaluation.setBlankResponsesAllowed( systemBlankResponses );
      }
      String systemInstructorOpt = (String) settings.get( EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE );
      if ( systemInstructorOpt == null ) {
         if (evaluation.getInstructorOpt() == null) {
            evaluation.setInstructorOpt( EvalConstants.INSTRUCTOR_OPT_OUT );
         }
      } else {
         evaluation.setInstructorOpt( systemInstructorOpt );
      }

      dao.save(evaluation);
      log.info("User ("+userId+") saved evaluation ("+evaluation.getId()+"), title: " + evaluation.getTitle());

      if (newEvaluation) {
         external.registerEntityEvent(EVENT_EVAL_CREATE, evaluation);
         // call logic to manage Quartz scheduled jobs
         evalJobLogic.processEvaluationStateChange(evaluation.getId(), EvalJobLogic.ACTION_CREATE);
      } else {
         external.registerEntityEvent(EVENT_EVAL_UPDATE, evaluation);
         // call logic to manage Quartz scheduled jobs
         evalJobLogic.processEvaluationStateChange(evaluation.getId(), EvalJobLogic.ACTION_UPDATE);
      }

      // effectively we are locking the evaluation when a user replies to it, otherwise the chain can be changed
      if (evaluation.getLocked().booleanValue()) {
         // lock evaluation and associated template
         log.info("Locking evaluation ("+evaluation.getId()+") and associated template ("+evaluation.getTemplate().getId()+")");
         dao.lockEvaluation(evaluation);			
      }

   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#deleteEvaluation(java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public void deleteEvaluation(Long evaluationId, String userId) {
      log.debug("evalId: " + evaluationId + ",userId: " + userId);
      
      EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
      if (evaluation == null) {
         log.warn("Cannot find evaluation to delete with id: " + evaluationId);
         return;
      }

      if ( securityChecks.canUserRemoveEval(userId, evaluation) ) {

         Set[] entitySets = new HashSet[4];
         // remove associated AssignGroups
         List<EvalAssignGroup> acs = dao.findByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignGroup> assignGroupSet = new HashSet<EvalAssignGroup>(acs);
         entitySets[0] = assignGroupSet;

         // remove associated assigned hierarchy nodes
         List<EvalAssignHierarchy> ahs = dao.findByProperties(EvalAssignHierarchy.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignHierarchy> assignHierSet = new HashSet<EvalAssignHierarchy>(ahs);
         entitySets[1] = assignHierSet;

         // remove associated unused email templates
         Set<EvalEmailTemplate> emailSet = new HashSet<EvalEmailTemplate>();
         entitySets[2] = emailSet;
         if (evaluation.getAvailableEmailTemplate() != null) {
            if (evaluation.getAvailableEmailTemplate().getDefaultType() == null) {
               // only remove non-default templates
               Long emailTemplateId = evaluation.getAvailableEmailTemplate().getId();
               if ( dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"availableEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getAvailableEmailTemplate() );
               }
            }
         }
         if (evaluation.getReminderEmailTemplate() != null) {
            if (evaluation.getReminderEmailTemplate().getDefaultType() == null) {
               Long emailTemplateId = evaluation.getReminderEmailTemplate().getId();
               if ( dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"reminderEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getReminderEmailTemplate() );
               }
            }
         }

         // add eval to a set to be removed
         Set evalSet = new HashSet();
         entitySets[3] = evalSet;
         evalSet.add(evaluation);

         // unlock associated template
         log.info("Unlocking associated template ("+evaluation.getTemplate().getId()+") for eval ("+evaluation.getId()+")");
         dao.lockTemplate(evaluation.getTemplate(), Boolean.FALSE);

         // fire the evaluation deleted event
         external.registerEntityEvent(EVENT_EVAL_DELETE, evaluation);

         // remove the evaluation and related data in one transaction
         dao.deleteMixedSet(entitySets);
         //dao.delete(eval);

         // remove any remaining scheduled jobs
         evalJobLogic.processEvaluationStateChange(evaluationId, EvalJobLogic.ACTION_DELETE);

         log.info("User ("+userId+") removed evaluation ("+evaluationId+"), title: " + evaluation.getTitle());
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete evaluation ("+evaluationId+")");
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvaluationByEid(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public EvalEvaluation getEvaluationByEid(String eid) {
      return evaluationService.getEvaluationByEid(eid);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationById(java.lang.Long)
    */
   public EvalEvaluation getEvaluationById(Long evaluationId) {
      return evaluationService.getEvaluationById(evaluationId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvaluationsByTemplateId(java.lang.Long)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
      return evaluationService.getEvaluationsByTemplateId(templateId);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#countEvaluationsByTemplateId(java.lang.Long)
    */
   public int countEvaluationsByTemplateId(Long templateId) {
      return evaluationService.countEvaluationsByTemplateId(templateId);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getVisibleEvaluationsForUser(java.lang.String, boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned) {
      List<EvalEvaluation> l = new ArrayList<EvalEvaluation>();
      if (recentOnly) {
         // only get recently closed evals 
         // check system setting to get "recent" value
         Integer recentlyClosedDays = (Integer) settings.get(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS);
         if (recentlyClosedDays == null) { recentlyClosedDays = 10; }
         Calendar calendar = GregorianCalendar.getInstance();
         calendar.add(Calendar.DATE, -1 * recentlyClosedDays.intValue());
         Date recent = calendar.getTime();

         if (external.isUserAdmin(userId)) {
            l = dao.findByProperties(EvalEvaluation.class,
                  new String[] {"stopDate"}, 
                  new Object[] {recent}, 
                  new int[] {EvaluationDao.GREATER});
         } else {

            // Get the owned + not-owned evaluations i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, recent);
            }
            // Get the evaluations owned by the user
            else {
               l = dao.findByProperties(EvalEvaluation.class,
                     new String[] {"owner", "stopDate"}, 
                     new Object[] {userId, recent}, 
                     new int[] {EvaluationDao.EQUALS, EvaluationDao.GREATER});
            }
         }
      } else {
         // don't worry about when they closed
         if (external.isUserAdmin(userId)) {
            // NOTE: this will probably be too slow -AZ
            l = dao.findAll(EvalEvaluation.class);
         } else {

            // Get the owned + not-owned evaluations i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, null);
            }
            // get all evaluations created (owned) by this user
            else {
               l = dao.findByProperties(EvalEvaluation.class,
                     new String[] {"owner"}, new Object[] {userId});
            }
         }
      }
      return l;
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationsForUser(java.lang.String, boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsForUser(String userId, boolean activeOnly, boolean untakenOnly) {
      List<EvalGroup> takeGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

      String[] evalGroupIds = new String[takeGroups.size()];
      for (int i=0; i<takeGroups.size(); i++) {
         EvalGroup c = (EvalGroup) takeGroups.get(i);
         evalGroupIds[i] = c.evalGroupId;
      }

      // get the evaluations
      List<EvalEvaluation> evals = dao.getEvaluationsByEvalGroups( evalGroupIds, activeOnly, false, true );

      if (untakenOnly) {
         // filter out the evaluations this user already took

         // create an array of the evaluation ids
         Long[] evalIds = new Long[evals.size()];
         for (int j = 0; j < evals.size(); j++) {
            evalIds[j] = evals.get(j).getId();
         }

         // now get the responses for all the returned evals
         List<EvalResponse> l = dao.findByProperties(EvalResponse.class, 
               new String[] {"owner", "evaluation.id"}, 
               new Object[] {userId, evalIds});

         // Iterate through and remove the evals this user already took
         for (int i = 0; i < l.size(); i++) {
            Long evalIdTaken = l.get(i).getEvaluation().getId();
            for (int j = 0; j < evals.size(); j++) {
               if (evalIdTaken.equals(evals.get(j).getId())) {
                  evals.remove(j);
               }
            }
         }
      }

      return evals;
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#countEvaluationContexts(java.lang.Long)
    */
   public int countEvaluationGroups(Long evaluationId) {
      return evaluationService.countEvaluationGroups(evaluationId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvaluationAssignGroups(java.lang.Long[], boolean)
    */
   @SuppressWarnings("unchecked")
   public Map<Long, List<EvalAssignGroup>> getEvaluationAssignGroups(Long[] evaluationIds, boolean includeUnApproved) {
      return evaluationService.getEvaluationAssignGroups(evaluationIds, includeUnApproved);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvaluationGroups(java.lang.Long[], boolean)
    */
   public Map<Long, List<EvalGroup>> getEvaluationGroups(Long[] evaluationIds, boolean includeUnApproved) {
      return evaluationService.getEvaluationGroups(evaluationIds, includeUnApproved);
   }

   // PERMISSIONS

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canBeginEvaluation(java.lang.String)
    */
   public boolean canBeginEvaluation(String userId) {
      return evaluationService.canBeginEvaluation(userId);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationState(java.lang.Long)
    */
   public String updateEvaluationState(Long evaluationId) {
      return evaluationService.updateEvaluationState(evaluationId);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canRemoveEvaluation(java.lang.String, java.lang.Long)
    */
   public boolean canRemoveEvaluation(String userId, Long evaluationId) {
      return evaluationService.canRemoveEvaluation(userId, evaluationId);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canTakeEvaluation(java.lang.String, java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public boolean canTakeEvaluation(String userId, Long evaluationId, String evalGroupId) {
      return evaluationService.canTakeEvaluation(userId, evaluationId, evalGroupId);
   }


   // CATEGORIES

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvalCategories(java.lang.String)
    */
   public String[] getEvalCategories(String userId) {
      log.debug("userId: " + userId );

      // return all current categories or only return categories created by this user if not null
      List<String> l = dao.getEvalCategories(userId);
      return (String[]) l.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#getEvaluationsByCategory(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory, String userId) {
      log.debug("evalCategory: " + evalCategory + ", userId: " + userId );

      if (evalCategory == null || evalCategory.equals("")) {
         throw new IllegalArgumentException("evalCategory cannot be blank or null");
      }

      List<EvalEvaluation> evals = new ArrayList<EvalEvaluation>();
      if (userId == null) {
         // get all evals for a category
         evals = dao.findByProperties(EvalEvaluation.class,
               new String[] {"evalCategory"}, 
               new Object[] {evalCategory}, 
               new int[] {EvaluationDao.EQUALS},
               new String[] {"startDate"});
      } else {
         // get all evals for a specific user for a category
         List takeGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

         String[] evalGroupIds = new String[takeGroups.size()];
         for (int i=0; i<takeGroups.size(); i++) {
            EvalGroup c = (EvalGroup) takeGroups.get(i);
            evalGroupIds[i] = c.evalGroupId;
         }

         // this sucks for efficiency -AZ
         List<EvalEvaluation> l = dao.getEvaluationsByEvalGroups( evalGroupIds, true, false, true ); // only get active for users
         for (EvalEvaluation evaluation : l) {
            if ( evalCategory.equals(evaluation.getEvalCategory()) ) {
               evals.add(evaluation);
            }
         }
      }
      return evals;
   }


   // INTERNAL METHODS


   /**
    * Fixes the state of an evaluation (if needed) and saves it,
    * will not save state for a new evaluation
    * 
    * @param eval
    * @param saveState if true, save the fixed eval state, else do not save
    * @return the state constant string
    */
   protected String fixReturnEvalState(EvalEvaluation evaluation, boolean saveState) {
      return evaluationService.returnAndFixEvalState(evaluation, saveState);
   }


   /**
    * Get both owned and not-owned evaluation for the given user.
    * 
    * @param userId the internal user id (not username).
    * @param recentOnly if true return recently closed evaluations only
    * (still returns all active and in queue evaluations), if false return all closed evaluations.
    * @param evalsToReturn list of owned and not-owned evaluations. 
    * @param recent date for comparison when looking for recently closed evaluations.
    */
   @SuppressWarnings("unchecked")
   private void getEvalsWhereBeEvaluated(String userId, boolean recentOnly, List<EvalEvaluation> evalsToReturn, Date recent) {

      // Get the list of EvalGroup where user has "eval.be.evaluated" permission.
      List<EvalGroup> evaluatedGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED);
      if (evaluatedGroups.size() > 0) {

         String[] evalGroupsIds = new String[evaluatedGroups.size()];
         for (int i = 0; i < evaluatedGroups.size(); i++) {
            EvalGroup c = (EvalGroup) evaluatedGroups.get(i);
            evalGroupsIds [i] = c.evalGroupId;
         }

         // Using the list of EvalGroups, get the corresponding list of EvalAssignGroup.
         List<EvalAssignGroup> assignGroupList = dao.findByProperties(EvalAssignGroup.class,
               new String[] {"evalGroupId"}, 
               new Object[] {evalGroupsIds}, 
               new int[] {EvaluationDao.EQUALS});

         // Iterate through list of EvalAssignGroup and get the EvalEvaluation.
         for (int i = 0; i < assignGroupList.size(); i++) {

            EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroupList.get(i);
            EvalEvaluation eval = assignGroup.getEvaluation();

            /*
             * If only recent evaluations have to be fetched, then check for
             * stop date else just add to the existing list of evaluations.
             */ 
            if (recentOnly) {
               if ((eval.getStopDate()).after(recent)) {
                  evalsToReturn.add(eval);
               } else {
                  // Do nothing
               }
            } else {
               evalsToReturn.add(eval);
            }

         } // end of for
      } // end of if
   } // end of method

}
