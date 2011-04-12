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
import org.sakaiproject.evaluation.logic.entity.EvalReportsEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalReminderStatus;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.ArrayUtils;
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


    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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

        // get the associated groups for this evaluation
        Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvalGroupsForEval(new Long[] { evaluationId }, true, null);

        // only one possible map key so we can assume evaluationId
        List<EvalGroup> groups = evalGroups.get(evaluationId);
        if (log.isDebugEnabled()) {
            log.debug("Found " + groups.size() + " groups for new evaluation: " + evaluationId);
        }

        List<String> sentEmails = new ArrayList<String>();
        int emailsSentCt=0;
        // loop through contexts and send emails to correct users in each evalGroupId
        for (int i = 0; i < groups.size(); i++) {
            EvalGroup group = (EvalGroup) groups.get(i);
            if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                continue; // skip processing for invalid groups
            }

            List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                    new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
            Set<String> instructors = EvalUtils.getUserIdsFromUserAssignments(userAssignments);

            // add in the owner or remove them based on the setting
            if (includeOwner) {
                instructors.add(eval.getOwner());
            } else {
                if (instructors.contains(eval.getOwner())) {
                    instructors.remove(eval.getOwner());
                }
            }

            // skip ahead if there is no one to send to
            if (instructors.size() == 0) continue;

            // turn the set into an array
            String[] toUserIds = (String[]) instructors.toArray(new String[] {});
            if (log.isDebugEnabled()) {
                log.debug("Found " + toUserIds.length + " users (" + ArrayUtils.arrayToString(toUserIds) + ") to send "
                        + EvalConstants.EMAIL_TEMPLATE_CREATED + " notification to for new evaluation ("
                        + evaluationId + ") and evalGroupId (" + group.evalGroupId + ")");
            }

            // replace the text of the template with real values
            EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, group);

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation created message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            emailsSentCt++;
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_CREATED, eval);
        }
        
        // send email to admin that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
        	Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
        	replacementValues.put("EvalTitle", eval.getTitle());
        	Integer iEmailsSentCt = Integer.valueOf(emailsSentCt);
        	replacementValues.put("NumEmailsSent", iEmailsSentCt.toString());
        	replacementValues.put("JobType", EvalConstants.JOB_TYPE_CREATED.substring(9));
        	sendEmailJobCompleted(eval.getId(), replacementValues);	
        }
        
        return (String[]) sentEmails.toArray(new String[] {});
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableNotifications(java.lang.Long, boolean)
     */
    public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
        log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees);

        Set<String> userIdsSet = null;
        boolean studentNotification = true;

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String from = getFromEmailOrFail(eval);
        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, evaluationId);
        // get the instructor opt-in email template
        EvalEmailTemplate emailOptInTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, null);

        // get the associated assign groups for this evaluation
        Map<Long, List<EvalAssignGroup>> evalAssignGroups = 
            evaluationService.getAssignGroupsForEvals(new Long[] { evaluationId }, true, null);
        List<EvalAssignGroup> assignGroups = evalAssignGroups.get(evaluationId);

        List<String> sentEmails = new ArrayList<String>();
        int emailsSentCt=0;
        // loop through groups and send emails to correct users group
        for (int i = 0; i < assignGroups.size(); i++) {
            EvalAssignGroup assignGroup = assignGroups.get(i);
            
            if(! commonLogic.isEvalGroupPublished(assignGroup.getEvalGroupId())) {
                continue;
            }
            
            EvalGroup group = commonLogic.makeEvalGroupObject(assignGroup.getEvalGroupId());
            if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
                // notify eval takers
                List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                        new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                userIdsSet = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
                studentNotification = true;
            } else {
                //instructor may opt-in or opt-out
                if (assignGroup.getInstructorApproval().booleanValue()) {
                    // instructor has opted-in, notify students
                    List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                            new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                    userIdsSet = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
                    studentNotification = true;
                } else {
                    if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN) && includeEvaluatees) {
                        // instructor has not opted-in, notify instructors
                        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                                new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
                        userIdsSet = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
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
                log.debug("Found " + toUserIds.length + " users (" + ArrayUtils.arrayToString(toUserIds) + ") to send "
                        + EvalConstants.EMAIL_TEMPLATE_CREATED + " notification to for available evaluation ("
                        + evaluationId + ") and group (" + group.evalGroupId + ")");
            }

            // choose from 2 templates
            EvalEmailTemplate currentTemplate = emailTemplate;
            if (! studentNotification) {
                currentTemplate = emailOptInTemplate;
            }
            EvalEmailMessage em = makeEmailMessage(currentTemplate.getMessage(), currentTemplate.getSubject(), eval, group);

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation available message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            emailsSentCt++;
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_AVAILABLE, eval);
        }
        
        // send email to admin that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
        	Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
        	replacementValues.put("EvalTitle", eval.getTitle());
        	Integer iEmailsSentCt = Integer.valueOf(emailsSentCt);
        	replacementValues.put("NumEmailsSent", iEmailsSentCt.toString());
        	replacementValues.put("JobType", EvalConstants.JOB_TYPE_ACTIVE.substring(9));
        	sendEmailJobCompleted(eval.getId(), replacementValues);	
        }

        return (String[]) sentEmails.toArray(new String[] {});
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)
     */
    public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId) {

        List<String> sentEmails = new ArrayList<String>();
        int emailsSentCt=0;
        if(! commonLogic.isEvalGroupPublished(evalGroupId)) {
            return new String[] {};
        }

        // get group
        EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
        // only process valid groups
        if ( EvalConstants.GROUP_TYPE_INVALID.equals(group.type) ) {
            throw new IllegalArgumentException("Invalid group type for group with id (" + evalGroupId + "), cannot send available emails");
        }

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String from = getFromEmailOrFail(eval);
        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, evaluationId);

        // get evaluator ids
        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Set<String> userIdsSet = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
        if (userIdsSet.size() > 0) {
            String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});

            EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, group);

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation available group message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            emailsSentCt++;
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_GROUP_AVAILABLE, eval);
        }
        
        // send email to admin that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
        	Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
        	replacementValues.put("EvalTitle", eval.getTitle());
        	Integer iEmailsSentCt = Integer.valueOf(emailsSentCt);
        	replacementValues.put("NumEmailsSent", iEmailsSentCt.toString());
        	replacementValues.put("JobType", EvalConstants.JOB_TYPE_ACTIVE.substring(9));
        	sendEmailJobCompleted(eval.getId(), replacementValues);	
        }
        
        return (String[]) sentEmails.toArray(new String[] {});
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalReminderNotifications(java.lang.Long, java.lang.String)
     */
    public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
        log.debug("evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);
        
        boolean updateReminderStatus = (Boolean) settings.get(EvalSettings.ENABLE_REMINDER_STATUS);
        
        EvalUtils.validateEmailIncludeConstant(includeConstant);

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String from = getFromEmailOrFail(eval);
        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_REMINDER, evaluationId);

        // get the associated eval groups for this evaluation
        // NOTE: this only returns the groups that should get emails, there is no need to do an additional check
        // to see if the instructor has opted in in this case -AZ
        Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvalGroupsForEval(new Long[] { evaluationId }, false, null);

        // handle recovery of interrupted email sending
        EvalReminderStatus reminderStatus = eval.getCurrentReminderStatus();
        boolean reminderGroupFound = false;
        if (reminderStatus != null) {
            log.info("Reminder recovery processing for eval ("+evaluationId+") will attempt to continue from: "+reminderStatus);
        }

        // only one possible map key so we can assume evaluationId
        List<EvalGroup> groups = evalGroupIds.get(evaluationId);
        log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);

        List<String> sentEmails = new ArrayList<String>();
        int emailsSentCt=0;
        // loop through groups and send emails to correct users in each
        for (int i = 0; i < groups.size(); i++) {
            EvalGroup group = (EvalGroup) groups.get(i);
            if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                continue; // skip processing for invalid groups
            }
            String evalGroupId = group.evalGroupId;
            
            // update the reminder status
            if (updateReminderStatus) {   
            	// skip courses until we reach the one that we stopped on before when email was interrupted
            	if (reminderStatus != null) {
            		if (reminderStatus.currentEvalGroupId.equals(evalGroupId)) {
            			reminderGroupFound = true;
            			log.info("Reminder processing for eval ("+evaluationId+"), found last processed group ("+evalGroupId+") at position "+(i+1)+" of "+groups.size());
            		}
            		if (reminderGroupFound) {
            			reminderStatus = null;
            		}
            		// skip this group
            		if (log.isDebugEnabled()) {
            			log.debug("Reminder status ("+reminderStatus+") for eval ("+evaluationId+"), skipping group "+evalGroupId);
            		}
            		continue;
            	}
            }

            if (! commonLogic.isEvalGroupPublished(evalGroupId)) {
                continue; // skip processing for groups that are not published?
            }

            String[] limitGroupIds = null;
            if (evalGroupId != null) {
                limitGroupIds = new String[] {evalGroupId};
            }

            List<EvalAssignUser> participants = evaluationService.getParticipantsForEval(evaluationId, null, limitGroupIds, null, null, includeConstant, null);
            Set<String> userIdsSet = EvalUtils.getUserIdsFromUserAssignments(participants);

            if (userIdsSet.size() > 0) {
                // turn the set into an array
                String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
                if (log.isDebugEnabled()) {
                    log.debug("Found " + toUserIds.length + " users (" + ArrayUtils.arrayToString(toUserIds) + ") to send "
                            + EvalConstants.EMAIL_TEMPLATE_REMINDER + " notification to for available evaluation ("
                            + evaluationId + ") and group (" + group.evalGroupId + ")");
                }

                EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, group);

                // send the actual emails for this evalGroupId
                String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
                log.info("Sent evaluation reminder message for eval ("+evaluationId+") and group ("+group.evalGroupId+") to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
                emailsSentCt++;
                // store sent emails to return
                for (int j = 0; j < emailAddresses.length; j++) {
                    sentEmails.add(emailAddresses[j]);            
                }
            }
            // update the reminder status
            if (updateReminderStatus) {
            	evaluationService.updateEvaluationReminderStatus(evaluationId, new EvalReminderStatus(groups.size(), i+1, evalGroupId));
            }
        }
        // set reminder status back to idle
        if (updateReminderStatus) {
        	evaluationService.updateEvaluationReminderStatus(evaluationId, null);
        }
        commonLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);

        // send email to helpdeskEmail that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
        	Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
        	replacementValues.put("EvalTitle", eval.getTitle());
        	Integer iEmailsSentCt = Integer.valueOf(emailsSentCt);
        	replacementValues.put("NumEmailsSent", iEmailsSentCt.toString());
        	replacementValues.put("JobType", EvalConstants.JOB_TYPE_REMINDER.substring(9));
        	sendEmailJobCompleted(eval.getId(), replacementValues);	
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
        Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvalGroupsForEval(new Long[] { evaluationId }, false, null);
        // only one possible map key so we can assume evaluationId
        List<EvalGroup> groups = evalGroupIds.get(evaluationId);
        if (log.isDebugEnabled()) log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
        Map<String, EvalGroup> groupsMap = new HashMap<String, EvalGroup>();
        for (EvalGroup evalGroup : groups) {
            groupsMap.put(evalGroup.evalGroupId, evalGroup);
        }

        // get the associated eval assign groups for this evaluation
        Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getAssignGroupsForEvals(new Long[] { evaluationId }, false, null);
        // only one possible map key so we can assume evaluationId
        List<EvalAssignGroup> assignGroups = evalAssignGroups.get(evaluationId);
        if (log.isDebugEnabled()) log.debug("Found " + assignGroups.size() + " assign groups for available evaluation: " + evaluationId);

        List<String> sentEmails = new ArrayList<String>();
        int emailsSentCt=0;
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
                    List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                            new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
                    Set<String> userIds = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
                    userIdsSet.addAll(userIds);
                }

                //at present, includeEvaluatees is always true
                if (includeEvaluatees && 
                        evalAssignGroup.getStudentsViewResults().booleanValue() &&
                        jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS)) {
                    List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                            new String[] {group.evalGroupId}, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                    Set<String> userIds = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
                    userIdsSet.addAll(userIds);
                }
            }

            if (userIdsSet.size() > 0) {
                // turn the set into an array
                String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
                log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                        + EvalConstants.EMAIL_TEMPLATE_RESULTS + " notification to for available evaluation ("
                        + evaluationId + ") and group (" + evalGroupId + ")");

                EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, group);

                // send the actual emails for this evalGroupId
                String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
                log.info("Sent evaluation results message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
                emailsSentCt++;
                // store sent emails to return
                for (int j = 0; j < emailAddresses.length; j++) {
                    sentEmails.add(emailAddresses[j]);            
                }
                commonLogic.registerEntityEvent(EVENT_EMAIL_RESULTS, eval);
            }
        }
        
        // send email to admin that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
        	Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
        	replacementValues.put("EvalTitle", eval.getTitle());
        	Integer iEmailsSentCt = Integer.valueOf(emailsSentCt);
        	replacementValues.put("NumEmailsSent", iEmailsSentCt.toString());
        	replacementValues.put("JobType", jobType.substring(9));
        	sendEmailJobCompleted(eval.getId(), replacementValues);	
        }
        
        return (String[]) sentEmails.toArray(new String[] {});
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEmailMessages(java.lang.String, java.lang.Long, java.lang.String[], java.lang.String)
     */
    public String[] sendEmailMessages(String message, String subject, Long evaluationId, String[] groupIds, String includeConstant) {
        EvalUtils.validateEmailIncludeConstant(includeConstant);
        log.debug("message:" + message + ", evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String from = getFromEmailOrFail(eval);

        // get the associated eval groups for this evaluation
        // NOTE: this only returns the groups that should get emails, there is no need to do an additional check
        // to see if the instructor has opted in in this case -AZ
        Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvalGroupsForEval(new Long[] { evaluationId }, false, null);

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

            String[] limitGroupIds = null;
            if (evalGroupId != null) {
                limitGroupIds = new String[] {evalGroupId};
            }

            HashSet<String> userIdsSet = new HashSet<String>();
            List<EvalAssignUser> participants = evaluationService.getParticipantsForEval(evaluationId, null, limitGroupIds, null, null, includeConstant, null);
            for (EvalAssignUser evalAssignUser : participants) {
                userIdsSet.add( evalAssignUser.getUserId() );
            }

            //Set<String> userIdsSet = evaluationService.getUserIdsTakingEvalInGroup(evaluationId, evalGroupId, includeConstant);
            if (userIdsSet.size() > 0) {
                // turn the set into an array
                String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
                log.debug("Found " + toUserIds.length + " users (" + toUserIds + ") to send "
                        + EvalConstants.EMAIL_TEMPLATE_REMINDER + " notification to for available evaluation ("
                        + evaluationId + ") and group (" + group.evalGroupId + ")");

                // replace the text of the template with real values
                Map<String, String> replacementValues = new HashMap<String, String>();
                replacementValues.put("HelpdeskEmail", from);

                // send the actual emails for this evalGroupId
                String[] emailAddresses = sendUsersEmails(from, toUserIds, subject, message);
                log.info("Sent evaluation reminder message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
                // store sent emails to return
                for (int j = 0; j < emailAddresses.length; j++) {
                    sentEmails.add(emailAddresses[j]);            
                }
                commonLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);
            }
        }

        return (String[]) sentEmails.toArray(new String[] {});
    }

    /**
     * Builds the email message from a template and a bunch of email gobal variables
     * 
     * @param messageTemplate
     * @param subjectTemplate
     * @param eval
     * @param group
     * @return the processed message template with replacements and logic handled
     */
    public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
            EvalGroup group) {
        // replace the text of the template with real values
    	Map<String, String> replacementValues = new HashMap<String, String>();
        replacementValues.put("EvalTitle", eval.getTitle());

        // use a date which is related to the current users locale
        DateFormat df;
        boolean useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);
        if (useDateTime) {
            // show date and time if date/time enabled
            df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, 
                    commonLogic.getUserLocale(commonLogic.getCurrentUserId()));
        } else {
            df = DateFormat.getDateInstance(DateFormat.MEDIUM, 
                    commonLogic.getUserLocale(commonLogic.getCurrentUserId()));
        }

        replacementValues.put("EvalStartDate", df.format(eval.getStartDate()));
        String dueDate = "--------";
        if (eval.getDueDate() != null) {
            dueDate = df.format(eval.getDueDate());
        }
        replacementValues.put("EvalDueDate", dueDate);
        String viewDate = null;
        if (eval.getViewDate() != null) {
            viewDate = df.format(eval.getDueDate());
        } else {
            viewDate = dueDate;
        }
        replacementValues.put("EvalResultsDate", viewDate);
        // https://bugs.caret.cam.ac.uk/browse/CTL-1505 - no titles for empty or adhoc groups
        String groupTitle = "";
        if (group == null || group.title == null || EvalConstants.GROUP_TYPE_ADHOC.equals(group.type)) {
            groupTitle = "";
        } else if (EvalConstants.GROUP_TYPE_ADHOC.equals(group.type)) {
            groupTitle = "Adhoc Group";
        } else {
            groupTitle = group.title;
        }
        replacementValues.put("EvalGroupTitle", groupTitle);
        
        replacementValues.put("HelpdeskEmail", getFromEmailOrFail(eval));

        // setup the opt-in, opt-out, and add questions variables
        int addItems = ((Integer) settings.get(EvalSettings.ADMIN_ADD_ITEMS_NUMBER)).intValue();
        if (! eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED) || (addItems > 0)) {
            if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN)) {
                // if eval is opt-in notify instructors that they may opt in
                replacementValues.put("ShowOptInText", "true");
            } else if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_OUT)) {
                // if eval is opt-out notify instructors that they may opt out
                replacementValues.put("ShowOptOutText", "true");
            }
            if (addItems > 0) {
                // if eval allows instructors to add questions notify instructors they may add questions
                replacementValues.put("ShowAddItemsText", "true");
            }
        }

        // ensure that the if-then variables are set to false if they are unset
        if (! replacementValues.containsKey("ShowAddItemsText")) {
            replacementValues.put("ShowAddItemsText", "false");
        }
        if (! replacementValues.containsKey("ShowOptInText")) {
            replacementValues.put("ShowOptInText", "false");
        }
        if (! replacementValues.containsKey("ShowOptOutText")) {
            replacementValues.put("ShowOptOutText", "false");
        }

        // generate URLs to the evaluation
        String evalEntityURL = null;
        if (group != null && group.evalGroupId != null) {
            // get the URL directly to the evaluation with group context included
            EvalAssignGroup assignGroup = evaluationService.getAssignGroupByEvalAndGroupId(eval.getId(), group.evalGroupId);
            if (assignGroup != null) {
                evalEntityURL = commonLogic.getEntityURL(assignGroup);
            }
        }

        if (evalEntityURL == null) {
            // just get the URL to the evaluation without group context
            evalEntityURL = commonLogic.getEntityURL(eval);
        }

        // all URLs are identical because the user permissions determine access uniquely
        replacementValues.put("URLtoTakeEval", evalEntityURL);
        replacementValues.put("URLtoAddItems", evalEntityURL);
        replacementValues.put("URLtoOptIn", evalEntityURL);
        replacementValues.put("URLtoOptOut", evalEntityURL);
        replacementValues.put("URLtoViewResults", 
                commonLogic.getEntityURL(EvalReportsEntityProvider.ENTITY_PREFIX, eval.getId().toString()) );
        replacementValues.put("URLtoSystem", commonLogic.getServerUrl());

        // these are values which are not handled - these are just placeholders -https://bugs.caret.cam.ac.uk/browse/CTL-1604
        replacementValues.put("EarliestEvalDueDate", dueDate);
        replacementValues.put("EvalCLE", commonLogic.getConfigurationSetting("ui.service", "Sakai"));
        replacementValues.put("EvalToolTitle", "Evaluation System");
        replacementValues.put("EvalSite", groupTitle);
        replacementValues.put("MyWorkspaceDashboard", evalEntityURL);

        String message = TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
        String subject = null;
        if (subjectTemplate != null) {
            subject = TextTemplateLogicUtils.processTextTemplate(subjectTemplate, replacementValues);
        }
        return new EvalEmailMessage(subjectTemplate, messageTemplate, subject, message);
    }

    
    
    // INTERNAL METHODS
    
    /**
     * EVALSYS-916 Send email to the helpdesk user notifying that the job is completed.
     *
     * @param evaluationId unique id of an eval from the email address this email appears to come from
     * @param replacementValues a map of values to be included in this email:
     *        HelpdeskEmail: the email to send this to
     *        EvalTitle: the title of the evaluation
     *        NumEmailsSent: the number of emails sent
     *        JobType: the email job that just completed
     */
    public void sendEmailJobCompleted(Long evaluationId, Map<String, String> replacementValues) {

    	try {
    		EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_JOB_COMPLETED, evaluationId);	
    		if (replacementValues.get("HelpdeskEmail") != null) {
    			String[] to = {replacementValues.get("HelpdeskEmail")};
    			//String subject = "Email Job for evaluation " + evalTitle + " has completed";
    			String subject = TextTemplateLogicUtils.processTextTemplate(emailTemplate.getSubject(), replacementValues);
    			//String message = "The " + jobType.substring(9) + " email job has completed. " + numEmailsSent + " emails were sent.";
    			String message = TextTemplateLogicUtils.processTextTemplate(emailTemplate.getMessage(), replacementValues);
    			String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
    			String[] emails = commonLogic.sendEmailsToAddresses(replacementValues.get("HelpdeskEmail"), to, subject, message, true, deliveryOption);
    			log.debug("TO: " + to[0]);
    			log.debug("SUBJECT: " + subject);
    			log.debug("MESSAGE: " + message);
    		} else  {
    			log.error("No HelpdeskEmail value set, job completed email NOT sent");
    		}
    	} catch ( Exception e) {
        	log.error("Exception in sendEmailJobCompleted, email NOT sent: " + e);
        } 
        
    }    

    /**
     * INTERNAL METHOD<br/>
     * Send emails to a set of users (can send to a single user
     * by specifying an array with one item only), gets the email addresses
     * for the users ids
     * 
     * @param from the email address this email appears to come from
     * @param toUserIds the userIds this message should be sent to
     * @param subject the message subject
     * @param message the message to send
     * @param deferExceptions if true, then exceptions are logged and then thrown after sending as many emails as possible,
     * if false then exceptions are thrown immediately
     * @return an array of email addresses that this message was sent to
     */
    public String[] sendUsersEmails(String from, String[] toUserIds, String subject, String message) {
        String[] emails;
        String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
        emails = commonLogic.sendEmailsToUsers(from, toUserIds, subject, message, true, deliveryOption);
        return emails;
    }
    /**
     * INTERNAL METHOD<br/>
     * Get an email template by type and evaluationId or fail
     * @param typeConstant an {@link EvalConstants} (e.g. {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}) constant
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
