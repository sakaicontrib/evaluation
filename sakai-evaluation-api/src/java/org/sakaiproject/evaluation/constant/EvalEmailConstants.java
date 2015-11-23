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
package org.sakaiproject.evaluation.constant;


/**
 * This holds the default email template constants until we can get them into properties
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalEmailConstants {

   /**
    * EmailTemplate subject: Created
    */
   public static final String EMAIL_CREATED_DEFAULT_SUBJECT = 
      "New Evaluation: ${EvalTitle} created";
   /**
    * EmailTemplate message setting:
    * This is the default template for when the evaluation is created<br/>
    * Replaceable strings:<br/>
    * ${EarliestEvalDueDate} - for single email per user, the earliest due date among evaluations available to a user
    * ${EvalTitle} - the title of this evaluation
    * ${EvalStartDate} - the start date of this evaluation
    * ${EvalDueDate} - the due date of this evaluation
    * ${EvalResultsDate} - the view results date of this evaluation
    * ${EvalGroupTitle} - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
    * ${EvalToolTitle} - the title of the evaluation tool seen by users of the tool (e.g., "Teaching Questionnaires")
    * ${EvalSite} - a description of the site where the evaluation tool is located
    * ${EvalCLE} - the local name of the Collaboration and Learning Environment where the evaluation tool is used
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${MyWorkspaceDashboard} - the direct URL the Evaluation Dashboard on the user's My Workspace site
    * ${URLtoAddItems} - the direct URL for evaluatees to add items to evals assigned from above
    * ${URLtoTakeEval} - the direct URL for evaluators to take this evaluation
    * ${URLtoViewResults} - the direct URL to view results for this evaluation
    * ${URLtoSystem} - the main URL to the system this is running in
    * ${URLtoAddItems} - the direct URL to add items to an evaluation
    * ${URLtoOptOut} - the direct URL for evaluators to opt in to use this evaluation
    * Special <#if Var == "true"> </#if> variables:
    * ShowAddItemsText - will be "true" if the add items text should be shown
    * ShowOptInText - will be "true" if this eval is opt-in (exclusive with opt-in)
    * ShowOptOutText - will be "true" if this eval is opt-out (exclusive with opt-out)
    */
   public static final String EMAIL_CREATED_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "An evaluation (${EvalTitle}) has been created for: ${EvalGroupTitle}.\n" +
      "\n" +
      "<#if ShowAddItemsText == \"true\">\n" +
      "You may add items to this evaluation until ${EvalStartDate} using the following link:\n" + 
      "${URLtoAddItems} \n" +
      "</#if>\n" +
      "<#if ShowOptInText == \"true\">\n" +
      "Its use is optional. To use the evaluation, you must opt in by using the following link:\n" +
      "${URLtoOptIn} \n" + 
      "If you do not opt in, the evaluation will not be used.\n" +
      "<#elseif ShowOptOutText == \"true\">\n" +
      "Its use is optional. The evaluation will be used unless you opt out by using the following link:\n" +
      "${URLtoOptOut} \n" +
      "</#if>\n" +
      "\n" +
      "The evaluation will run from ${EvalStartDate} to ${EvalDueDate} and the results of the evaluation will be viewable on ${EvalResultsDate}.\n" +
      "\n" +      
      "Thank you for your cooperation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in working with the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n";


   /**
    * EmailTemplate subject: Available
    */
   public static final String EMAIL_AVAILABLE_DEFAULT_SUBJECT = 
      "The Evaluation: ${EvalTitle} for ${EvalGroupTitle} is available to be taken";
   /**
    * EmailTemplate message setting:
    * This is the default template for when the evaluation is available for users to take
    * Replaceable strings:<br/>
    * ${EvalTitle} - the title of this evaluation
    * ${EvalDueDate} - the due date of this evaluation
    * ${EvalResultsDate} - the view results date of this evaluation
    * ${EvalGroupTitle} - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${URLtoTakeEval} - the direct URL for evaluators to take this evaluation
    * ${URLtoSystem} - the main URL to the system this is running in
    */
   public static final String EMAIL_AVAILABLE_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "An evaluation (${EvalTitle}) for: ${EvalGroupTitle} is ready to be filled out. Please complete this evaluation by ${EvalDueDate} at the latest.\n" +
      "\n" +
      "You may access the evaluation at:\n" +
      "${URLtoTakeEval} \n" +
      "If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
      "Enter the site using your username and password. You may submit the evaluation once only. \n" +
      "\n" +
      "Thank you for your participation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in filling out the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
      "\n" +
      "Alternate Instructions: \n" +
      "1) Go to ${URLtoSystem} \n" +
      "2) Enter your username and password and click on 'Login' button. \n" +
      "3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
      "4) Click on '${EvalGroupTitle}' link under '${EvalTitle}'.. \n";

   /**
    * EmailTemplate subject: Available
    */
   public static final String EMAIL_AVAILABLE_EVALUATEE_DEFAULT_SUBJECT = 
      "The Evaluation: ${EvalTitle} for ${EvalGroupTitle} is available to be taken";
   /**
    * EmailTemplate message setting:
    * This is the default template for when the evaluation is available for users to take
    * Replaceable strings:<br/>
    * ${EvalTitle} - the title of this evaluation
    * ${EvalStartDate} - the open date of this evaluation
    * ${EvalDueDate} - the due date of this evaluation
    * ${EvalResultsDate} - the view results date of this evaluation
    * ${EvalGroupTitle} - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${URLtoTakeEval} - the direct URL for evaluators to take this evaluation
    * ${URLtoSystem} - the main URL to the system this is running in
    */
   public static final String EMAIL_AVAILABLE_EVALUATEE_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "An evaluation (${EvalTitle}) for: ${EvalGroupTitle} is ready to be filled out by students." +
      "It has an open date of ${EvalStartDate} and is due by ${EvalDueDate} at the latest.\n" +
      "\n" +
      "You may access the evaluation at:\n" +
      "${URLtoTakeEval} \n" +
      "If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
      "Enter the site using your username and password.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in filling out the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
      "\n" +
      "Alternate Instructions: \n" +
      "1) Go to ${URLtoSystem} \n" +
      "2) Enter your username and password and click on 'Login' button. \n" +
      "3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
      "4) Click on '${EvalGroupTitle}' link under '${EvalTitle}'.. \n";
   /**
    * EmailTemplate subject: Available OPT IN
    */
   public static final String EMAIL_AVAILABLE_OPT_IN_SUBJECT = 
      "The Evaluation: ${EvalTitle} for ${EvalGroupTitle} is available for you to opt into";
   /**
    * EmailTemplate message setting:
    * This is the default template for when instructor must opt in for the evaluation to be available for users to take
    * Replaceable strings:<br/>
    * ${EvalTitle} - the title of this evaluation
    * ${EvalStartDate} - the start date of this evaluation
    * ${EvalDueDate} - the due date of this evaluation
    * ${EvalResultsDate} - the view results date of this evaluation
    * ${EvalGroupTitle} - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${URLtoOptIn} - the direct URL for evaluators to opt in to use this evaluation
    * ${URLtoTakeEval} - the direct URL to take an evaluation in the current group
    * ${URLtoSystem} - the main URL to the system this is running in
    * ${URLtoViewResults} - the direct URL to view results for this evaluation
    */
   public static final String EMAIL_AVAILABLE_OPT_IN_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "An evaluation (${EvalTitle}) for: ${EvalGroupTitle} is ready to be filled out. However, you have not opted to use this evaluation.\n" +
      "\n" +
      "If you now wish to use the evaluation, you may do so by opting in at:\n" +
      "${URLtoOptIn} \n" +
      "If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
      "Enter the site using your username and password. \n" +
      "\n" +
      "Thank you for your participation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in opting in to the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
      "\n" +
      "Alternate Instructions: \n" +
      "1) Go to ${URLtoSystem} \n" +
      "2) Enter your username and password and click on 'Login' button. \n" +
      "3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
      "4) Click on '${EvalGroupTitle}' link under '${EvalTitle}'.. \n";


   /**
    * EmailTemplate subject: Reminder
    */
   public static final String EMAIL_REMINDER_DEFAULT_SUBJECT = 
      "You still haven't completed your Evaluation: ${EvalTitle}";
   /**
    * EmailTemplate subject: Reminder, includes the name of the current group
    */
   public static final String EMAIL_REMINDER_DEFAULT_SUBJECT_GROUP = 
      "You still haven't completed your Evaluation: ${EvalTitle} for ${EvalGroupTitle}";
   /**
    * EmailTemplate message setting:
    * This is the default template for when the evaluation reminder is sent out
    */
   public static final String EMAIL_REMINDER_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "We are still awaiting the completion of an evaluation (${EvalTitle}) for: ${EvalGroupTitle}. \n" +
      "\n" +
      "<#if InProgress == \"true\">" +
      "This evaluation has been saved but not completed.  You will need to " +
      "complete and submit this evaluation before it is included in the consolidated results.\n\n" +
      "</#if>" +
      "You may access the evaluation at: \n" +
      "${URLtoTakeEval} \n" +
      "If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
      "Enter the site using your username and password. Please submit your evaluation by ${EvalDueDate}. \n" +
      "\n" +
      "Thank you for your participation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in filling out the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
      "\n" +
      "Alternate Instructions: \n" +
      "1) Go to ${URLtoSystem} \n" +
      "2) Enter your username and password and click on 'Login' button. \n" +
      "3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
      "4) Click on '${EvalGroupTitle}' link under '${EvalTitle}'.. \n";

   /**
    * EmailTemplate subject: Default results
    */
   public static final String EMAIL_RESULTS_DEFAULT_SUBJECT = 
      "The Evaluation ${EvalTitle} is complete and results are now available";
   /**
    * Sent when the evaluation closes and the default results are available
    */
   public static final String EMAIL_RESULTS_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "The results of an evaluation (${EvalTitle}) for: ${EvalGroupTitle} are available now.\n" +
      "\n" +
      "You may access the evaluation results at: \n" +
      "${URLtoViewResults} \n" +
      "If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
      "Enter the site using your username and password. \n" +
      "\n" +
      "Thank you for your participation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in viewing the evaluation results, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
      "\n" +
      "Alternate Instructions: \n" +
      "1) Go to ${URLtoSystem} \n" +
      "2) Enter your username and password and click on 'Login' button. \n" +
      "3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
      "4) Click on '${EvalGroupTitle}' link under '${EvalTitle}'.. \n";
   
   /**
    * EmailTemplate subject: Default subject for available single-email-notification 
    */
   public static final String EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_SUBJECT = "Course feedback due by ${EarliestEvalDueDate}\n";

   
   /**
    * EmailTemplate message setting:
    * This is the default template for when the single email per student option is in effect and an evaluation is available.
    * Replaceable strings:<br/>
    * ${EarliestEvalDueDate} - the earliest due date among evaluations available to a user
    * ${EvalCLE} - the local name of the Collaboration and Learning Environment where the evaluation tool is used
    * ${EvalSite} - a description of the site where the evaluation tool is located
    * ${EvalToolTitle} - the title of the evaluation tool seen by users of the tool (e.g., "Teaching Questionnaires")
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${MyWorkspaceDashboard} - the direct URL the Evaluation Dashboard on the user's My Workspace site
    * ${URLtoSystem} - the main URL to the system this is running in
    */
	public static final String EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_TEXT = 
		"Course feedback on one or more of your classes is due by ${EarliestEvalDueDate}. You " +
		"are asked to fill out ${EvalToolTitle} in the ${EvalSite} area of ${EvalCLE}, which is " +
		"available from this link:\n\n" +
		"	${MyWorkspaceDashboard}\n\n" + 
		"You will need to provide a login name and password to access the ${EvalToolTitle} " + 
		"site.  This identification is required to ensure that only authorized students " +
		"submit questionnaires and that each student submits only one questionnaire per class.  Note, however, " +
		"that teachers and administrators will not have access to any identifying information you " +
		"submit, and they will not be able to associate specific ratings or comments with " +
		"specific students.\n\n" +
		"Thank you in advance for submitting your ${EvalToolTitle} and helping the " +
		"University maintain and improve the quality of its teaching.\n\n" +
		"If the above link is not working then please follow the Alternate Instructions at the bottom of the message.\n\n" +
		"------------------------------------------------------------\n" +
		"Should you encounter any technical difficulty in viewing the ${EvalToolTitle}, please send an email to ${HelpdeskEmail} " +
		"clearly indicating the problem you encountered. For any other concerns please contact your department.\n\n" +
		"Alternate Instructions: \n" +
		"1) Go to ${URLtoSystem} \n" +
		"2) Enter your username and password and click on 'Login' button. \n" +
		"3) Click on '${EvalToolTitle}' in the left navigation menu under ${EvalSite}. \n" +
		"4) Click on a link under 'Current evaluations to take'. \n";
	
	 /**
	  * EmailTemplate subject: Default subject for reminder single-email-notification 
	  */
	public static final String EMAIL_CONSOLIDATED_REMINDER_DEFAULT_SUBJECT = "Course feedback due by ${EarliestEvalDueDate}\n";


	/**
	 * EmailTemplate message setting:
	 * This is the default template for when the single email per student option is in effect and an evaluation response is outstanding.
	 * Replaceable strings:<br/>
	 * ${EarliestEvalDueDate} - the earliest due date among evaluations available to a user
	 * ${EvalSite} - a description of the site where the evaluation tool is located
	 * ${EvalCLE} - the local name of the Collaboration and Learning Environment where the evaluation tool is used
	 * ${EvalToolTitle} - the title of the evaluation tool seen by users of the tool (e.g., "Teaching Questionnaires")
	 * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
	 * ${MyWorkspaceDashboard} - the direct URL the Evaluation Dashboard on the user's My Workspace site
	 * ${URLtoSystem} - the main URL to the system this is running in
	 */
	public static final String EMAIL_CONSOLIDATED_REMINDER_DEFAULT_TEXT = 
		"Course feedback on one or more of your classes is due by ${EarliestEvalDueDate}. You " +
		"are asked to fill out ${EvalToolTitle} in the ${EvalSite} area of ${EvalCLE}, which is " +
		"available from this link:\n\n" +
		"	${MyWorkspaceDashboard}\n\n" + 
		"<#if InProgress==\"true\">" +
		"Some evaluations have been saved but not completed.  You will need to " +
		"complete and submit these evaluations before they are included in the consolidated results.\n\n" +
		"</#if>" + 
		"You will need to provide a logion name and password to access the ${EvalToolTitle} " + 
		"site.  This identification is required to ensure that only authorized students " +
		"submit questionnaires  and that each student submits only one questionnaire per class.  Note, however, " +
		"that teachers and administrators will not have access to any identifying information you " +
		"submit, and they will not be able to associate specific ratings or comments with " +
		"specific students.\n\n" +
		"Thank you in advance for submitting your ${EvalToolTitle} and helping the " +
		"University maintain and improve the quality of its teaching.\n\n" +
		"If the above link is not working then please follow the Alternate Instructions at the bottom of the message.\n\n" +
		"------------------------------------------------------------\n" +
		"Should you encounter any technical difficulty in viewing the ${EvalToolTitle}, please send an email to ${HelpdeskEmail} " +
		"clearly indicating the problem you encountered. For any other concerns please contact your department.\n\n" +
		"Alternate Instructions: \n" +
		"1) Go to ${URLtoSystem} \n" +
		"2) Enter your username and password and click on 'Login' button. \n" +
		"3) Click on '${EvalToolTitle}' in the left navigation menu under ${EvalSite}. \n" +
		"4) Click on a link under 'Current evaluations to take'. \n";

	 /**
	  * EmailTemplate subject: Default subject for email job completion notification 
	  */
	public static final String EMAIL_JOB_COMPLETED_DEFAULT_SUBJECT = "${JobType} Email Job for Evaluation: ${EvalTitle} has completed\n";
	
	/**
	 * EmailTemplate message setting:
	 * This is the default template for when the single email per student option is in effect and an evaluation response is outstanding.
	 * Replaceable strings:<br/>
	 * ${EvalTitle} - the related evaluation
	 * ${JobType} - the ScheduledInvocationCommand jobType name for the eval email job.
	 * ${NumEmailsSent} - the number of emails sent.
	 * ${EmailsSentList} - list of email addresses (separated by newlines)
	 * ${SampleEmail} - a sample of the email that was sent for this job
	 */
	public static final String EMAIL_JOB_COMPLETED_DEFAULT_TEXT = 
		"The ${JobType} email job has completed for Evaluation: ${EvalTitle}. \n\n" +
		"${NumEmailsSent} emails were sent. \n" +
		"They were sent to the following users: \n" +
		"${EmailsSentList}\n\n" +
		"Sample email: \n" +
		"${SampleEmail}";
	
    /**
     * EmailTemplate subject: Default subject for submission confirmation
     */
    public static final String EMAIL_SUBMITTED_DEFAULT_SUBJECT = "${EvalTitle} submission confirmation";
		
    /**
     * Sent as submission confirmation when response had been saved to the database
     * 
     * Replaceable strings:<br/>
     * ${UserName} - the name used in salutation
     * ${EvalToolTitle} - the title of the evaluation tool seen by users of the tool (e.g., "Teaching Questionnaires")
     * ${EvalTitle} - the title of this evaluation
     * ${TimeStamp} - the time of evaluation submission
     */
    public static final String EMAIL_SUBMITTED_DEFAULT_TEXT = 
	   "This message is a confirmation of your submission for ${EvalTitle}.  " +
	   "It was submitted on ${TimeStamp}.  Thank you for helping the University maintain and improve the quality of its teaching.\n\n" +
	   "Please save this message for your records.\n\n" +
	   "Note that all student responses are kept confidential." +
	   "<#if ShowAllowEditResponsesText == \"true\">\n" +
	   "You may edit your evaluation responses up until the close date for this evaluation.\n" +
       "</#if>\n";
   
		/**
		 * Email text describing where a user should look for the evaluation tool.
		 *
		public static final String EMAIL_EVALUATION_SITE = "My Workspace";
		*/
		
		/**
		 * Email text describing to a user what the evaluation tool is called.
		 *
		public static final String EMAIL_EVALUATION_TOOL = "Course Evaluations";
		*/
}
