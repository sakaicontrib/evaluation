/**
 * $Id$
 * $URL$
 * EvalEmailConstants.java - evaluation - Feb 29, 2008 9:14:11 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
    * ${EvalTitle} - the title of this evaluation
    * ${EvalStartDate} - the start date of this evaluation
    * ${EvalDueDate} - the due date of this evaluation
    * ${EvalResultsDate} - the view results date of this evaluation
    * ${EvalGroupTitle} - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
    * ${HelpdeskEmail} - the email address for the helpdesk (or the support contact)
    * ${URLtoAddItems} - the direct URL for evaluatees to add items to evals assigned from above
    * ${URLtoTakeEval} - the direct URL for evaluators to take this evaluation
    * ${URLtoViewResults} - the direct URL to view results for this evaluation
    * ${URLtoSystem} - the main URL to the system this is running in
    * ${URLtoAddItems} - the direct URL to add items to an evaluation
    * ${URLtoOptOut} - the direct URL for evaluators to opt in to use this evaluation
    */
   public static final String EMAIL_CREATED_DEFAULT_TEXT = 
      "All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n" +
      "\n" +
      "An evaluation (${EvalTitle}) has been created for: ${EvalGroupTitle}.\n" +
      "\n";

   public static final String EMAIL_CREATED_DEFAULT_TEXT_FOOTER =
      "\n" +
      "The evaluation will run from ${EvalStartDate} to ${EvalDueDate} and the results of the evaluation will be viewable on ${EvalResultsDate}.\n" +
      "\n" +      
      "Thank you for your cooperation.\n" +
      "------------------------------------------------------------\n" +
      "Should you encounter any technical difficulty in working with the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n";

   /**
    * EmailTemplate message setting:
    * This is included text for when an evaluation is created to which an instructor may add items<br/>
    * Replaceable strings:<br/>
    * ${EvalStartDate} - the start date of this evaluation
    * ${URLtoAddItems} - the direct URL for evaluatees to add items to evals assigned from above
    */
   public static final String EMAIL_CREATED_ADD_ITEMS_TEXT = 
      "You may add items to this evaluation until ${EvalStartDate} using the following link:\n" + "${URLtoAddItems} \n";

   /**
    * EmailTemplate message setting:
    * This is the included text for when an evaluation is created to which an instructor may opt in<br/>
    * Replaceable strings:<br/>
    * ${URLtoOptIn} - the direct URL for evaluators to opt in to use this evaluation
    */
   public static final String EMAIL_CREATED_OPT_IN_TEXT = 
      "Its use is optional. To use the evaluation, you must opt in by using the following link:\n ${URLtoOptIn} \n\n" + 
      "If you do not opt in, the evaluation will not be used.";

   /**
    * EmailTemplate message setting:
    * This is the included text for when an evaluation is created to which an instructor may opt out<br/>
    * Replaceable strings:<br/>
    * ${URLtoOptOut} - the direct URL for evaluators to opt in to use this evaluation
    */
   public static final String EMAIL_CREATED_OPT_OUT_TEXT = 
      "Its use is optional. The evaluation will be used unless you opt out by using the following link:\n ${URLtoOptOut} \n\n";


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


}
