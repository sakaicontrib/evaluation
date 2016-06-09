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


import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.jobmonitor.JobStatusReporter;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Handles all logic associated with processing email notifications,
 * the methods to send emails are in {@link EvalCommonLogic}
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalEmailsLogic {

    /**
     * Sends a message to a set of recipients for an evaluation and optionally some groups 
     * based on the include constant (e.g. {@link EvalConstants#EVAL_INCLUDE_NONTAKERS} )
     * 
     * @param message the message to send (should already have the values replaced)
     * @param subject (optional) the subject of the message, will generate a subject using the eval title if this is null or blank
     * @param evaluationId the id of an EvalEvaluation object
     * @param evalGroupIds (optional) the eval groups to send to in this eval OR null/empty to send to all
     * @param includeConstant a constant to indicate what users should receive the notification, EVAL_INCLUDE_* from {@link EvalConstants}
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEmailMessages(String message, String subject, Long evaluationId, String[] evalGroupIds, String includeConstant);

    /**
     * Builds the email message from a template and a bunch of replacement variables
     * (passed in and otherwise)
     * 
     * @param messageTemplate an email message template with variables to replace
     * @param subjectTemplate TODO
     * @param eval the evaluation related to this message (for replacements)
     * @param group the eval group related to the message (for replacements)
     * @return the processed message template with replacements and logic handled
     */
    public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
          EvalGroup group);
    
    /**
     * Builds the email message from a template and a bunch of replacement variables
     * (passed in and otherwise)
     * 
     * @param messageTemplate an email message template with variables to replace
     * @param subjectTemplate TODO
     * @param eval the evaluation related to this message (for replacements)
     * @param group the eval group related to the message (for replacements)
     * @param includeConstant the bunch of replacement variables
     * @return the processed message template with replacements and logic handled
     */
    public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
            EvalGroup group, String includeConstant);

    // NOTIFICATION methods

    /**
     * Send notifications to evaluatees (and owner if desired) that a new evaluation
     * has been created, includes links directly to add their own questions and details
     * about the dates for the evaluation. If questions may be added by instructor
     * include that in the notification. If instructor may opt in or opt out include
     * that in the notification.
     * 
     * @param evaluationId the id of an EvalEvaluation object
     * @param includeOwner if true then send an email to the owner (creator of this evaluation) also, else do not include the owner
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner);

    /**
     * Send notifications to evaluators that there is an evaluation ready for them to take
     * and includes information about the evaluation (dates), also includes links to
     * take the evaluation in "one-click" (i.e. link directly to the take_eval page)
     * 
     * @param evaluationId the id of an EvalEvaluation object
     * @param includeEvaluatees if true, if evaluatees (probably instructors)
     * have not opted into an evaluation which is opt-in include notification, otherwise this does nothing
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees);

    /**
     * Send late notification to evaluators that there is an evaluation ready for them to take
     * and includes information about the evaluation (dates), also includes links to
     * take the evaluation in "one-click" (i.e. link directly to the take_eval page)
     * Called if an instructor opts-in after receiving an email sent on the 
     * Start Date saying that an evaluation may be taken if the instructor opts-in
     * 
     * @param evaluationId the identifier for the EvalEvaluation that may be taken
     * @param evalGroupId the identifier for the EvalGroup to be notified
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId);

    /**
     * Send reminder notifications to all users who are taking an evaluation,
     * these include a direct link to the evaluation and information about the
     * evalGroupId being evaluated and the dates of the evaluation, 
     * uses the reminder template and the users to include can be controlled
     * 
     * @param evaluationId the id of an EvalEvaluation object
     * @param includeConstant a constant to indicate what users should receive the notification, EVAL_INCLUDE_* from {@link EvalConstants}
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant);

    /**
     * Send notifications that the evaluation is now complete and the results are viewable,
     * includes stats for the evaluation (response rate, etc.) and links directly to
     * the results page for this evaluation, owner of the evaluation is always included
     * in the notification
     * 
     * @param evaluationId the id of an EvalEvaluation object
     * @param includeEvaluatees if true, include notifications to all evaluated users
     * @param includeAdmins if true, include notifications to all admins above the contexts and
     * eval groups evaluated in this evaluation, otherwise include evaluatees only
     * @param jobType JOB_TYPE_VIEWABLE, JOB_TYPE_VIEWABLE_INSTRUCTORS or JOB_TYPE_VIEWABLE_STUDENTS
     * @return an array of the email addresses that were sent to
     */
    public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees, boolean includeAdmins, String jobType);

    /**
     * 
     * @param jobStatusReporter TODO
     * @param jobId
     * @return
     */
	public String[] sendConsolidatedReminderNotifications(JobStatusReporter jobStatusReporter, String jobId);

	/**
	 * 
	 * @param jobStatusReporter TODO
	 * @param jobId
	 * @return
	 */
	public String[] sendConsolidatedAvailableNotifications(JobStatusReporter jobStatusReporter, String jobId);

    /**
     * Send confirmation to a user that an evaluation has been submitted (might be required by Instructor)
     * @param evalId the id of an EvalEvaluation object
     * @param userId the UUID of the user to send this email to
     * @return the email address of the user
     */
    public String sendEvalSubmissionConfirmationEmail(String userId, Long evalId);
    
}
