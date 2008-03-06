/**
 * $Id$
 * $URL$
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

package org.sakaiproject.evaluation.logic;

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
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.utils.ArrayUtils;

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

   private EvalSecurityChecksImpl securityChecks;
   public void setSecurityChecks(EvalSecurityChecksImpl securityChecks) {
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
      initiateUpdateStateTimer();
   }

   /**
    * This will start up a timer which will keep the evaluations up to date, the disadvantage here is that
    * it will run every hour and on every server and therefore could increase the load substantially,
    * the other disadvantage is that if an evaluation goes from say active all the way to closed or viewable
    * then this would require a lot of extra logic to handle those cases,
    * holding off on using this for now -AZ
    */
   public static String EVAL_UPDATE_TIMER = "eval_update_timer";
   protected void initiateUpdateStateTimer() {
      // timer repeats every 60 minutes
      final long repeatInterval = 1000 * 60 * 60;
      // start up a timer after 2 mins + random(10 mins)
      long startDelay =  (1000 * 60 * 2) + (1000 * 60 * new Random().nextInt(10));

      TimerTask runStateUpdateTask = new TimerTask() {
         @SuppressWarnings("unchecked")
         @Override
         public void run() {
            String serverId = external.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
            Boolean lockObtained = dao.obtainLock(EVAL_UPDATE_TIMER, serverId, repeatInterval);
            // only execute the code if we have an exclusive lock
            if (lockObtained != null && lockObtained) {
               // get all evals that are not viewable (i.e. completely done with) or deleted 
               List<EvalEvaluation> evals = dao.findByProperties(EvalEvaluation.class, 
                     new String[] {"state", "state"}, 
                     new Object[] {EvalConstants.EVALUATION_STATE_VIEWABLE, EvalConstants.EVALUATION_STATE_DELETED},
                     new int[] {EvaluationDao.NOT_EQUALS, EvaluationDao.NOT_EQUALS});
               if (evals.size() > 0) {
                  log.info("Checking the state of " + evals.size() + " evaluations to ensure they are all up to date...");
                  // set the partial purge number of days to 2
                  long partialPurgeTime = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000);
                  // loop through and update the state of the evals if needed
                  int count = 0;
                  for (EvalEvaluation evaluation : evals) {
                     String evalState = evaluationService.returnAndFixEvalState(evaluation, false);
                     if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evalState)) {
                        // purge out partial evaluations older than the partial purge time
                        if (evaluation.getLastModified().getTime() < partialPurgeTime) {
                           log.info("Purging partial evaluation ("+evaluation.getId()+") from " + evaluation.getLastModified());
                           deleteEvaluation(evaluation.getId(), EvalExternalLogic.ADMIN_USER_ID);
                        }
                     } else {
                        String currentEvalState = evaluation.getState();
                        if (! currentEvalState.equals(evalState) ) {
                           evalState = evaluationService.returnAndFixEvalState(evaluation, true); // update the state
                           count++;
                           // trigger the jobs logic to look at this since the state changed
                           evalJobLogic.processEvaluationStateChange(evaluation.getId(), EvalJobLogic.ACTION_UPDATE);
                        }
                     }
                  }
                  if (count > 0) {
                     log.info("Updated the state of "+count+" evaluations...");
                  }
               }

               // finally we will reset the system config cache
               if (settings instanceof EvalSettingsImpl) {
                  ((EvalSettingsImpl) settings).resetCache();
               }
            }
         }
      };

      // now we need to obtain a lock and then run the task if we have it
      Timer timer = new Timer(true);
      log.info("Initializing the repeating timer task for evaluation, first run in " + (startDelay/1000) + " seconds " +
      		"and subsequent runs will happen every " + (repeatInterval/1000) + " seconds after that");
      timer.schedule(runStateUpdateTask, startDelay, repeatInterval);
   }


   // EVALUATIONS

   public void saveEvaluation(EvalEvaluation evaluation, String userId) {
      log.debug("evalId: " + evaluation.getId() + ",userId: " + userId);

      boolean newEvaluation = false;

      // set the date modified
      evaluation.setLastModified( new Date() );

      // test date ordering first (for the dates that are set) - this should be externalized
      if (evaluation.getDueDate() != null) {
         if (evaluation.getStartDate().compareTo(evaluation.getDueDate()) >= 0) {
            throw new IllegalArgumentException(
                  "due date (" + evaluation.getDueDate() +
                  ") must occur after start date (" + 
                  evaluation.getStartDate() + "), can occur on the same date but not at the same time");
         }

         if (evaluation.getStopDate() != null) {
            if (evaluation.getDueDate().compareTo(evaluation.getStopDate()) > 0 ) {
               throw new IllegalArgumentException(
                     "stop date (" + evaluation.getStopDate() +
                     ") must occur on or after due date (" + 
                     evaluation.getDueDate() + "), can be identical");
            }
            if (evaluation.getViewDate() != null) {
               if (evaluation.getViewDate().compareTo(evaluation.getStopDate()) < 0 ) {
                  throw new IllegalArgumentException(
                        "view date (" + evaluation.getViewDate() +
                        ") must occur on or after stop date (" + 
                        evaluation.getStopDate() + "), can be identical");
               }
            }
         }

         if (evaluation.getViewDate() != null) {
            if (evaluation.getViewDate().compareTo(evaluation.getDueDate()) < 0 ) {
               throw new IllegalArgumentException(
                     "view date (" + evaluation.getViewDate() +
                     ") must occur on or after due date (" + 
                     evaluation.getDueDate() + "), can be identical");
            }
         }
      }

      // now perform checks depending on whether this is new or existing
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.add(Calendar.MINUTE, -30); // put today a bit in the past (30 minutes)
      Date today = calendar.getTime();
      if (evaluation.getId() == null) { // creating new evaluation
         newEvaluation = true;

         if (evaluation.getDueDate() != null 
               && evaluation.getDueDate().before(today)) {
            throw new IllegalArgumentException(
                  "due date (" + evaluation.getDueDate() +
            ") cannot occur in the past for new evaluations");
         }
         if (evaluation.getStopDate() != null 
               && evaluation.getStopDate().before(today)) {
            throw new IllegalArgumentException(
                  "stop date (" + evaluation.getStopDate() +
            ") cannot occur in the past for new evaluations");
         }

         // test if new evaluation occurs in the past
         if (evaluation.getStartDate().before(today)) {
            log.warn("Evaluation was set to start in the past ("+evaluation.getStartDate()+"), it has been reset to start now...");
            evaluation.setStartDate( new Date() );
         }

         // check user permissions (uses public method)
         if (! evaluationService.canBeginEvaluation(userId) ) {
            throw new SecurityException("User ("+userId+") attempted to create evaluation without permissions");
         }

      } else { // updating existing evaluation

         if (! securityChecks.canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }

         // All other checks have been moved to the tool (bad I know)
      }

      // make sure the evaluation type required field is set
      if (evaluation.getType() == null) {
         evaluation.setType(EvalConstants.EVALUATION_TYPE_EVALUATION);
      }

      // make sure the state is set correctly (does not override special states)
      evaluationService.returnAndFixEvalState(evaluation, false);

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

      // force the student/instructor dates based on the boolean settings
      if (evaluation.studentViewResults != null && ! evaluation.studentViewResults) {
         evaluation.setStudentsDate(null);
      }
      if (evaluation.instructorViewResults != null && ! evaluation.instructorViewResults) {
         evaluation.setInstructorsDate(null);
      }

      // fill in any default values and nulls here
      if (evaluation.getLocked() == null) {
         evaluation.setLocked( Boolean.FALSE );
      }
      if (evaluation.getResultsSharing() == null) {
         evaluation.setResultsSharing( EvalConstants.SHARING_VISIBLE );
      }
      if (evaluation.getAuthControl() == null) {
         evaluation.setAuthControl( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ );
      }

      if (evaluation.getEvalCategory() != null && 
            evaluation.getEvalCategory().length() <= 0) {
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
      // TODO - disabled for now
//      String systemInstructorOpt = (String) settings.get( EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE );
//      if ( systemInstructorOpt == null ) {
//         if (evaluation.getInstructorOpt() == null) {
//            evaluation.setInstructorOpt( EvalConstants.INSTRUCTOR_OPT_OUT );
//         }
//      } else {
//         evaluation.setInstructorOpt( systemInstructorOpt );
//      }
      evaluation.setInstructorOpt(EvalConstants.INSTRUCTOR_REQUIRED);

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
         dao.lockEvaluation(evaluation, true);
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
               int evalsUsingTemplate = dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"availableEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) ;
               if ( evalsUsingTemplate <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getAvailableEmailTemplate() );
               }
            }
         }
         if (evaluation.getReminderEmailTemplate() != null) {
            if (evaluation.getReminderEmailTemplate().getDefaultType() == null) {
               Long emailTemplateId = evaluation.getReminderEmailTemplate().getId();
               int evalsUsingTemplate = dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"reminderEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) ;
               if ( evalsUsingTemplate <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getReminderEmailTemplate() );
               }
            }
         }

         // add eval to a set to be removed
         Set evalSet = new HashSet();
         entitySets[3] = evalSet;
         evalSet.add(evaluation);

         // unlock the evaluation (this is clear the other locks)
         dao.lockEvaluation(evaluation, false);

         // destroy all the related responses and answers for now
         List<Long> responseIds = dao.getResponseIds(evaluation.getId(), null, null, null);
         if (responseIds.size() > 0) {
            dao.removeResponses( responseIds.toArray(new Long[responseIds.size()]) );
         }

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

      Date recentClosedDate = null;
      if (recentOnly) {
         // only get recently closed evals, check system setting to get "recent" value
         Integer recentlyClosedDays = (Integer) settings.get(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS);
         if (recentlyClosedDays == null) { recentlyClosedDays = 10; }
         Calendar calendar = GregorianCalendar.getInstance();
         calendar.add(Calendar.DATE, -1 * recentlyClosedDays.intValue());
         recentClosedDate = calendar.getTime();
      }

      String[] evalGroupIds = null;
      if (external.isUserAdmin(userId)) {
         // null out the userId so we get all evaluations
         userId = null;
      } else {
         if (showNotOwned) {
            // Get the list of EvalGroup where user has "eval.be.evaluated" permission.
            List<EvalGroup> evaluatedGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED);
            if (evaluatedGroups.size() > 0) {
               evalGroupIds = new String[evaluatedGroups.size()];
               for (int i = 0; i < evaluatedGroups.size(); i++) {
                  EvalGroup c = (EvalGroup) evaluatedGroups.get(i);
                  evalGroupIds[i] = c.evalGroupId;
               }
            }
         }
      }

      List<EvalEvaluation> l = dao.getEvaluationsForOwnerAndGroups(userId, evalGroupIds, recentClosedDate, 0, 0);

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



   // GROUPS

   public void saveAssignGroup(EvalAssignGroup assignGroup, String userId) {
      log.debug("userId: " + userId + ", evalGroupId: " + assignGroup.getEvalGroupId());

      // set the date modified
      assignGroup.setLastModified( new Date() );

      EvalEvaluation eval = assignGroup.getEvaluation();
      if (eval == null || eval.getId() == null) {
         throw new IllegalStateException("Evaluation (" + eval.getId() + ") is not set or not saved for assignGroup (" + 
               assignGroup.getId() + "), evalgroupId: " + assignGroup.getEvalGroupId() );
      }

      setDefaults(eval, assignGroup); // set the defaults before saving

      if (assignGroup.getId() == null) {
         // creating new AC
         if (securityChecks.checkCreateAssignGroup(userId, eval)) {
            // check for duplicate AC first
            if ( checkRemoveDuplicateAssignGroup(assignGroup) ) {
               throw new IllegalStateException("Duplicate mapping error, there is already an assignGroup that defines a link from evalGroupId: " + 
                     assignGroup.getEvalGroupId() + " to eval: " + eval.getId());
            }

            dao.save(assignGroup);

            // if a late instructor opt-in, notify students in this group that an evaluation is available
            if (EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt())
                  && assignGroup.getInstructorApproval().booleanValue()
                  && assignGroup.getEvaluation().getStartDate().before(new Date())) {
               emails.sendEvalAvailableGroupNotification(
                     assignGroup.getEvaluation().getId(), assignGroup.getEvalGroupId());
            }

            log.info("User ("+userId+") created a new assignGroup ("+assignGroup.getId()+"), " +
                  "linked evalGroupId ("+assignGroup.getEvalGroupId()+") with eval ("+eval.getId()+")");
         }
      } else {
         // updating an existing AG

         // fetch the existing AG out of the DB to compare it
         EvalAssignGroup existingAG = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroup.getId());

         // check the user control permissions
         if (! securityChecks.checkControlAssignGroup(userId, assignGroup) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing assignGroup ("+existingAG.getId()+") without permissions");
         }

         // cannot change the evaluation or evalGroupId so fail if they have been changed
         if (! existingAG.getEvalGroupId().equals(assignGroup.getEvalGroupId())) {
            throw new IllegalArgumentException("Cannot update evalGroupId ("+assignGroup.getEvalGroupId()+
                  ") for an existing AC, evalGroupId ("+existingAG.getEvalGroupId()+")");
         } else if (! existingAG.getEvaluation().getId().equals(eval.getId())) {
            throw new IllegalArgumentException("Cannot update eval ("+eval.getId()+
                  ") for an existing AC, eval ("+existingAG.getEvaluation().getId()+")");
         }

         // allow any other changes
         dao.save(assignGroup);
         log.info("User ("+userId+") updated existing assignGroup ("+assignGroup.getId()+") properties");
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
            // set the settings to null to allow the defaults to override correctly
            EvalAssignHierarchy eah = new EvalAssignHierarchy(userId, nodeId, eval);
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
         boolean userAdmin = external.isUserAdmin(userId);
         // existing template

         if (! userAdmin) {
            if (emailTemplate.getDefaultType() != null) {
               throw new IllegalArgumentException(
                     "Cannot modify default templates or set existing templates to be default unless you are an admin");
            }

            // check if there are evaluations this is used in and if the user can modify this based on them

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
         } else {
            // admin can modify any templates that they like
         }
      }

      // save the template if allowed
      dao.save(emailTemplate);
      log.info("User (" + userId + ") saved email template (" + emailTemplate.getId() + ")");
   }

   @SuppressWarnings("unchecked")
   public void removeEmailTemplate(Long emailTemplateId, String userId) {
      EvalEmailTemplate emailTemplate = evaluationService.getEmailTemplate(emailTemplateId);
      if (emailTemplate != null) {
         if (emailTemplate.getDefaultType() != null) {
            throw new IllegalArgumentException("Cannot remove email templates ("+emailTemplateId+") which are defaults: " + emailTemplate.getDefaultType());
         }
         securityChecks.checkEvalTemplateControl(userId, null, emailTemplate);
         String emailTemplateType = emailTemplate.getType();

         if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateType)
               || EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateType) ) {
            String templateTypeEval = "availableEmailTemplate";
            if ( EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateType) ) {
               templateTypeEval = "reminderEmailTemplate";
            }
            // get the evals that this template is used in
            List<EvalEvaluation> evals = dao.findByProperties(EvalEvaluation.class, 
                  new String[] {templateTypeEval + ".id"}, 
                  new Object[] {emailTemplateId});
            for (EvalEvaluation evaluation : evals) {
               // replace with the default template (that means null it out)
               if ( EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateType) ) {
                  evaluation.setAvailableEmailTemplate(null);
               } else if ( EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplate.getType()) ) {
                  evaluation.setReminderEmailTemplate(null);
               }
               dao.save(evaluation); // save the new template
            }
         }

         // now go ahead and wipe out the template itself
         dao.delete(emailTemplate);
      }
   }



   // INTERNAL METHODS

   /**
    * Retrieve the complete set of eval assign groups for this evaluation
    * @param evaluationId
    * @return
    */
   @SuppressWarnings("unchecked")
   protected List<EvalAssignGroup> getEvaluationAssignGroups(Long evaluationId) {
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
   protected EvalEvaluation getEvaluationOrFail(Long evaluationId) {
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
   protected boolean checkRemoveDuplicateAssignGroup(EvalAssignGroup ac) {
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
   protected Set<EvalAssignGroup> makeAssignGroups(EvalEvaluation eval, String userId, Set<String> evalGroupIdsSet, String nodeId) {
      Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
      for (String evalGroupId : evalGroupIdsSet) {
         String type = EvalConstants.GROUP_TYPE_PROVIDED;
         if (evalGroupId.startsWith("/site")) {
            type = EvalConstants.GROUP_TYPE_SITE;
         }
         // set the booleans to null to get the correct defaults set
         EvalAssignGroup eag = new EvalAssignGroup(userId, evalGroupId, type, eval);
         eag.setNodeId(nodeId);
         // fill in defaults and the values from the evaluation
         setDefaults(eval, eag);
         groupAssignments.add(eag);
      }
      return groupAssignments;
   }

   /**
    * Ensures that the settings for assignments are correct based on the system settings and the evaluation settings,
    * also ensures that none of them are null
    * 
    * @param eval the evaluation associated with this assginment
    * @param eah the assignment object (persistent or non)
    */
   protected void setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah) {
      // setInstructorsViewResults
      if (eah.getInstructorsViewResults() == null) {
         Boolean instViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
         if (instViewResults == null) {
            if (eval.getInstructorsDate() != null) {
               eah.setInstructorsViewResults( Boolean.TRUE );
            } else {
               eah.setInstructorsViewResults( Boolean.FALSE );
            }
         } else {
            eah.setInstructorsViewResults(instViewResults);
         }
      }
      // setStudentsViewResults
      if (eah.getStudentsViewResults() == null) {
         Boolean studViewResults = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
         if (studViewResults == null) {
            if (eval.getStudentsDate() != null) {
               eah.setStudentsViewResults( Boolean.TRUE );
            } else {
               eah.setStudentsViewResults( Boolean.FALSE );
            }
         } else {
            eah.setStudentsViewResults(studViewResults);
         }
      }
      // setInstructorApproval
      // TODO - temporary force to enabled
      eah.setInstructorApproval( Boolean.TRUE );
//      if (eah.getInstructorApproval() == null) {
//         String globalEvalOpt = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
//         if (globalEvalOpt == null) {
//            if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
//               eah.setInstructorApproval( Boolean.FALSE );
//            } else {
//               // REQUIRED or OPT_OUT set to true
//               eah.setInstructorApproval( Boolean.TRUE );
//            }
//         } else {
//            if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(globalEvalOpt) ) {
//               eah.setInstructorApproval( Boolean.FALSE );
//            } else {
//               // REQUIRED or OPT_OUT set to true
//               eah.setInstructorApproval( Boolean.TRUE );
//            }
//         }
//      }
   }


}
