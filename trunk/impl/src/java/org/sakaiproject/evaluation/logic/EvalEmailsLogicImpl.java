/**
 * $Id$
 * $URL$
 * EvalEmailsLogicImpl.java - evaluation - Dec 29, 2006 10:07:31 AM - azeckoski
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.constant.EvalEmailConstants;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TextTemplateLogicUtils;

/**
 * EvalEmailsLogic implementation,
 * this is a BACKUP service and should only depend on LOWER and BOTTOM services
 * (and maybe other BACKUP services if necessary)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImpl implements EvalEmailsLogic {

   private static Log log = LogFactory.getLog(EvalEmailsLogicImpl.class);

   // Event names cannot be over 32 chars long              // max-32:12345678901234567890123456789012
   protected final String EVENT_EMAIL_CREATED =                      "eval.email.eval.created";
   protected final String EVENT_EMAIL_AVAILABLE =                    "eval.email.eval.available";
   protected final String EVENT_EMAIL_GROUP_AVAILABLE =              "eval.email.evalgroup.available";
   protected final String EVENT_EMAIL_REMINDER =                     "eval.email.eval.reminders";
   protected final String EVENT_EMAIL_RESULTS =                      "eval.email.eval.results";


   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }


   // INIT method
   public void init() {
      log.debug("Init");
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalCreatedNotifications(java.lang.Long, boolean)
    */
   public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner) {
      log.debug("evaluationId: " + evaluationId + ", includeOwner: " + includeOwner);

      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      String from = getFromEmailOrFail(eval);
      EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_CREATED, evaluationId);

      // append opt-in, opt-out, and/or add questions messages followed by footer
      StringBuilder sb = new StringBuilder();
      int addItems = ((Integer) settings.get(EvalSettings.ADMIN_ADD_ITEMS_NUMBER)).intValue();
      sb.append(emailTemplate.getMessage());
      if (! eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED) || (addItems > 0)) {
         if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN)) {
            // if eval is opt-in notify instructors that they may opt in
            sb.append(EvalEmailConstants.EMAIL_CREATED_OPT_IN_TEXT);
         } else if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_OUT)) {
            // if eval is opt-out notify instructors that they may opt out
            sb.append(EvalEmailConstants.EMAIL_CREATED_OPT_OUT_TEXT);
         }
         if (addItems > 0) {
            // if eval allows instructors to add questions notify instructors they may add questions
            sb.append(EvalEmailConstants.EMAIL_CREATED_ADD_ITEMS_TEXT);
         }
      }
      sb.append(EvalEmailConstants.EMAIL_CREATED_DEFAULT_TEXT_FOOTER);
      String message = sb.toString();

      // get the associated groups for this evaluation
      Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvaluationGroups(new Long[] { evaluationId }, true);

      // only one possible map key so we can assume evaluationId
      List<EvalGroup> groups = evalGroups.get(evaluationId);
      if (log.isDebugEnabled()) {
         log.debug("Found " + groups.size() + " groups for new evaluation: " + evaluationId);
      }

      List<String> sentEmails = new ArrayList<String>();
      // loop through contexts and send emails to correct users in each evalGroupId
      for (int i = 0; i < groups.size(); i++) {
         EvalGroup group = (EvalGroup) groups.get(i);
         if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
            continue; // skip processing for invalid groups
         }

         Set<String> userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId,
               EvalConstants.PERM_BE_EVALUATED);
         // add in the owner or remove them based on the setting
         if (includeOwner) {
            userIdsSet.add(eval.getOwner());
         } else {
            if (userIdsSet.contains(eval.getOwner())) {
               userIdsSet.remove(eval.getOwner());
            }
         }

         // skip ahead if there is no one to send to
         if (userIdsSet.size() == 0) continue;

         // turn the set into an array
         String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
         if (log.isDebugEnabled()) {
            log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                  + EvalConstants.EMAIL_TEMPLATE_CREATED + " notification to for new evaluation ("
                  + evaluationId + ") and evalGroupId (" + group.evalGroupId + ")");
         }

         // replace the text of the template with real values
         Map<String, String> replacementValues = new HashMap<String, String>();
         replacementValues.put("HelpdeskEmail", from);
         message = makeEmailMessage(message, eval, group, replacementValues);
         String subject = makeEmailMessage(emailTemplate.getSubject(), eval, group, replacementValues);

         // send the actual emails for this evalGroupId
         String[] emailAddresses = externalLogic.sendEmailsToUsers(from, toUserIds, subject, message, true);
         log.info("Sent evaluation created message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
         // store sent emails to return
         for (int j = 0; j < emailAddresses.length; j++) {
            sentEmails.add(emailAddresses[j]);            
         }
         externalLogic.registerEntityEvent(EVENT_EMAIL_CREATED, eval);
      }

      return (String[]) sentEmails.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableNotifications(java.lang.Long, boolean)
    */
   public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
      log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees);

      Set<String> userIdsSet = null;
      String message = null;
      boolean studentNotification = true;

      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      String from = getFromEmailOrFail(eval);
      EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, evaluationId);
      // get the instructor opt-in email template
      EvalEmailTemplate emailOptInTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, null);

      // get the associated assign groups for this evaluation
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = 
         evaluationService.getEvaluationAssignGroups(new Long[] { evaluationId }, true);
      List<EvalAssignGroup> assignGroups = evalAssignGroups.get(evaluationId);

      List<String> sentEmails = new ArrayList<String>();
      // loop through groups and send emails to correct users group
      for (int i = 0; i < assignGroups.size(); i++) {
         EvalAssignGroup assignGroup = assignGroups.get(i);
         EvalGroup group = externalLogic.makeEvalGroupObject(assignGroup.getEvalGroupId());
         if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
            //notify students
            userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId,
                  EvalConstants.PERM_TAKE_EVALUATION);
            studentNotification = true;
         } else {
            //instructor may opt-in or opt-out
            if (assignGroup.getInstructorApproval().booleanValue()) {
               //instructor has opted-in, notify students
               userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId,
                     EvalConstants.PERM_TAKE_EVALUATION);
               studentNotification = true;
            } else {
               if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN) && includeEvaluatees) {
                  // instructor has not opted-in, notify instructors
                  userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId,
                        EvalConstants.PERM_BE_EVALUATED);
                  studentNotification = false;
               } else {
                  userIdsSet = new HashSet<String>();
               }
            }
         }

         // skip ahead if there is no one to send to
         if (userIdsSet.size() == 0) continue;

         // turn the set into an array
         String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});

         if (log.isDebugEnabled()) {
            log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                  + EvalConstants.EMAIL_TEMPLATE_CREATED + " notification to for available evaluation ("
                  + evaluationId + ") and group (" + group.evalGroupId + ")");
         }

         // replace the text of the template with real values
         Map<String, String> replacementValues = new HashMap<String, String>();
         replacementValues.put("HelpdeskEmail", from);

         // choose from 2 templates
         EvalEmailTemplate currentTemplate = emailTemplate;
         if (! studentNotification) {
            currentTemplate = emailOptInTemplate;
         }
         message = makeEmailMessage(currentTemplate.getMessage(), eval, group, replacementValues);
         String subject = makeEmailMessage(currentTemplate.getSubject(), eval, group, replacementValues);

         // send the actual emails for this evalGroupId
         String[] emailAddresses = externalLogic.sendEmailsToUsers(from, toUserIds, subject, message, true);
         log.info("Sent evaluation available message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
         // store sent emails to return
         for (int j = 0; j < emailAddresses.length; j++) {
            sentEmails.add(emailAddresses[j]);            
         }
         externalLogic.registerEntityEvent(EVENT_EMAIL_AVAILABLE, eval);
      }

      return (String[]) sentEmails.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)
    */
   public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId) {

      List<String> sentEmails = new ArrayList<String>();

      // get group
      EvalGroup group = externalLogic.makeEvalGroupObject(evalGroupId);
      // only process valid groups
      if ( EvalConstants.GROUP_TYPE_INVALID.equals(group.type) ) {
         throw new IllegalArgumentException("Invalid group type for group with id (" + evalGroupId + "), cannot send available emails");
      }

      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      String from = getFromEmailOrFail(eval);
      EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, evaluationId);

      //get student ids
      Set<String> userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId,
            EvalConstants.PERM_TAKE_EVALUATION);
      if (userIdsSet.size() > 0) {
         String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});

         // replace the text of the template with real values
         Map<String, String> replacementValues = new HashMap<String, String>();
         replacementValues.put("HelpdeskEmail", from);
         String message = makeEmailMessage(emailTemplate.getMessage(), eval, group, replacementValues);
         String subject = makeEmailMessage(emailTemplate.getSubject(), eval, group, replacementValues);

         // send the actual emails for this evalGroupId
         String[] emailAddresses = externalLogic.sendEmailsToUsers(from, toUserIds, subject, message, true);
         log.info("Sent evaluation available group message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
         // store sent emails to return
         for (int j = 0; j < emailAddresses.length; j++) {
            sentEmails.add(emailAddresses[j]);            
         }
         externalLogic.registerEntityEvent(EVENT_EMAIL_GROUP_AVAILABLE, eval);
      }

      return (String[]) sentEmails.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalReminderNotifications(java.lang.Long, java.lang.String)
    */
   public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
      log.debug("evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);
      EvalUtils.validateEmailIncludeConstant(includeConstant);

      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      String from = getFromEmailOrFail(eval);
      EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_REMINDER, evaluationId);

      // get the associated eval groups for this evaluation
      // NOTE: this only returns the groups that should get emails, there is no need to do an additional check
      // to see if the instructor has opted in in this case -AZ
      Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvaluationGroups(new Long[] { evaluationId }, false);

      // only one possible map key so we can assume evaluationId
      List<EvalGroup> groups = evalGroupIds.get(evaluationId);
      log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);

      List<String> sentEmails = new ArrayList<String>();
      // loop through groups and send emails to correct users in each
      for (int i = 0; i < groups.size(); i++) {
         EvalGroup group = (EvalGroup) groups.get(i);
         if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
            continue; // skip processing for invalid groups
         }
         String evalGroupId = group.evalGroupId;

         Set<String> userIdsSet = evaluationService.getUserIdsTakingEvalInGroup(evaluationId, evalGroupId, includeConstant);
         if (userIdsSet.size() > 0) {
            // turn the set into an array
            String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
            log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                  + EvalConstants.EMAIL_TEMPLATE_REMINDER + " notification to for available evaluation ("
                  + evaluationId + ") and group (" + group.evalGroupId + ")");

            // replace the text of the template with real values
            Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
            String message = makeEmailMessage(emailTemplate.getMessage(), eval, group, replacementValues);
            String subject = makeEmailMessage(emailTemplate.getSubject(), eval, group, replacementValues);

            // send the actual emails for this evalGroupId
            String[] emailAddresses = externalLogic.sendEmailsToUsers(from, toUserIds, subject, message, true);
            log.info("Sent evaluation reminder message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
               sentEmails.add(emailAddresses[j]);            
            }
            externalLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);
         }
      }

      return (String[]) sentEmails.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalResultsNotifications(java.lang.Long, boolean, boolean, java.lang.String)
    */
   public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees,
         boolean includeAdmins, String jobType) {
      log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees
            + ", includeAdmins: " + includeAdmins);

      /*TODO deprecated?
       if(EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant)) {
       }
       boolean includeEvaluatees = true;
       if (includeEvaluatees) {
       // TODO Not done yet
       log.error("includeEvaluatees Not implemented");
       }
       */

      EvalEvaluation eval = getEvaluationOrFail(evaluationId);
      String from = getFromEmailOrFail(eval);
      EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_RESULTS, evaluationId);

      // get the associated eval groups for this evaluation
      Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvaluationGroups(new Long[] { evaluationId }, false);
      // only one possible map key so we can assume evaluationId
      List<EvalGroup> groups = evalGroupIds.get(evaluationId);
      if (log.isDebugEnabled()) log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
      Map<String, EvalGroup> groupsMap = new HashMap<String, EvalGroup>();
      for (EvalGroup evalGroup : groups) {
         groupsMap.put(evalGroup.evalGroupId, evalGroup);
      }

      // get the associated eval assign groups for this evaluation
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getEvaluationAssignGroups(new Long[] { evaluationId }, false);
      // only one possible map key so we can assume evaluationId
      List<EvalAssignGroup> assignGroups = evalAssignGroups.get(evaluationId);
      if (log.isDebugEnabled()) log.debug("Found " + assignGroups.size() + " assign groups for available evaluation: " + evaluationId);

      List<String> sentEmails = new ArrayList<String>();
      // loop through groups and send emails to correct users in each evalGroupId
      for (int i = 0; i < assignGroups.size(); i++) {
         EvalAssignGroup evalAssignGroup = assignGroups.get(i);
         String evalGroupId = evalAssignGroup.getEvalGroupId();
         EvalGroup group = groupsMap.get(evalGroupId);
         if ( group == null ||
               EvalConstants.GROUP_TYPE_INVALID.equals(group.type) ) {
            log.warn("Invalid group returned for groupId ("+evalGroupId+"), could not send results notifications");
            continue;
         }

         /*
          * Notification of results may occur on separate dates for owner,
          * instructors, and students. Job type is used to distinguish the
          * intended recipient group.
          */

         //always send results email to eval.getOwner()
         Set<String> userIdsSet = new HashSet<String>();
         if (jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE)) {
            userIdsSet.add(eval.getOwner());
         }

         //if results are not private
         if (! EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) ) {
            //at present, includeAdmins is always true
            if (includeAdmins && 
                  evalAssignGroup.getInstructorsViewResults().booleanValue() &&
                  jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS)) {
               userIdsSet.addAll(externalLogic.getUserIdsForEvalGroup(evalGroupId,
                     EvalConstants.PERM_BE_EVALUATED));
            }

            //at present, includeEvaluatees is always true
            if (includeEvaluatees && 
                  evalAssignGroup.getStudentsViewResults().booleanValue() &&
                  jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS)) {
               userIdsSet.addAll(externalLogic.getUserIdsForEvalGroup(evalGroupId,
                     EvalConstants.PERM_TAKE_EVALUATION));
            }
         }

         if (userIdsSet.size() > 0) {
            // turn the set into an array
            String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
            log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                  + EvalConstants.EMAIL_TEMPLATE_RESULTS + " notification to for available evaluation ("
                  + evaluationId + ") and group (" + evalGroupId + ")");

            // replace the text of the template with real values
            Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
            String message = makeEmailMessage(emailTemplate.getMessage(), eval, group, replacementValues);
            String subject = makeEmailMessage(emailTemplate.getSubject(), eval, group, replacementValues);

            // send the actual emails for this evalGroupId
            String[] emailAddresses = externalLogic.sendEmailsToUsers(from, toUserIds, subject, message, true);
            log.info("Sent evaluation results message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
               sentEmails.add(emailAddresses[j]);            
            }
            externalLogic.registerEntityEvent(EVENT_EMAIL_RESULTS, eval);
         }
      }
      return (String[]) sentEmails.toArray(new String[] {});
   }



   // INTERNAL METHODS


   /**
    * INTERNAL METHOD<br/>
    * Builds the email message from a template and a bunch of variables
    * (passed in and otherwise)
    * 
    * @param messageTemplate
    * @param eval
    * @param group
    * @param replacementValues a map of String -> String representing $keys in the template to replace with text values
    * @return the processed message template with replacements and logic handled
    */
   public String makeEmailMessage(String messageTemplate, EvalEvaluation eval, EvalGroup group,
         Map<String, String> replacementValues) {
      // replace the text of the template with real values
      if (replacementValues == null) {
         replacementValues = new HashMap<String, String>();
      }
      replacementValues.put("EvalTitle", eval.getTitle());

      // use a date which is related to the current users locale
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, 
            externalLogic.getUserLocale(externalLogic.getCurrentUserId()));

      replacementValues.put("EvalStartDate", df.format(eval.getStartDate()));
      replacementValues.put("EvalDueDate", df.format(eval.getDueDate()));
      replacementValues.put("EvalResultsDate", df.format(eval.getViewDate()));
      replacementValues.put("EvalGroupTitle", group.title);

      // generate URLs to the evaluation
      String evalEntityURL = null;
      if (group != null && group.evalGroupId != null) {
         // get the URL directly to the evaluation with group context included
         Long assignGroupId = evaluationService.getAssignGroupId(eval.getId(), group.evalGroupId);
         EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(assignGroupId);
         if (assignGroup != null) {
            evalEntityURL = externalLogic.getEntityURL(assignGroup);
         }
      }

      if (evalEntityURL == null) {
         // just get the URL to the evaluation without group context
         evalEntityURL = externalLogic.getEntityURL(eval);
      }

      // all URLs are identical because the user permissions determine access uniquely
      replacementValues.put("URLtoTakeEval", evalEntityURL);
      replacementValues.put("URLtoAddItems", evalEntityURL);
      replacementValues.put("URLtoOptIn", evalEntityURL);
      replacementValues.put("URLtoOptOut", evalEntityURL);
      replacementValues.put("URLtoViewResults", evalEntityURL);
      replacementValues.put("URLtoSystem", externalLogic.getServerUrl());

      return TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
   }

   /**
    * INTERNAL METHOD<br/>
    * Get an email template by type and evaluationId or fail
    * @param typeConstant an EvalConstants.EMAIL_TEMPLATE constant
    * @param evaluationId unique id of an eval or null to only get the default template
    * @return an email template
    * @throws IllegalStateException if no email template can be found
    */
   public EvalEmailTemplate getEmailTemplateOrFail(String typeConstant, Long evaluationId) {
      EvalEmailTemplate emailTemplate = null;
      if (evaluationId != null &&
            ( EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(typeConstant) ||
                  EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(typeConstant) ) ) {
         // get the template from the evaluation itself
         EvalEmailTemplate evalEmailTemplate = evaluationService.getEmailTemplate(evaluationId, typeConstant);
         if (evalEmailTemplate != null) {
            emailTemplate = evalEmailTemplate;
         }
      }
      if (emailTemplate == null) {
         // get the default email template
         try {
            emailTemplate = evaluationService.getDefaultEmailTemplate(typeConstant);
         } catch (RuntimeException e) {
            log.error("Failed to get default email template ("+typeConstant+"): " + e.getMessage());
            emailTemplate = null;
         }
      }
      if (emailTemplate == null) {
         throw new IllegalArgumentException("Cannot find email template default or in eval ("+evaluationId+"): " + typeConstant);
      }
      return emailTemplate;
   }

   /**
    * INTERNAL METHOD<br/>
    * Get the email address from system settings or the evaluation
    * @param eval
    * @return an email address
    * @throws IllegalStateException if a from address cannot be found
    */
   public String getFromEmailOrFail(EvalEvaluation eval) {
      String from = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
      if (eval.getReminderFromEmail() != null && ! "".equals(eval.getReminderFromEmail())) {
         from = eval.getReminderFromEmail();
      }
      if (from == null) {
         throw new IllegalStateException("Could not get a from email address from system settings or the evaluation");
      }
      return from;
   }

   /**
    * INTERNAL METHOD<br/>
    * Gets the evaluation or throws exception,
    * reduce code duplication
    * @param evaluationId
    * @return eval for this id
    * @throws IllegalArgumentException if no eval exists
    */
   protected EvalEvaluation getEvaluationOrFail(Long evaluationId) {
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with id: " + evaluationId);
      }
      return eval;
   }

}
