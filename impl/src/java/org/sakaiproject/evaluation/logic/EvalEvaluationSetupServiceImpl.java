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
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidDatesException;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

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
   private final String EVENT_EVAL_CLOSED = "eval.evaluation.closed.early";

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic common) {
      this.commonLogic = common;
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

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
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
        	String serverId = commonLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
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
                  // set the partial purge number of days to 5
                  long partialPurgeTime = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000);
                  // loop through and update the state of the evals if needed
                  int count = 0;
                  for (EvalEvaluation evaluation : evals) {
                     String evalState = evaluationService.returnAndFixEvalState(evaluation, false);
                     if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evalState)) {
                        // purge out partial evaluations older than the partial purge time
                        if (evaluation.getLastModified().getTime() < partialPurgeTime) {
                           log.info("Purging partial evaluation ("+evaluation.getId()+") from " + evaluation.getLastModified());
                           deleteEvaluation(evaluation.getId(), EvalCommonLogic.ADMIN_USER_ID);
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
               /*
                * CT-700/EVALSYS-607 both of these expressions evaluate to false.
                * System settings cache was not being refreshed. Perhaps there is
                * a way to do this with Hibernate proxies, but for now exposing 
                * method through interface instead.
                * 
               if(settings.getClass().isAssignableFrom(EvalSettingsImpl.class)) {
                   ((EvalSettingsImpl) settings).resetCache();
               }
               // finally we will reset the system config cache
               if (settings instanceof EvalSettingsImpl) {
                  ((EvalSettingsImpl) settings).resetCache();
               }
               */
            }
            // we want this to happen on all servers in the cluster
            settings.resetCache();
         }
      };

      // now we need to obtain a lock and then run the task if we have it
      Timer timer = new Timer(true);
      log.info("Initializing the repeating timer task for evaluation, first run in " + (startDelay/1000) + " seconds " +
      		"and subsequent runs will happen every " + (repeatInterval/1000) + " seconds after that");
      timer.schedule(runStateUpdateTask, startDelay, repeatInterval);
   }


   // EVALUATIONS

   public void saveEvaluation(EvalEvaluation evaluation, String userId, boolean created) {
      log.debug("evalId: " + evaluation.getId() + ",userId: " + userId);

      // set the date modified
      evaluation.setLastModified( new Date() );

      if (created && EvalUtils.checkStateAfter(evaluation.getState(), EvalConstants.EVALUATION_STATE_PARTIAL, false)) {
         // created can only be true when this eval is in partial state
         created = false;
      }

      // check for required fields first
      if (EvalUtils.isBlank(evaluation.getTitle())) {
         throw new BlankRequiredFieldException("Cannot save an evaluation with a blank title", "title");
      }

      if (evaluation.getStartDate() == null) {
         throw new BlankRequiredFieldException("Cannot save an evaluation with a null startDate", "startDate");
      }

      // test date ordering first (for the dates that are set) - this should be externalized
      if (evaluation.getDueDate() != null) {
         if (evaluation.getStartDate().compareTo(evaluation.getDueDate()) >= 0) {
            throw new InvalidDatesException(
                  "due date (" + evaluation.getDueDate() +
                  ") must occur after start date (" + 
                  evaluation.getStartDate() + "), can occur on the same date but not at the same time",
                  "dueDate");
         }

         if (evaluation.getStopDate() != null) {
            if (evaluation.getDueDate().compareTo(evaluation.getStopDate()) > 0 ) {
               throw new InvalidDatesException(
                     "stop date (" + evaluation.getStopDate() +
                     ") must occur on or after due date (" + 
                     evaluation.getDueDate() + "), can be identical",
                     "stopDate");
            }
            if (evaluation.getViewDate() != null) {
               if (evaluation.getViewDate().compareTo(evaluation.getStopDate()) < 0 ) {
                  throw new InvalidDatesException(
                        "view date (" + evaluation.getViewDate() +
                        ") must occur on or after stop date (" + 
                        evaluation.getStopDate() + "), can be identical",
                        "viewDate");
               }
            }
         }

         if (evaluation.getViewDate() != null) {
            if (evaluation.getViewDate().compareTo(evaluation.getDueDate()) < 0 ) {
               throw new InvalidDatesException(
                     "view date (" + evaluation.getViewDate() +
                     ") must occur on or after due date (" + 
                     evaluation.getDueDate() + "), can be identical",
                     "viewDate");
            }
         }
      }

      // now perform checks depending on whether this is new or existing
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.add(Calendar.MINUTE, -30); // put today a bit in the past (30 minutes)
      Date today = calendar.getTime();
      if (evaluation.getId() == null) {
         // creating new evaluation

         if (evaluation.getDueDate() != null 
               && evaluation.getDueDate().before(today)) {
            throw new InvalidDatesException(
                  "due date (" + evaluation.getDueDate() +
                  ") cannot occur in the past for new evaluations",
                  "dueDate");
         }
         if (evaluation.getStopDate() != null 
               && evaluation.getStopDate().before(today)) {
            throw new InvalidDatesException(
                  "stop date (" + evaluation.getStopDate() +
                  ") cannot occur in the past for new evaluations",
                  "stopDate");
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

      } else {
         // updating existing evaluation

         if (! securityChecks.canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }

         // All other checks have been moved to the tool (bad I know)
      }

      // make sure the state is set correctly (does not override special states)
      if (created) {
         // eval was just created so we will force it out of partial state
         evaluation.setState(EvalConstants.EVALUATION_STATE_INQUEUE);
      }
      String evalState = evaluationService.returnAndFixEvalState(evaluation, false);

      // make sure we are not using a blank template here and get the template without using lazy loading
      EvalTemplate template = null;
      if (evaluation.getTemplate() == null 
            || evaluation.getTemplate().getId() == null) {
         throw new IllegalArgumentException("Evaluations must include a template (it cannot be null)");
      } else {
         // template is set so check that it has items in it
         template = authoringService.getTemplateById(evaluation.getTemplate().getId());
         if (template.getTemplateItems() == null 
               || template.getTemplateItems().size() <= 0) {
            throw new IllegalArgumentException("Evaluations must include a template with at least one item in it");
         }         
      }

      // make sure the template is copied if not in partial state, it is ok to have the original template while in partial state
      if ( EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_PARTIAL, false) ) {
         // this eval is not partial anymore so the template MUST be a hidden copy
         if (template.getCopyOf() == null ||
               template.isHidden() == false) {
            // not a hidden copy so make one
            Long copiedTemplateId = authoringService.copyTemplate(template.getId(), null, evaluation.getOwner(), true, true);
            EvalTemplate copy = authoringService.getTemplateById(copiedTemplateId);
            evaluation.setTemplate(copy);
            template = copy; // set the new template to the template variable
            // alternative is to throw an exception to force the user to do this, but we may as well handle it
//            throw new IllegalStateException("This evaluation ("+evaluation.getId()+") is being saved "
//            		+ "in a state ("+evalState+") that is after the partial state with "
//                  + "a template that has not been copied yet, this is invalid as all evaluations must use copied "
//                  + "templates, copy the template using the authoringService.copyTemplate method before saving this eval");
         }
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

      // make sure the evaluation type required field is set
      if (evaluation.getType() == null) {
         evaluation.setType(EvalConstants.EVALUATION_TYPE_EVALUATION);
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

      // cleanup for XSS scripting and strings
      evaluation.setTitle( commonLogic.cleanupUserStrings(evaluation.getTitle()) );
      evaluation.setInstructions( commonLogic.cleanupUserStrings(evaluation.getInstructions()) );
      evaluation.setReminderFromEmail( commonLogic.cleanupUserStrings(evaluation.getReminderFromEmail()) );
      evaluation.setEvalCategory( commonLogic.cleanupUserStrings(evaluation.getEvalCategory()) );

      // save the eval
      dao.save(evaluation);
      log.info("User ("+userId+") saved evaluation ("+evaluation.getId()+"), title: " + evaluation.getTitle());

      // initialize the scheduling for the eval jobs (only if state is not partial)
      if ( EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_PARTIAL, false) ) {
         if (created) {
            commonLogic.registerEntityEvent(EVENT_EVAL_CREATE, evaluation);
            // call logic to manage Quartz scheduled jobs
            evalJobLogic.processEvaluationStateChange(evaluation.getId(), EvalJobLogic.ACTION_CREATE);
         } else {
            commonLogic.registerEntityEvent(EVENT_EVAL_UPDATE, evaluation);
            // call logic to manage Quartz scheduled jobs
            evalJobLogic.processEvaluationStateChange(evaluation.getId(), EvalJobLogic.ACTION_UPDATE);
         }
      }

      // support for autoUse insertion of items on eval creation
      if (created) {
         if (! EvalUtils.isBlank(evaluation.getAutoUseInsertion())
               && ! EvalUtils.isBlank(evaluation.getAutoUseTag()) ) {
            // the tag and the AutoUseInsertion values are set so we should try to find any autoUse items and add them in
            Long templateId = evaluation.getTemplate().getId();
            authoringService.doAutoUseInsertion(evaluation.getAutoUseTag(), templateId, evaluation.getAutoUseInsertion(), true);
         }
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

         // remove associated AssignGroups
         List<EvalAssignGroup> acs = dao.findByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignGroup> assignGroupSet = new HashSet<EvalAssignGroup>(acs);
         dao.deleteSet(assignGroupSet);

         // remove associated assigned hierarchy nodes
         List<EvalAssignHierarchy> ahs = dao.findByProperties(EvalAssignHierarchy.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignHierarchy> assignHierSet = new HashSet<EvalAssignHierarchy>(ahs);
         dao.deleteSet(assignHierSet);

         EvalEmailTemplate available = evaluation.getAvailableEmailTemplate();
         EvalEmailTemplate reminder = evaluation.getReminderEmailTemplate();

         // unlock the evaluation (this will clear the other locks)
         dao.lockEvaluation(evaluation, false);

         boolean removeTemplate = false;
         // check for related responses and answers
         List<Long> responseIds = dao.getResponseIds(evaluation.getId(), null, null, null);
         if (responseIds.size() > 0) {
            // cannot remove this evaluation, there are responses, we will just set the state to deleted
            evaluation.setState(EvalConstants.EVALUATION_STATE_DELETED);
            // clear email templates
            evaluation.setAvailableEmailTemplate(null);
            evaluation.setReminderEmailTemplate(null);
            // save the new state
            dao.save(evaluation);

            // old method was to actually remove data
            // dao.removeResponses( responseIds.toArray(new Long[responseIds.size()]) );
         } else {
            // remove the evaluation and copied template since there are no responses
            removeTemplate = true;
            dao.delete(evaluation);
         }

         // fire the evaluation deleted event
         commonLogic.registerEntityEvent(EVENT_EVAL_DELETE, evaluation);

         // remove any remaining scheduled jobs
         evalJobLogic.processEvaluationStateChange(evaluationId, EvalJobLogic.ACTION_DELETE);

         // this has to be after the removal of the evaluation

         // remove associated unused email templates
         Set<EvalEmailTemplate> emailSet = new HashSet<EvalEmailTemplate>();
         if (available != null) {
            if (available.getDefaultType() == null) {
               // only remove non-default templates
               int evalsUsingTemplate = dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"availableEmailTemplate.id"}, 
                     new Object[] {available.getId()}) ;
               if ( evalsUsingTemplate <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( available );
               }
            }
         }
         if (reminder != null) {
            if (reminder.getDefaultType() == null) {
               int evalsUsingTemplate = dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"reminderEmailTemplate.id"}, 
                     new Object[] {reminder.getId()}) ;
               if ( evalsUsingTemplate <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( reminder );
               }
            }
         }
         dao.deleteSet(emailSet);

         if (removeTemplate) {
            // remove template if it is a copy
            if (EvalUtils.checkStateAfter(evaluation.getState(), EvalConstants.EVALUATION_STATE_PARTIAL, false)) {
               // this is not partial (partials do not have copies made yet)
               // remove the associated template if it is a copy (it should be)
               EvalTemplate template = null;
               if (evaluation.getTemplate() != null 
                     || evaluation.getTemplate().getId() != null) {
                  // there is a template so get it and check to see if it needs to be removed
                  template = authoringService.getTemplateById(evaluation.getTemplate().getId());
                  if (template.getCopyOf() != null ||
                        template.isHidden() == true) {
                     // this is a copy so remove it and all children
                     if (securityChecks.checkUserControlTemplate(userId, template)) {
                        authoringService.deleteTemplate(template.getId(), userId);
                     } else {
                        log.warn("Could not remove the template ("+template.getId()+") associated with this "
                              + "eval ("+evaluationId+") since this user has no permission, continuing to remove evaluation anyway");
                     }
                  }
               }
            }
         }

         log.info("User ("+userId+") removed evaluation ("+evaluationId+"), title: " + evaluation.getTitle());
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete evaluation ("+evaluationId+")");
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#closeEvaluation(java.lang.Long, java.lang.String)
    */
   public EvalEvaluation closeEvaluation(Long evaluationId, String userId) {
      EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
      if (evaluation == null) {
         throw new IllegalArgumentException("Invalid evaluation id, cannot find evaluation: " + evaluationId);
      }

      String evalState = evaluationService.returnAndFixEvalState(evaluation, true);
      if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_CLOSED, false)) {
         // set closing date to now
         Date now = new Date();
         evaluation.setDueDate(now);

         // fix stop and view dates if needed
         evaluation.setStopDate(null);
         if (evaluation.getViewDate() != null
               && evaluation.getViewDate().before(now)) {
            evaluation.setViewDate(null);
         }

         // fix up state (should go to closed)
         evaluationService.returnAndFixEvalState(evaluation, false);

         // save evaluation (should also update the email sending)
         saveEvaluation(evaluation, userId, false);
   
         // fire the evaluation closed event
         commonLogic.registerEntityEvent(EVENT_EVAL_CLOSED, evaluation);
      } else {
         log.warn(userId + " tried to close eval that is already closed ("+evaluationId+"): " + evaluation.getTitle());
      }

      return evaluation;
   }

   
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned, boolean includePartial) {

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
      if (commonLogic.isUserAdmin(userId)) {
         // null out the userId so we get all evaluations
         userId = null;
      } else {
         if (showNotOwned) {
            // Get the list of EvalGroup where user has "eval.be.evaluated" permission.
            List<EvalGroup> evaluatedGroups = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED);
            if (evaluatedGroups.size() > 0) {
               evalGroupIds = new String[evaluatedGroups.size()];
               for (int i = 0; i < evaluatedGroups.size(); i++) {
                  EvalGroup c = (EvalGroup) evaluatedGroups.get(i);
                  evalGroupIds[i] = c.evalGroupId;
               }
            }
         }
      }

      List<EvalEvaluation> l = dao.getEvaluationsForOwnerAndGroups(userId, evalGroupIds, recentClosedDate, 0, 0, includePartial);

      return l;
   }


   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationsForUser(java.lang.String, boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsForUser(String userId, Boolean activeOnly, Boolean untakenOnly, Boolean includeAnonymous) {
      List<EvalGroup> takeGroups = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

      String[] evalGroupIds = new String[takeGroups.size()];
      for (int i=0; i<takeGroups.size(); i++) {
         EvalGroup c = (EvalGroup) takeGroups.get(i);
         evalGroupIds[i] = c.evalGroupId;
      }

      // get the evaluations
      List<EvalEvaluation> evals = dao.getEvaluationsByEvalGroups( evalGroupIds, activeOnly, true, includeAnonymous, 0, 0 );

      if (evals.size() > 0) {
         // filter out taken/untaken if desired
         if (untakenOnly != null) {
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
                     if (untakenOnly) {
                        // filter out the evaluations this user already took
                        evals.remove(j);
                     }
                  } else {
                     if (! untakenOnly) {
                        // filter out the evaluations this user hasn't taken
                        evals.remove(j);
                     }
                  }
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
         List takeGroups = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

         String[] evalGroupIds = new String[takeGroups.size()];
         for (int i=0; i<takeGroups.size(); i++) {
            EvalGroup c = (EvalGroup) takeGroups.get(i);
            evalGroupIds[i] = c.evalGroupId;
         }

         // this sucks for efficiency -AZ
         List<EvalEvaluation> l = dao.getEvaluationsByEvalGroups( evalGroupIds, true, true, true, 0, 0 ); // only get active for users
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

   public List<EvalAssignHierarchy> setEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds, boolean appendMode) {

      // get the evaluation
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);

      if (evalGroupIds == null) {
         evalGroupIds = new String[] {};
      }
      if (nodeIds == null) {
         nodeIds = new String[] {};
      }

      if (evalGroupIds.length == 0 && nodeIds.length == 0) {
         throw new IllegalArgumentException("Cannot assign an evaluation to 0 nodes and 0 groups, you must pass in at least one node id or group id");
      }

      // check if this evaluation can be modified
      String userId = commonLogic.getCurrentUserId();
      if (securityChecks.checkCreateAssignGroup(userId, eval)) {

         // first we have to get all the assigned hierarchy nodes for this eval
         Set<String> nodeIdsSet = new HashSet<String>();
         Set<String> currentNodeIds = new HashSet<String>();

         List<EvalAssignHierarchy> currentAssignHierarchies = evaluationService.getAssignHierarchyByEval(evaluationId);
         for (EvalAssignHierarchy assignHierarchy : currentAssignHierarchies) {
            currentNodeIds.add(assignHierarchy.getNodeId());
         }

         for (int i = 0; i < nodeIds.length; i++) {
            nodeIdsSet.add(nodeIds[i]);
         }

         if (! appendMode) {
            Set<String> selectedNodeIds = new HashSet<String>(nodeIdsSet);
            Set<String> existingNodeIds = new HashSet<String>(currentNodeIds);
            existingNodeIds.removeAll(selectedNodeIds);
            // now remove all the nodes remaining in the current set
            if (existingNodeIds.size() > 0) {
               Long[] removeHierarchyIds = new Long[existingNodeIds.size()];
               int counter = 0;
               for (EvalAssignHierarchy assignHierarchy : currentAssignHierarchies) {
                  if (existingNodeIds.contains(assignHierarchy.getNodeId())) {
                     removeHierarchyIds[counter] = assignHierarchy.getId();
                     counter++;
                  }
               }
               deleteAssignHierarchyNodesById(removeHierarchyIds);
            }            
         }

         // then remove the duplicates so we end up with the filtered list to only new ones
         nodeIdsSet.removeAll(currentNodeIds);
         nodeIds = nodeIdsSet.toArray(new String[] {});

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
         Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
         List<EvalAssignGroup> currentGroups = groupsMap.get(evaluationId);
         for (EvalAssignGroup evalAssignGroup : currentGroups) {
            currentEvalGroupIds.add(evalAssignGroup.getEvalGroupId());
         }

         for (int i = 0; i < evalGroupIds.length; i++) {
            evalGroupIdsSet.add(evalGroupIds[i]);
         }

         if (! appendMode) {
            Set<String> selectedGroupIds = new HashSet<String>(evalGroupIdsSet);
            Set<String> existingGroupIds = new HashSet<String>();
            for (EvalAssignGroup assignGroup : currentGroups) {
               if (assignGroup.getNodeId() == null) {
                  existingGroupIds.add(assignGroup.getEvalGroupId());
               }
            }
            existingGroupIds.removeAll(selectedGroupIds);
            // now remove all the groups remaining in the existing set
            if (existingGroupIds.size() > 0) {
               Set<EvalAssignGroup> removeAssignGroups = new HashSet<EvalAssignGroup>();
               for (EvalAssignGroup assignGroup : currentGroups) {
                  if (existingGroupIds.contains(assignGroup.getEvalGroupId())) {
                     removeAssignGroups.add(assignGroup);
                  }
               }
               dao.deleteSet(removeAssignGroups);
            }
         }

         // next we need to expand all the assigned hierarchy nodes into a massive set of eval assign groups
         Set<EvalHierarchyNode> nodes = hierarchyLogic.getNodesByIds(nodeIdsSet.toArray(new String[] {}));
         // expand the actual new set of nodes into a complete list of nodes including children
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);
         allNodeIds.addAll(currentNodeIds);
         Map<String, Set<String>> allEvalGroupIds = hierarchyLogic.getEvalGroupsForNodes( allNodeIds.toArray(new String[] {}) );

         // now eliminate the evalgroupids from the evalGroupIds array which happen to be contained in the nodes,
         // this leaves us with only the group ids which are not contained in the nodes which are already assigned
         for (Set<String> egIds : allEvalGroupIds.values()) {
            evalGroupIdsSet.removeAll(egIds);
         }

         // then remove the eval groups ids which are already assigned to this eval so we only have new ones
         evalGroupIdsSet.removeAll(currentEvalGroupIds);
         evalGroupIds = evalGroupIdsSet.toArray(new String[] {});

         // now we need to create all the persistent group assignment objects for the new groups
         Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
         groupAssignments.addAll( makeAssignGroups(eval, userId, evalGroupIdsSet, null) );

         // finally we add in the groups for all the new expanded assign groups for the expanded nodes set
         for (String nodeId : allNodeIds) {
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
      String userId = commonLogic.getCurrentUserId();
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
         boolean userAdmin = commonLogic.isUserAdmin(userId);
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

      // cleanup for XSS scripting and strings
      emailTemplate.setMessage( commonLogic.cleanupUserStrings(emailTemplate.getMessage()) );
      emailTemplate.setSubject( commonLogic.cleanupUserStrings(emailTemplate.getSubject()) );

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
            if (eval.getInstructorViewResults()) {
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
         Boolean studViewResults = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
         if (studViewResults == null) {
            if (eval.getStudentViewResults()) {
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


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationSetupService#assignEmailTemplate(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
    */
   public void assignEmailTemplate(Long emailTemplateId, Long evaluationId, String emailTemplateTypeConstant, String userId) {
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      if (! securityChecks.canUserControlEvaluation(userId, eval) ) {
         throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+eval.getId()+") without permissions");
      }

      boolean clearAssociation = false;
      if (emailTemplateId == null) {
         if (emailTemplateTypeConstant == null) {
            throw new IllegalArgumentException("emailTemplateTypeConstant cannot be null when clearing association");
         }
         clearAssociation = true;
      } else {
         EvalEmailTemplate emailTemplate = evaluationService.getEmailTemplate(emailTemplateId);
         // assign to the evaluation
         if (emailTemplate.getDefaultType() == null) {
            // only assign non-default templates of the right type
            if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplate.getType())) {
               eval.setAvailableEmailTemplate(emailTemplate);
            } else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplate.getType())) {
               eval.setReminderEmailTemplate(emailTemplate);
            }
            dao.save(eval);
         } else {
            emailTemplateTypeConstant = emailTemplate.getType();
            clearAssociation = true;
         }
      }
      
      if (clearAssociation
            && emailTemplateTypeConstant != null) {
         Long checkEmailTemplateId = null;
         String evalTemplateType = null;
         if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
            if (eval.getAvailableEmailTemplate() != null) {
               checkEmailTemplateId = eval.getAvailableEmailTemplate().getId();
               evalTemplateType = "availableEmailTemplate";
            }
            eval.setAvailableEmailTemplate(null);
         } else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
            if (eval.getReminderEmailTemplate() != null) {
               checkEmailTemplateId = eval.getReminderEmailTemplate().getId();
               evalTemplateType = "reminderEmailTemplate";
            }
            eval.setReminderEmailTemplate(null);
         }
         dao.save(eval);

         // also remove the unused template if possible
         if (checkEmailTemplateId != null) {
            EvalEmailTemplate checkTemplate = evaluationService.getEmailTemplate(checkEmailTemplateId);
            if (checkTemplate != null
                  && checkTemplate.getDefaultType() == null) {
               // only remove non-default templates
               int evalsUsingTemplate = dao.countByProperties(EvalEvaluation.class, 
                     new String[] {evalTemplateType + ".id"}, 
                     new Object[] {checkEmailTemplateId}) ;
               if ( evalsUsingTemplate <= 0 ) {
                  // template was only used in this evaluation
                  dao.delete(checkTemplate);
               }
            }
         }
      }
   }


}
