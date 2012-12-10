/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.logic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.jobmonitor.JobStatusReporter;
import org.sakaiproject.evaluation.logic.entity.EvalReportsEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalReminderStatus;
import org.sakaiproject.evaluation.logic.model.EvalUser;
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
    protected final String EVENT_EMAIL_SUBMISSION =                   "eval.email.eval.submission";

    protected static final int MIN_BATCH_SIZE = 10;
    protected static final long MILLISECONDS_PER_HOUR = 60L * 60L * 1000L;
	protected static final long MILLISECONDS_PER_DAY = 24L * MILLISECONDS_PER_HOUR;

	// ESTIMATED_CONSOLIDATED_EMAIL_JOB_TIME is an offset used when selecting EvalAssignGroup records 
	// for Consolidated Email processing.  We estimate the timestamp that would have been used in 
	// the previous email job, add two hours to that time and select records with a timestamp before
	// that time. 
	private static final long ESTIMATED_CONSOLIDATED_EMAIL_JOB_TIME = 2L * MILLISECONDS_PER_HOUR;

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

        String sampleEmail = null;
        List<String> sentEmails = new ArrayList<String>();
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
            if (sampleEmail == null && em.message != null) {
                sampleEmail = em.message;
            }

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation created message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_CREATED, eval);
        }

        if (sampleEmail == null && emailTemplate != null) {
            sampleEmail = emailTemplate.getMessage();
        }
        String[] emailsSent = sentEmails.toArray(new String[sentEmails.size()]);
        // send email to admin that reminders are finished.
        handleJobCompletion(eval, emailsSent, EvalConstants.JOB_TYPE_CREATED, from, sampleEmail);

        return emailsSent;
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

        String sampleEmail = null;
        List<String> sentEmails = new ArrayList<String>();
        // loop through groups and send emails to correct users group
        for (int i = 0; i < assignGroups.size(); i++) {
            EvalAssignGroup assignGroup = assignGroups.get(i);
            
            if(! commonLogic.isEvalGroupPublished(assignGroup.getEvalGroupId())) {
                log.info("Skipping available email for evaluationId ("+evaluationId+") and group ("+assignGroup.getEvalGroupId()+") because the group is not published");
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
            if (userIdsSet.size() == 0) {
                log.info("Skipping available email for evaluationId ("+evaluationId+") and group ("+assignGroup.getEvalGroupId()+") because there is no one (instructors or participants) to send the email to");
                continue;
            }

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
            if (sampleEmail == null && em.message != null) {
                sampleEmail = em.message;
            }

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation available message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_AVAILABLE, eval);
        }

        if (sampleEmail == null && emailTemplate != null) {
            sampleEmail = emailTemplate.getMessage();
        }
        String[] emailsSent = sentEmails.toArray(new String[sentEmails.size()]);
        handleJobCompletion(eval, emailsSent, EvalConstants.JOB_TYPE_ACTIVE, from, sampleEmail);

        return emailsSent;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)
     */
    public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId) {

        if (! commonLogic.isEvalGroupPublished(evalGroupId)) {
            return new String[] {};
        }

        String sampleEmail = null;
        List<String> sentEmails = new ArrayList<String>();

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
            if (sampleEmail == null && em.message != null) {
                sampleEmail = em.message;
            }

            // send the actual emails for this evalGroupId
            String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
            log.info("Sent evaluation available group message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
            // store sent emails to return
            for (int j = 0; j < emailAddresses.length; j++) {
                sentEmails.add(emailAddresses[j]);            
            }
            commonLogic.registerEntityEvent(EVENT_EMAIL_GROUP_AVAILABLE, eval);
        }

        if (sampleEmail == null && emailTemplate != null) {
            sampleEmail = emailTemplate.getMessage();
        }
        String[] emailsSent = sentEmails.toArray(new String[sentEmails.size()]);
        handleJobCompletion(eval, emailsSent, EvalConstants.JOB_TYPE_ACTIVE, from, sampleEmail);

        return emailsSent;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalReminderNotifications(java.lang.Long, java.lang.String)
     */
    public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
        if (log.isDebugEnabled()) log.debug("sendEvalReminderNotifications(evaluationId: " + evaluationId + ", includeConstant: " + includeConstant+")");

        EvalUtils.validateEmailIncludeConstant(includeConstant);

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String from = getFromEmailOrFail(eval);
        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_REMINDER, evaluationId);

        // get the associated eval groups for this evaluation
        // NOTE: this only returns the groups that should get emails, there is no need to do an additional check
        // to see if the instructor has opted in in this case -AZ
        Map<Long, List<EvalGroup>> evalGroupIds = evaluationService.getEvalGroupsForEval(new Long[] { evaluationId }, false, null);

        // handle recovery of interrupted email sending
        boolean updateReminderStatus = (Boolean) settings.get(EvalSettings.ENABLE_REMINDER_STATUS);
        EvalReminderStatus reminderStatus = eval.getCurrentReminderStatus();
        if (updateReminderStatus && reminderStatus != null) {
            log.info("Reminder recovery processing for eval ("+evaluationId+") will attempt to continue from: "+reminderStatus);
        }

        // only one possible map key so we can assume evaluationId
        List<EvalGroup> groups = evalGroupIds.get(evaluationId);
        if (log.isDebugEnabled()) log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);

        String sampleEmail = null;
        List<String> sentEmails = new ArrayList<String>();
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
            			reminderStatus = null;
            			log.info("Reminder recovery processing for eval ("+evaluationId+"), found last processed group ("+evalGroupId+") at position "+(i+1)+" of "+groups.size());
            		}
            		// skip this group
            		if (log.isDebugEnabled()) {
            			log.debug("Reminder recovery processing for eval ("+evaluationId+"), reminder status ("+reminderStatus+"), skipping group "+evalGroupId);
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
                    log.debug("Found " + toUserIds.length + " users (" + ArrayUtils.arrayToString(toUserIds) + ") of type "
                            + includeConstant+" to send " + EvalConstants.EMAIL_TEMPLATE_REMINDER 
                            + " notification to for available evaluation ("+ evaluationId + ") and group (" + group.evalGroupId + ")");
                }

                EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, group, includeConstant);
                if (sampleEmail == null && em.message != null) {
                    sampleEmail = em.message;
                }

                // send the actual emails for this evalGroupId
                String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
                log.info("Sent evaluation reminder message for eval ("+evaluationId+") and group ("+group.evalGroupId+") to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
                // store sent emails to return
                for (int j = 0; j < emailAddresses.length; j++) {
                    sentEmails.add(emailAddresses[j]);            
                }
            }
            // update the reminder status
            if (updateReminderStatus) {
                if (log.isDebugEnabled()) log.debug("Reminder recovery processing for eval ("+evaluationId+"), update to group ("+evalGroupId+"), at "+(i+1)+" / "+groups.size());
            	evaluationService.updateEvaluationReminderStatus(evaluationId, new EvalReminderStatus(groups.size(), i+1, evalGroupId));
            }
        }
        // set reminder status back to idle
        if (updateReminderStatus) {
            if (log.isDebugEnabled()) log.debug("Reminder recovery processing for eval ("+evaluationId+"), cleared status");
        	evaluationService.updateEvaluationReminderStatus(evaluationId, null);
        }
        commonLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);

        if (sampleEmail == null && emailTemplate != null) {
            sampleEmail = emailTemplate.getMessage();
        }
        String[] emailsSent = sentEmails.toArray(new String[sentEmails.size()]);
        // send email to helpdeskEmail that reminders are finished.
        handleJobCompletion(eval, emailsSent, EvalConstants.JOB_TYPE_REMINDER, from, sampleEmail);

        if (log.isDebugEnabled()) log.debug("Reminder processing complete for eval ("+evaluationId+"), sent emails to: "+sentEmails);
        return emailsSent;
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

        String sampleEmail = null;
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
                if (sampleEmail == null && em.message != null) {
                    sampleEmail = em.message;
                }

                // send the actual emails for this evalGroupId
                String[] emailAddresses = sendUsersEmails(from, toUserIds, em.subject, em.message);
                log.info("Sent evaluation results message to " + emailAddresses.length + " users (attempted to send to "+toUserIds.length+")");
                // store sent emails to return
                for (int j = 0; j < emailAddresses.length; j++) {
                    sentEmails.add(emailAddresses[j]);            
                }
                commonLogic.registerEntityEvent(EVENT_EMAIL_RESULTS, eval);
            }
        }

        if (sampleEmail == null && emailTemplate != null) {
            sampleEmail = emailTemplate.getMessage();
        }
        String[] emailsSent = sentEmails.toArray(new String[sentEmails.size()]);
        // send email to admin that reminders are finished.
        handleJobCompletion(eval, emailsSent, jobType, from, sampleEmail);

        return emailsSent;
    }


    /**
     * Special method to handle the job completion notifications
     * EVALSYS-916 Send email to the helpdesk user notifying that the job is completed
     * 
     * @param eval the eval
     * @param emailsSent email addresses sent to
     * @param jobType type of job
     * @param from [OPTIONAL] from address
     * @param sampleEmail [OPTIONAL] a sample of the email that was sent
     */
    protected void handleJobCompletion(EvalEvaluation eval, String[] emailsSent, String jobType, String from, String sampleEmail) {
        // send email to admin that reminders are finished.
        boolean sendJobCompletion = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        if (sendJobCompletion) {
            if (eval == null) {
                log.error("Cannot send job completion email");
                return;
            }
            if (jobType == null) {
                jobType = EvalConstants.JOB_TYPE_ACTIVE;
            }
            if (jobType.length() > 9) {
                jobType = jobType.substring(9); // trim off "scheduled" from beginning
            }
            if (from == null) {
                from = getFromEmailOrFail(eval);
            }
            if (emailsSent == null) {
                emailsSent = new String[0];
            }
            if (sampleEmail == null) {
                sampleEmail = "NO SAMPLE AVAILABLE";
            }
            int emailsSentCt = emailsSent.length;
            StringBuilder sb = new StringBuilder();
            for (String email : emailsSent) {
                sb.append("  - ");
                sb.append(email);
                sb.append("\n");
            }
            String emailsSentList = sb.toString();

            log.info("Evals Job Completed: sent "+emailsSentCt+" "+jobType+" emails from "+from+" for eval: "+eval.getTitle()+" ("+eval.getId()+"): emails: "+emailsSentList);

            Map<String, String> replacementValues = new HashMap<String, String>();
            replacementValues.put("HelpdeskEmail", from);
            replacementValues.put("EvalTitle", eval.getTitle());
            replacementValues.put("NumEmailsSent", String.valueOf(emailsSentCt));
            replacementValues.put("EmailsSentList", emailsSentList);
            replacementValues.put("JobType", jobType);
            replacementValues.put("SampleEmail", sampleEmail);

            /* @param replacementValues a map of values to be included in this email:
             *        HelpdeskEmail: the email to send this to
             *        EvalTitle: the title of the evaluation
             *        NumEmailsSent: the number of emails sent
             *        EmailsSent: the email addresses sent to
             *        JobType: the email job that just completed
             *        SampleEmail: a sample of the sent email
             */
            try {
                EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_JOB_COMPLETED, eval.getId()); 
                if (replacementValues.get("HelpdeskEmail") != null) {
                    String[] to = {replacementValues.get("HelpdeskEmail")};
                    //String subject = "Email Job for evaluation " + evalTitle + " has completed";
                    String subject = TextTemplateLogicUtils.processTextTemplate(emailTemplate.getSubject(), replacementValues);
                    //String message = "The " + jobType.substring(9) + " email job has completed. " + numEmailsSent + " emails were sent.";
                    String message = TextTemplateLogicUtils.processTextTemplate(emailTemplate.getMessage(), replacementValues);
                    String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
                    String[] emails = commonLogic.sendEmailsToAddresses(replacementValues.get("HelpdeskEmail"), to, subject, message, true, deliveryOption);
                    if (log.isDebugEnabled()) {
                        log.debug("SENT TO: " + ArrayUtils.arrayToString(emails));
                        log.debug("TO: " + ArrayUtils.arrayToString(to));
                        log.debug("SUBJECT: " + subject);
                        log.debug("MESSAGE: " + message);
                    }
                } else  {
                    log.error("No HelpdeskEmail value set, job completed email NOT sent");
                }
            } catch ( Exception e) {
                log.error("Exception in sendEmailJobCompleted, email NOT sent: " + e);
            }
        }
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
    	String includeConstant = null;
    	return makeEmailMessage(messageTemplate, subjectTemplate, eval, group, includeConstant);
    }
    
    public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
    		EvalGroup group, String includeConstant) {
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
            viewDate = df.format(eval.getViewDate());
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
        int addItems = ((Integer) settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER)).intValue();
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
        
        Boolean canEditResponses = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
		
		if (canEditResponses == null){
			if(EvalUtils.safeBool(eval.getModifyResponsesAllowed(), false)){
				replacementValues.put("ShowAllowEditResponsesText", "true");
			}else{
				replacementValues.put("ShowAllowEditResponsesText", "false");
			}
		}else if (canEditResponses){
			replacementValues.put("ShowAllowEditResponsesText", "true");
		}else{
			replacementValues.put("ShowAllowEditResponsesText", "false");
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
        if (! replacementValues.containsKey("ShowAllowEditResponsesText")) {
            replacementValues.put("ShowAllowEditResponsesText", "false");
        }
        if (! replacementValues.containsKey("InProgress")) {
        	replacementValues.put("InProgress", (EvalConstants.EVAL_INCLUDE_IN_PROGRESS.equals(includeConstant) ? "true" : "false"));
        }

        // generate URLs to the evaluation
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
        
		String timeStamp =  df.format(new Date());
		replacementValues.put("TimeStamp", timeStamp);
		
		//handle the username variable if we can get the user
		String name = "";
		try{
			String currentUserId = commonLogic.getCurrentUserId();
	        EvalUser user = commonLogic.getEvalUserById(currentUserId);
	        name = user.displayName;
		}catch (Exception e) {
			//not populating the username variable with anything proper. We could not get a valid user.
		}
		replacementValues.put("UserName", name);
		
        String message = TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
        String subject = null;
        if (subjectTemplate != null) {
            subject = TextTemplateLogicUtils.processTextTemplate(subjectTemplate, replacementValues);
        }
        return new EvalEmailMessage(subjectTemplate, messageTemplate, subject, message);
    }

    // INTERNAL METHODS
	
	/**
     * INTERNAL METHOD<br/>
	 * @param endTime
	 * @param startTime
	 * @return
	 */
	protected String calculateElapsedTimeMessage(Date endTime, Date startTime) {
		long elapsedTime = endTime.getTime() - startTime.getTime();
		long milliseconds = elapsedTime % 1000L;
		long seconds = elapsedTime / 1000L;
		StringBuilder buf = new StringBuilder();
		buf.append(seconds);
		buf.append(".");
		if(milliseconds < 10) {
			buf.append("00");
		} else if(milliseconds < 100) {
			buf.append("0");
		}
		buf.append(milliseconds);
		buf.append(" seconds from ");
		DateFormat df = DateFormat.getTimeInstance();
		buf.append(df.format(startTime));
		buf.append(" to ");
		buf.append(df.format(endTime));
		String msg = buf.toString();
		return msg;
	}

	/**
     * INTERNAL METHOD<br/>
	 * @param waitInterval
	 */
	protected void takeShortBreak(Integer waitInterval) {
		if(waitInterval.longValue() > 0L) {
			if(log.isDebugEnabled()) {
				log.debug("Starting wait interval during email processing (in seconds): " + waitInterval);
			}
			try {
				Thread.sleep(waitInterval.longValue() * 1000L);
			} catch (InterruptedException e) {
				log.warn("InterruptedException while waiting during email processing: " + e);
			}
		}
	}
     
    /**
     * INTERNAL METHOD<br/>
	 * @param jobId 
     * @param userMap
     * @param jobStatusReporter 
	 * @return
	 */
	protected List<String> processConsolidatedEmails(String jobId, List<Map<String,Object>> userMap, JobStatusReporter jobStatusReporter) {
		Integer reportingInterval = (Integer) this.settings.get(EvalSettings.LOG_PROGRESS_EVERY);
		if(reportingInterval == null) {
			// setting reportingInterval to zero results in no incremental reports.
			reportingInterval = new Integer(0);
		}
		Date timeEmailSent = new Date();
		int userCounter = 0;
		int emailCounter = 0;
		List<String> recipients = new ArrayList<String>();
		Set<String> inProgressEvaluationOwners = new HashSet<String>();
		boolean saveWithoutSubmit = (Boolean) settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
		if (saveWithoutSubmit) {
		    inProgressEvaluationOwners = evaluationService.getInProgressEvaluationOwners();
		}
    	for(Map<String,Object> entry : userMap) {
    		String userId = (String) entry.get(EvalConstants.KEY_USER_ID);
    		Date earliestDueDate = (Date) entry.get(EvalConstants.KEY_EARLIEST_DUE_DATE);
    		Long emailTemplateId = (Long) entry.get(EvalConstants.KEY_EMAIL_TEMPLATE_ID);
    		
    		EvalEmailTemplate template = evaluationService.getEmailTemplate(emailTemplateId);
    			
    		Map<String, String> replacementValues = new HashMap<String, String>();
    		EvalUser user = commonLogic.getEvalUserById(userId);
    		// get user's locale
    		Locale locale = commonLogic.getUserLocale(userId);
    	    // use a date which is related to the current users locale
    	    DateFormat df;
    	    boolean useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);
    	    if (useDateTime) {
	            // show date and time if date/time enabled
	            df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
    	    } else {
	            df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
	        }
			// add date to replacementValues
			replacementValues.put("EarliestEvalDueDate",df.format(earliestDueDate));
			replacementValues.put("EvalCLE", commonLogic.getConfigurationSetting("ui.service", "Sakai"));
			// get eval tool title from settings? from message bundle?
			replacementValues.put("EvalToolTitle", "Teaching Evaluations");
			replacementValues.put("EvalSite", "MyWorkspace");
			String from = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
			// we can get it from the eval if needed, but it should come from settings
			replacementValues.put("HelpdeskEmail",from);
			replacementValues.put("MyWorkspaceDashboard", commonLogic.getMyWorkspaceDashboard(userId));
			replacementValues.put("InProgress", (inProgressEvaluationOwners.contains(userId) ? "true" : "false"));
			replacementValues.put("URLtoSystem", commonLogic.getServerUrl());
			try {
				String message = TextTemplateLogicUtils.processTextTemplate(template.getMessage(), replacementValues);
				String subject = TextTemplateLogicUtils.processTextTemplate(template.getSubject(), replacementValues);
				if(message == null || subject == null) {
					if(jobStatusReporter != null) {
						jobStatusReporter.reportError(jobId, false, "error", "Error attempting to send email to user (" + user.displayId + "). ");
					}
					log.warn("Error trying to send consolidated email to user " + user.displayId, new RuntimeException("\nsubject == " + subject + "\nmessage == " + message));
				} else {
					this.commonLogic.sendEmailsToUsers(from, new String[]{userId}, subject, message, false, EvalConstants.EMAIL_DELIVERY_DEFAULT);
					emailCounter++;
					recipients.add(user.displayId);
					List<Long> evalAssignUserIds = (List<Long>) entry.get(EvalConstants.KEY_USER2EVAL_ASSIGNMENTS);
					if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE.equalsIgnoreCase(template.getType())) {
						this.evaluationService.updateEvalAssignUsersAnnouncementsSent(evalAssignUserIds, timeEmailSent);
					} else if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER.equalsIgnoreCase(template.getType())) {
						this.evaluationService.updateEvalAssignUsersRemindersSent(evalAssignUserIds, timeEmailSent);
					}
				}
			} catch (Exception e) {
				if(jobStatusReporter != null) {
					jobStatusReporter.reportError(jobId, false, "error", "Error attempting to send email to user (" + user.displayId + "). " + e);
				}
				log.warn("Error trying to send consolidated email to user " + user.displayId, e);
			}

    		if(jobId != null && reportingInterval.intValue() > 0) {
	    		userCounter++;
	    		if(userCounter % reportingInterval.intValue() == 0) {
	    			if(jobStatusReporter != null) {
	    				jobStatusReporter.reportProgress(jobId, "ProcessingEmails", "Processed " + userCounter + " of " + userMap.size() + " evaluatees and sent " + emailCounter + " emails.");
	    			}
	    		}
    		}
    	}
		return recipients;
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
        String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
        if (log.isDebugEnabled()) log.debug("sendUsersEmails(from:"+from+", to:"+ArrayUtils.arrayToString(toUserIds)+", subj:"+subject);
        String[] emails = commonLogic.sendEmailsToUsers(from, toUserIds, subject, message, true, deliveryOption);
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

	/**
     * INTERNAL METHOD<br/>
	 * 
	 */
	protected void logConsolidatedEmailRecipients(String emailType, String[] recipients) {
		StringBuilder buf = new StringBuilder();
		buf.append(emailType);
		buf.append(" sent to ");
		buf.append(recipients.length);
		buf.append(" recipients: ");
		boolean first = true;
		for(String recipient : recipients) {
			if(! first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append(recipient);
		}
		log.info(buf.toString());
	}

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalSubmissionConfirmationEmail(java.lang.Long)
     */
	public String sendEvalSubmissionConfirmationEmail(String userId, Long evaluationId) {
	    String to = null;
	    Boolean sendConfirmation = (Boolean) settings.get(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL);

	    if (sendConfirmation.booleanValue()) {
	        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
	        String from = getFromEmailOrFail(eval);
	        //get the template
	        EvalEmailTemplate emailTemplate = getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_SUBMITTED, evaluationId);
	        if (emailTemplate != null) {
	            //make email and do the variable substitutions
	            EvalEmailMessage em = makeEmailMessage(emailTemplate.getMessage(), emailTemplate.getSubject(), eval, null);
	            // send the actual email for this user
	            String[] emailAddresses = sendUsersEmails(from, new String[]{userId}, em.subject, em.message);
	            if (emailAddresses.length > 0){
	                log.info("Sent Submission Confirmation email to " + userId + ". (attempted to send to "+emailAddresses.length+")");	                
	                commonLogic.registerEntityEvent(EVENT_EMAIL_SUBMISSION, EvalEvaluation.class, eval.getId().toString());
                }
			}
		}
 		return to;
 	}

	@Override
	public String[] sendConsolidatedNotifications(
			JobStatusReporter jobStatusReporter, String jobId,
			String emailTemplateType) {
		
		Date startTime = new Date();
		
		List<String> actuallySent = new ArrayList<String>();
		
		boolean sendingReminders = EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER.equalsIgnoreCase(emailTemplateType); 
		
		Boolean usingAnnouncements = (Boolean) this.settings.get(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE);
		Date priorToThisJob = new Date(System.currentTimeMillis() - ESTIMATED_CONSOLIDATED_EMAIL_JOB_TIME);
		
		try {
			Set<String> groups = new HashSet<String>();
			Map<Long,List<EvalEvaluation>> emailTemplate2EvalMap = new HashMap<Long,List<EvalEvaluation>>();
			
			// TODO: Limit the set of evals to those ready for available/reminder email?
			
			List<EvalEvaluation> allOpenEvals = this.evaluationService.getEvaluationsByState(EvalConstants.EVALUATION_STATE_ACTIVE);
			
			for(EvalEvaluation eval : allOpenEvals) {
				EvalEmailTemplate emailTemplate = null;
				
				if(sendingReminders) {
					emailTemplate = eval.getReminderEmailTemplate();
				} else {
					emailTemplate = eval.getAvailableEmailTemplate();
				}
				if(emailTemplate.getType().equalsIgnoreCase(emailTemplateType)) {
					List<EvalEvaluation> evalList = emailTemplate2EvalMap.get(emailTemplate.getId());
					if(evalList == null) {
						evalList = new ArrayList<EvalEvaluation>();
						emailTemplate2EvalMap.put(emailTemplate.getId(), evalList);
					}
					evalList.add(eval);
				}
			}
			
			int templateCount = 0;
			for(Map.Entry<Long, List<EvalEvaluation>> entry : emailTemplate2EvalMap.entrySet()) {
				templateCount++;
				StringBuilder templateMsgBuf = new StringBuilder();
				templateMsgBuf.append(emailTemplateType);
				templateMsgBuf.append(": Email template ");
				templateMsgBuf.append(templateCount);
				templateMsgBuf.append(" of ");
				templateMsgBuf.append(emailTemplate2EvalMap.size());
				jobStatusReporter.reportProgress(jobId, "newTemplate", templateMsgBuf.toString());
				Map<String, Map<String, Object>> emailDataMap = new HashMap<String, Map<String, Object>>();
				List<EvalEvaluation> evals = entry.getValue();
				if(evals != null) {
					int evalCount = 0;
					for(EvalEvaluation eval : evals) {
						if(evalCount % 100 == 0) {
							reportQueueSize(jobStatusReporter, jobId,
									emailTemplateType, evalCount,
									evals.size(), emailDataMap.size());
						}
						evalCount++;
						Long evaluationId = eval.getId();
						Boolean includeAvailableEmailSentNull = null;
						Date includeAvailableEmailSentBefore = null;
						Boolean includeReminderEmailSentNull = null;
						Date includeReminderEmailSentBefore = null;
						if(sendingReminders) {
							// sending reminders; check that no emails have been sent too recently
							// and that announcement has been sent if it's required first
							if(usingAnnouncements.booleanValue()) {
								// check that announcement has been sent but not too recently  
								includeAvailableEmailSentNull = false;
								includeAvailableEmailSentBefore = priorToThisJob;
							} 
							includeReminderEmailSentNull = true;
							includeReminderEmailSentBefore = priorToThisJob;
						} else {
							if(usingAnnouncements.booleanValue()) {
								// check that announcement has not been sent   
								includeAvailableEmailSentNull = true;
								// includeAvailableEmailSentBefore = whenPreviousEmailJobStarted;
							}							
						}
						List<EvalAssignUser> evalAssignUsers = this.evaluationService.getEvaluatorsForEval(evaluationId, includeAvailableEmailSentNull, includeAvailableEmailSentBefore, includeReminderEmailSentNull, includeReminderEmailSentBefore);
						// List<EvalAssignUser> evalAssignUsers = this.evaluationService.getParticipantsForEval(eval.getId(), null, null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
						if(evalAssignUsers != null) {
							for(EvalAssignUser evalAssignUser : evalAssignUsers) {
								if(evalAssignUser.getCompletedDate() != null) {
									// skip this one; user has completed eval
									continue;
								}
								Map<String, Object> emailData = emailDataMap.get(evalAssignUser.getUserId());
								if(emailData == null) {
									emailData = new HashMap<String, Object>();
									emailDataMap.put(evalAssignUser.getUserId(), emailData);
								}
								emailData.put(EvalConstants.KEY_USER_ID, evalAssignUser.getUserId());
								Date earliestDueDate = (Date) emailData.get(EvalConstants.KEY_EARLIEST_DUE_DATE);
								if(earliestDueDate == null || earliestDueDate.after(eval.getDueDate())) {
									emailData.put(EvalConstants.KEY_EARLIEST_DUE_DATE, eval.getDueDate());
								}
								emailData.put(EvalConstants.KEY_EMAIL_TEMPLATE_ID, entry.getKey());
								
								List<Long> assignments = (List<Long>) emailData.get(EvalConstants.KEY_USER2EVAL_ASSIGNMENTS);
								if(assignments == null) {
									assignments = new ArrayList<Long>();
									emailData.put(EvalConstants.KEY_USER2EVAL_ASSIGNMENTS, assignments);
								}
								assignments.add(new Long(evalAssignUser.getId().longValue()));
								
								groups.add(evalAssignUser.getEvalGroupId());
								if(sendingReminders) {
									evalAssignUser.setReminderEmailSent(startTime);
								} else {
									evalAssignUser.setAvailableEmailSent(startTime);
								}
							}
						}
					}
					reportQueueSize(jobStatusReporter, jobId,
						emailTemplateType, evalCount,
						evals.size(), emailDataMap.size());
				}
				
				List<Map<String,Object>> emailDataList = new ArrayList<Map<String,Object>>(emailDataMap.values());
				
		    	if(jobId != null && jobStatusReporter != null) {
		    		if(sendingReminders) {
		    			jobStatusReporter.reportProgress(jobId, "sendingReminders", Integer.toString(emailDataList.size()));
		    			jobStatusReporter.reportProgress(jobId, "reminderGroups", Integer.toString(groups.size()));
		    		} else {
		    			jobStatusReporter.reportProgress(jobId, "sendingAnnouncements", Integer.toString(emailDataList.size()));
		    			jobStatusReporter.reportProgress(jobId, "announcementGroups", Integer.toString(groups.size()));
		    		}
		    	}
		    	
				List<String> processed = this.processConsolidatedEmails(jobId, emailDataList, jobStatusReporter);
				if(processed != null) {
					actuallySent.addAll(processed);
				}
			}
	
		} catch(RuntimeException e) {
			log.warn("Unexpected error processing consolidated notifications", e);
			
			if(jobStatusReporter != null) {
				jobStatusReporter.reportError(jobId, true, "error", "Fatal error attempting to send consolidated notifications. See server logs for details. " + e.getMessage());
			}
		} finally {
			if(jobId != null && jobStatusReporter != null) {
				if(sendingReminders) {
					jobStatusReporter.reportProgress(jobId, "reminders", calculateElapsedTimeMessage(new Date(), startTime));
					jobStatusReporter.reportProgress(jobId, "reminderUsers", Integer.toString(actuallySent.size()));
				} else {
					jobStatusReporter.reportProgress(jobId, "announcements", calculateElapsedTimeMessage(new Date(), startTime));
					jobStatusReporter.reportProgress(jobId, "announcementUsers", Integer.toString(actuallySent.size()));
				}
			}
		}

		return (String[]) actuallySent.toArray(new String[actuallySent.size()]);
		
	}

	/**
	 * @param jobStatusReporter
	 * @param jobId
	 * @param emailTemplateType
	 * @param evalCount
	 * @param totalEvalsToProcess
	 * @param evaluatorsInQueue
	 */
	protected void reportQueueSize(JobStatusReporter jobStatusReporter,
			String jobId, String emailTemplateType, int evalCount,
			int totalEvalsToProcess, int evaluatorsInQueue) {
		StringBuilder evalMsgBuf = new StringBuilder();
		evalMsgBuf.append(emailTemplateType);
		evalMsgBuf.append(": Processed ");
		evalMsgBuf.append(evalCount);
		evalMsgBuf.append(" evals of ");
		evalMsgBuf.append(totalEvalsToProcess );
		evalMsgBuf.append("; Evaluators in queue for email: ");
		evalMsgBuf.append(evaluatorsInQueue );
		jobStatusReporter.reportProgress(jobId, "buildingQueue", evalMsgBuf.toString());
	}

}


