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
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.ArrayUtils;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;

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

   private ExternalHierarchyLogic hierarchyLogic;
   public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
      this.hierarchyLogic = hierarchyLogic;
   }

   private EvalEmailsLogic emails;
   public void setEmails(EvalEmailsLogic emails) {
      this.emails = emails;
   }

   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }


   // INIT method
   public void init() {
      log.debug("Init");
      // run a timer which ensures that evaluation states are kept up to date
      //initiateUpdateStateTimer();
   }

   /**
    * This will start up a timer which will keep the evaluations up to date, the disadvantage here is that
    * it will run every hour and on every server and therefore could increase the load substantially,
    * the other disadvantage is that if an evaluation goes from say active all the way to closed or viewable
    * then this would require a lot of extra logic to handle those cases,
    * holding off on using this for now -AZ
    */
   private void initiateUpdateStateTimer() {
      TimerTask runStateUpdateTask = new TimerTask() {
         @SuppressWarnings("unchecked")
         @Override
         public void run() {
            // get all evals that are not viewable (i.e. completed done with)
            List<EvalEvaluation> evals = dao.findByProperties(EvalEvaluation.class, 
                  new String[] {"state"}, 
                  new Object[] {EvalConstants.EVALUATION_STATE_VIEWABLE},
                  new int[] {ByPropsFinder.NOT_EQUALS});
            log.info("Checking the state of " + evals.size() + " evaluations to ensure they are all up to date...");
            // loop through and update the state of the evals if needed
            int count = 0;
            for (EvalEvaluation evaluation : evals) {
               if (! EvalUtils.getEvaluationState(evaluation).equals(evaluationService.returnAndFixEvalState(evaluation, true)) ) {
                  // could also trigger the various evaluation email events from this as well with extra logic here
                  count++;
               }
            }
            log.info("Updated the state of "+count+" evaluations...");
         }
      };
      Timer timer = new Timer(true);
      long startDelay = 1000 * 60 * new Random(new Date().getTime()).nextInt(30) + (1000 * 60 * 5);
      // start up a timer after 5 mins + random(30 mins) and run it every 60 mins
      timer.schedule(runStateUpdateTask, startDelay, 1000 * 60 * 60);
   }


   // EVALUATIONS

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
            ") cannot occur in the past for new evaluationSetupService");
         } else if (evaluation.getDueDate().before(today)) {
            throw new IllegalArgumentException(
                  "due date (" + evaluation.getDueDate() +
            ") cannot occur in the past for new evaluationSetupService");
         } else if (evaluation.getStopDate().before(today)) {
            throw new IllegalArgumentException(
                  "stop date (" + evaluation.getStopDate() +
            ") cannot occur in the past for new evaluationSetupService");
         }

         // make sure the state is set correctly
         evaluationService.returnAndFixEvalState(evaluation, false);

         // check user permissions (uses public method)
         if (! evaluationService.canBeginEvaluation(userId) ) {
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

            // Get the owned + not-owned evaluationSetupService i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, recent);
            }
            // Get the evaluationSetupService owned by the user
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

            // Get the owned + not-owned evaluationSetupService i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, null);
            }
            // get all evaluationSetupService created (owned) by this user
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

      // get the evaluationSetupService
      List<EvalEvaluation> evals = dao.getEvaluationsByEvalGroups( evalGroupIds, activeOnly, false, true );

      if (untakenOnly) {
         // filter out the evaluationSetupService this user already took

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


   // CATEGORIES

   public String[] getEvalCategories(String userId) {
      log.debug("userId: " + userId );

      // return all current categories or only return categories created by this user if not null
      List<String> l = dao.getEvalCategories(userId);
      return (String[]) l.toArray(new String[] {});
   }


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
    * Get both owned and not-owned evaluation for the given user.
    * 
    * @param userId the internal user id (not username).
    * @param recentOnly if true return recently closed evaluationSetupService only
    * (still returns all active and in queue evaluationSetupService), if false return all closed evaluationSetupService.
    * @param evalsToReturn list of owned and not-owned evaluationSetupService. 
    * @param recent date for comparison when looking for recently closed evaluationSetupService.
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
             * If only recent evaluationSetupService have to be fetched, then check for
             * stop date else just add to the existing list of evaluationSetupService.
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


   // GROUPS

   public void saveAssignGroup(EvalAssignGroup assignGroup, String userId) {
      log.debug("userId: " + userId + ", evalGroupId: " + assignGroup.getEvalGroupId());

      // set the date modified
      assignGroup.setLastModified( new Date() );

      EvalEvaluation eval = assignGroup.getEvaluation();
      if (eval == null || eval.getId() == null) {
         throw new IllegalStateException("Evaluation (" + eval.getId() + ") is not set or not saved for assignContext (" + 
               assignGroup.getId() + "), evalgroupId: " + assignGroup.getEvalGroupId() );
      }

      if (assignGroup.getId() == null) {
         // creating new AC
         if (securityChecks.checkCreateAssignGroup(userId, eval)) {
            // check for duplicate AC first
            if ( checkRemoveDuplicateAssignGroup(assignGroup) ) {
               throw new IllegalStateException("Duplicate mapping error, there is already an AC that defines a link from evalGroupId: " + 
                     assignGroup.getEvalGroupId() + " to eval: " + eval.getId());
            }

            dao.save(assignGroup);
            log.info("User ("+userId+") created a new AC ("+assignGroup.getId()+"), " +
                  "linked evalGroupId ("+assignGroup.getEvalGroupId()+") with eval ("+eval.getId()+")");
         }
      } else {
         // updating an existing AC

         // fetch the existing AC out of the DB to compare it
         EvalAssignGroup existingAC = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroup.getId());
         //log.info("AZQ: current AC("+existingAC.getId()+"): ctxt:" + existingAC.getContext() + ", eval:" + existingAC.getEvaluation().getId());

         // check the user control permissions
         if (! securityChecks.checkControlAssignGroup(userId, assignGroup) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing AC ("+existingAC.getId()+") without permissions");
         }

         // cannot change the evaluation or evalGroupId so fail if they have been changed
         if (! existingAC.getEvalGroupId().equals(assignGroup.getEvalGroupId())) {
            throw new IllegalArgumentException("Cannot update evalGroupId ("+assignGroup.getEvalGroupId()+
                  ") for an existing AC, evalGroupId ("+existingAC.getEvalGroupId()+")");
         } else if (! existingAC.getEvaluation().getId().equals(eval.getId())) {
            throw new IllegalArgumentException("Cannot update eval ("+eval.getId()+
                  ") for an existing AC, eval ("+existingAC.getEvaluation().getId()+")");
         }

         // fill in defaults
         if (assignGroup.getInstructorApproval() == null) {
            if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
               assignGroup.setInstructorApproval( Boolean.FALSE );
            } else {
               assignGroup.setInstructorApproval( Boolean.TRUE );
            }
         }

         // if a late instructor opt-in, notify students in this group that an evaluation is available
         if (EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt())
               && assignGroup.getInstructorApproval().booleanValue()
               && assignGroup.getEvaluation().getStartDate().before(new Date())) {
            emails.sendEvalAvailableGroupNotification(
                  assignGroup.getEvaluation().getId(),
                  assignGroup.getEvalGroupId());
         }

         if (assignGroup.getInstructorsViewResults() == null) {
            if (eval.getInstructorsDate() != null) {
               assignGroup.setInstructorsViewResults( Boolean.TRUE );
            } else {
               assignGroup.setInstructorsViewResults( Boolean.FALSE );
            }
         }
         if (assignGroup.getStudentsViewResults() == null) {
            if (eval.getStudentsDate() != null) {
               assignGroup.setStudentsViewResults( Boolean.TRUE );
            } else {
               assignGroup.setStudentsViewResults( Boolean.FALSE );
            }
         }

         // allow any other changes
         dao.save(assignGroup);
         log.info("User ("+userId+") updated existing AC ("+assignGroup.getId()+") properties");
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#deleteAssignGroup(java.lang.Long, java.lang.String)
    */
   public void deleteAssignGroup(Long assignGroupId, String userId) {
      log.debug("userId: " + userId + ", assignGroupId: " + assignGroupId);

      // get AC
      EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(assignGroupId);
      if (assignGroup == null) {
         throw new IllegalArgumentException("Cannot find assign evalGroupId with this id: " + assignGroupId);
      }

      EvalEvaluation eval = evaluationService.getEvaluationById(assignGroup.getEvaluation().getId());
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with this id: " + assignGroup.getEvaluation().getId());         
      }

      if ( securityChecks.checkRemoveAssignGroup(userId, assignGroup, eval) ) {
         dao.delete(assignGroup);
         log.info("User ("+userId+") deleted existing assign group ("+assignGroup.getId()+")");
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete assign group ("+assignGroup.getId()+")");
   }


   // HIERARCHY

   public List<EvalAssignHierarchy> addEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds) {

      // get the evaluation
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);

      // check if this evaluation can be modified
      String userId = external.getCurrentUserId();
      if (securityChecks.checkCreateAssignGroup(userId, eval)) {

         // first we have to get all the assigned hierarchy nodes for this eval
         Set<String> nodeIdsSet = new HashSet<String>();
         Set<String> currentNodeIds = new HashSet<String>();

         List<EvalAssignHierarchy> current = evaluationService.getAssignHierarchyByEval(evaluationId);
         for (EvalAssignHierarchy evalAssignHierarchy : current) {
            currentNodeIds.add(evalAssignHierarchy.getNodeId());
         }

         for (int i = 0; i < nodeIds.length; i++) {
            nodeIdsSet.add(nodeIds[i]);
         }

         // then remove the duplicates so we end up with the filtered list to only new ones
         nodeIdsSet.removeAll(currentNodeIds);
         nodeIds = nodeIdsSet.toArray(nodeIds);

         // now we need to create all the persistent hierarchy assignment objects
         Set<EvalAssignHierarchy> nodeAssignments = new HashSet<EvalAssignHierarchy>();
         for (String nodeId : nodeIdsSet) {
            EvalAssignHierarchy eah = new EvalAssignHierarchy(new Date(), userId, nodeId, false, true, false, eval);
            // fill in defaults and the values from the evaluation
            setDefaults(eval, eah);
            nodeAssignments.add(eah);
         }

         // next we have to get all the assigned eval groups for this eval
         Set<String> evalGroupIdsSet = new HashSet<String>();
         Set<String> currentEvalGroupIds = new HashSet<String>();

         // get the current list of assigned eval groups
         List<EvalAssignGroup> currentGroups = getEvaluationAssignGroups(evaluationId);
         for (EvalAssignGroup evalAssignGroup : currentGroups) {
            currentEvalGroupIds.add(evalAssignGroup.getEvalGroupId());
         }

         /*
          * According to the API Documentation, evalGroupIds can be null if there
          * are none to assign.
          */
         if (evalGroupIds == null) {
            evalGroupIds = new String[] {};
         }

         for (int i = 0; i < evalGroupIds.length; i++) {
            evalGroupIdsSet.add(evalGroupIds[i]);
         }

         // next we need to expand all the assigned hierarchy nodes into a massive set of eval assign groups
         Set<String> allNodeIds = new HashSet<String>();
         allNodeIds.addAll(nodeIdsSet);
         allNodeIds.addAll(currentNodeIds);
         Map<String, Set<String>> allEvalGroupIds = hierarchyLogic.getEvalGroupsForNodes( allNodeIds.toArray(nodeIds) );

         // now eliminate the evalgroupids from the evalGroupIds array which happen to be contained in the nodes,
         // this leaves us with only the group ids which are not contained in the nodes which are already assigned
         for (Set<String> egIds : allEvalGroupIds.values()) {
            evalGroupIdsSet.removeAll(egIds);
         }

         // then remove the eval groups ids which are already assigned to this eval so we only have new ones
         evalGroupIdsSet.removeAll(currentEvalGroupIds);
         evalGroupIds = evalGroupIdsSet.toArray(evalGroupIds);

         // now we need to create all the persistent group assignment objects for the new groups
         Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
         groupAssignments.addAll( makeAssignGroups(eval, userId, evalGroupIdsSet, null) );

         // finally we add in the groups for all the new expanded assign groups
         for (String nodeId : nodeIdsSet) {
            if (allEvalGroupIds.containsKey(nodeId)) {
               groupAssignments.addAll( makeAssignGroups(eval, userId, allEvalGroupIds.get(nodeId), nodeId) );               
            }
         }

         // save everything at once
         dao.saveMixedSet(new Set[] {nodeAssignments, groupAssignments});
         log.info("User (" + userId + ") added nodes (" + ArrayUtils.arrayToString(nodeIds)
               + ") and groups (" + ArrayUtils.arrayToString(evalGroupIds) + ") to evaluation ("
               + evaluationId + ")");
         List<EvalAssignHierarchy> results = new ArrayList<EvalAssignHierarchy>(nodeAssignments);
         results.addAll(groupAssignments);
         return results;
      }

      // should not get here so die if we do
      throw new RuntimeException("User (" + userId
            + ") could NOT create hierarchy assignments for nodes ("
            + ArrayUtils.arrayToString(nodeIds) + ") in evaluation (" + evaluationId + ")");
   }


   @SuppressWarnings("unchecked")
   public void deleteAssignHierarchyNodesById(Long[] assignHierarchyIds) {
      String userId = external.getCurrentUserId();
      // get the list of hierarchy assignments
      List<EvalAssignHierarchy> l = dao.findByProperties(EvalAssignHierarchy.class,
            new String[] { "id" }, new Object[] { assignHierarchyIds });
      if (l.size() > 0) {
         Set<String> nodeIds = new HashSet<String>();         
         Long evaluationId = l.get(0).getEvaluation().getId();

         Set<EvalAssignHierarchy> eahs = new HashSet<EvalAssignHierarchy>();
         for (EvalAssignHierarchy evalAssignHierarchy : l) {
            if (evaluationService.canDeleteAssignGroup(userId, evalAssignHierarchy.getId())) {
               nodeIds.add(evalAssignHierarchy.getNodeId());
               eahs.add(evalAssignHierarchy);
            }
         }

         // now get the list of assign groups with a nodeId that matches any of these and remove those also
         List<EvalAssignGroup> eags = dao.findByProperties(EvalAssignGroup.class,
               new String[] { "evaluation.id", "nodeId" }, 
               new Object[] { evaluationId, nodeIds });
         Set<EvalAssignGroup> groups = new HashSet<EvalAssignGroup>();
         StringBuilder groupListing = new StringBuilder();
         if (eags.size() > 0) {
            for (EvalAssignGroup evalAssignGroup : groups) {
               if (evaluationService.canDeleteAssignGroup(userId, evalAssignGroup.getId())) {
                  groups.add(evalAssignGroup);
                  groupListing.append(evalAssignGroup.getEvalGroupId() + ":");
               }
            }
         }

         dao.deleteMixedSet(new Set[] {eahs, groups});
         log.info("User (" + userId + ") deleted existing hierarchy assignments ("
               + ArrayUtils.arrayToString(assignHierarchyIds) + ") and groups ("+groupListing.toString()+")");
         return;

      }
      // should not get here so die if we do
      throw new RuntimeException("User (" + userId + ") could NOT delete hierarchy assignments ("
            + ArrayUtils.arrayToString(assignHierarchyIds) + ")");
   }

   // EMAIL TEMPLATES

   @SuppressWarnings("unchecked")
   public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId) {
      log.debug("userId: " + userId + ", emailTemplate: " + emailTemplate.getId());

      // set the date modified
      emailTemplate.setLastModified(new Date());

      // check user permissions
      if (! securityChecks.canUserControlEmailTemplate(userId, emailTemplate)) {
         throw new SecurityException("User (" + userId + ") cannot control email template ("
               + emailTemplate.getId() + ") without permissions");
      }

      // checks to keeps someone from overwriting the default templates
      if (emailTemplate.getId() == null) {
         // null out the defaultType for new templates 
         emailTemplate.setDefaultType(null);

      } else {
         // existing template
         if (emailTemplate.getDefaultType() != null) {
            throw new IllegalArgumentException(
            "Cannot modify default templates or set existing templates to be default");
         }

         // check if there are evaluationSetupService this is used in and if the user can modify this based on them
         // check available templates
         List<EvalEvaluation> l = dao.findByProperties(EvalEvaluation.class, new String[] { "availableEmailTemplate.id" },
               new Object[] { emailTemplate.getId() });
         for (int i = 0; i < l.size(); i++) {
            EvalEvaluation eval = (EvalEvaluation) l.get(i);
            // check eval/template permissions
            securityChecks.checkEvalTemplateControl(userId, eval, emailTemplate);
         }
         // check reminder templates
         l = dao.findByProperties(EvalEvaluation.class, new String[] { "reminderEmailTemplate.id" },
               new Object[] { emailTemplate.getId() });
         for (int i = 0; i < l.size(); i++) {
            EvalEvaluation eval = (EvalEvaluation) l.get(i);
            // check eval/template permissions
            securityChecks.checkEvalTemplateControl(userId, eval, emailTemplate);
         }
      }

      // save the template if allowed
      dao.save(emailTemplate);
      log.info("User (" + userId + ") saved email template (" + emailTemplate.getId() + ")");
   }



   // PRIVATE METHODS

   /**
    * Retrieve the complete set of eval assign groups for this evaluation
    * @param evaluationId
    * @return
    */
   @SuppressWarnings("unchecked")
   private List<EvalAssignGroup> getEvaluationAssignGroups(Long evaluationId) {
      // get all the evalGroupIds for the given eval ids in one storage call
      List<EvalAssignGroup> l = new ArrayList<EvalAssignGroup>();
      l = dao.findByProperties(EvalAssignGroup.class,
            new String[] {"evaluation.id"}, 
            new Object[] {evaluationId} );
      return l;
   }

   /**
    * @param evaluationId
    * @return
    */
   private EvalEvaluation getEvaluationOrFail(Long evaluationId) {
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Invalid eval id, cannot find evaluation with this id: " + evaluationId);
      }
      return eval;
   }

   /**
    * Check for existing AC which matches this ones linkage
    * @param ac
    * @return true if duplicate found
    */
   @SuppressWarnings("unchecked")
   private boolean checkRemoveDuplicateAssignGroup(EvalAssignGroup ac) {
      log.debug("assignContext: " + ac.getId());

      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evalGroupId", "evaluation.id"}, 
            new Object[] {ac.getEvalGroupId(), ac.getEvaluation().getId()});
      if ( (ac.getId() == null && l.size() >= 1) || 
            (ac.getId() != null && l.size() >= 2) ) {
         // there is an existing AC which does the same mapping
//       EvalAssignContext eac = (EvalAssignContext) l.get(0);
         return true;
      }
      return false;
   }

   /**
    * Create EvalAssignGroup objects from a set of evalGroupIds for an eval and user
    * @param eval
    * @param userId
    * @param evalGroupIdsSet
    * @param nodeId (optional), null if there is no nodeId association, otherwise set to the associated nodeId
    * @return the set with the new assignments
    */
   private Set<EvalAssignGroup> makeAssignGroups(EvalEvaluation eval, String userId, Set<String> evalGroupIdsSet, String nodeId) {
      Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
      for (String evalGroupId : evalGroupIdsSet) {
         String type = EvalConstants.GROUP_TYPE_PROVIDED;
         if (evalGroupId.startsWith("/site")) {
            type = EvalConstants.GROUP_TYPE_SITE;
         }
         EvalAssignGroup eag = new EvalAssignGroup(new Date(), userId, evalGroupId, type, false, true, false, eval, nodeId);
         // fill in defaults and the values from the evaluation
         setDefaults(eval, eag);
         groupAssignments.add(eag);
      }
      return groupAssignments;
   }

   /**
    * @param eval
    * @param eah
    */
   private void setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah) {
      if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
         eah.setInstructorApproval( Boolean.FALSE );
      } else {
         eah.setInstructorApproval( Boolean.TRUE );
      }
      if (eval.getInstructorsDate() != null) {
         eah.setInstructorsViewResults( Boolean.TRUE );
      } else {
         eah.setInstructorsViewResults( Boolean.FALSE );
      }
      if (eval.getStudentsDate() != null) {
         eah.setStudentsViewResults( Boolean.TRUE );
      } else {
         eah.setStudentsViewResults( Boolean.FALSE );
      }
   }


}
