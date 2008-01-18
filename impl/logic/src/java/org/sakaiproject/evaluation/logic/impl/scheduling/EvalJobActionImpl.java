/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.impl.scheduling;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobAction;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

public class EvalJobActionImpl implements EvalJobAction{
	
	private static Log log = LogFactory.getLog(EvalJobActionImpl.class);
	
	private final String EVENT_EVAL_START                  = "evaluation.state.start";
	private final String EVENT_EVAL_DUE                    = "evaluation.state.due";
	private final String EVENT_EVAL_STOP                   = "evaluation.state.stop";
	private final String EVENT_EVAL_VIEWABLE               = "evaluation.state.viewable";
	private final String EVENT_EVAL_VIEWABLE_INSTRUCTORS   = "evaluation.state.viewable.inst";
	private final String EVENT_EVAL_VIEWABLE_STUDENTS      = "evaluation.state.viewable.stud";
	private final String EVENT_EMAIL_REMINDER              = "evaluation.email.reminder";
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}

   private EvalEvaluationsLogic evalEvaluationsLogic;
   public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
      this.evalEvaluationsLogic = evalEvaluationsLogic;
   }
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }
   
   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
	   this.evalSettings = evalSettings;
   }
	
	public void init()  {
		log.debug("EvalJobActionImpl.init()");
	}
	
	public EvalJobActionImpl() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#jobAction(java.lang.Long)
	 */
	public void execute(Long evaluationId, String jobType) {
		
		if(log.isDebugEnabled())
			log.debug("EvalJobLogicImpl.jobAction(" + evaluationId + "," + jobType + ")");
		try
		{
			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
			
			//fix EvalEvaluation state
			String state = evalEvaluationsLogic.updateEvaluationState(evaluationId);
			if(log.isDebugEnabled())
				log.debug("evaluation state " + state + " saved");
			
			//Note: if consolidated email is set, available and reminder email is sent by EvalConsolidatedNotificationJob
			Boolean consolidatedNotification = (Boolean)evalSettings.get(EvalSettings.CONSOLIDATE_NOTIFICATION);
			
			//dispatch to send email and/or schedule jobs based on jobType
			if(EvalConstants.JOB_TYPE_CREATED.equals(jobType)) {
				//if opt-in, opt-out, or questions addable, notify instructors
				sendCreatedEmail(evaluationId);
			}
			//TODO JOB_TYPE_LATE_OPT_IN late opt in job type  emails.sendEvalAvailableGroupNotification(evaluationId, evalGroupId);
			else if(EvalConstants.JOB_TYPE_ACTIVE.equals(jobType)) {
				externalLogic.registerEntityEvent(EVENT_EVAL_START, eval);
				//if consolidating email, skip sending available email here
				if(!consolidatedNotification.booleanValue())
					sendAvailableEmail(evaluationId);
				evalJobLogic.scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_DUE);
				//if consolidating email, skip scheduling a reminder
				if(!consolidatedNotification.booleanValue())
					if(eval.getReminderDays().intValue() != 0)
						evalJobLogic.scheduleReminder(eval.getId());
			}
			else if(EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) {
				//if statement below should be superfluous, because we shouldn't have a JOB_TYPE_REMINDER if consolidatedEmail is true
				if(!consolidatedNotification.booleanValue()) {
					if(eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE) && eval.getReminderDays().intValue() != 0) {
						if(eval.getDueDate().after(new Date())) {
							sendReminderEmail(evaluationId);
							evalJobLogic.scheduleReminder(eval.getId());
						}
					}
				}
			}
			else if(EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
				externalLogic.registerEntityEvent(EVENT_EVAL_DUE, eval);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.jobAction scheduleJob(" + eval.getId() + "," + eval.getStopDate() + "," + EvalConstants.JOB_TYPE_CLOSED + ")");
				evalJobLogic.scheduleJob(eval.getId(), eval.getStopDate(), EvalConstants.JOB_TYPE_CLOSED);
			}
			else if(EvalConstants.JOB_TYPE_CLOSED.equals(jobType)) {
				externalLogic.registerEntityEvent(EVENT_EVAL_STOP, eval);
				//schedule results viewable by owner - admin notification
				evalJobLogic.scheduleJob(eval.getId(), eval.getViewDate(), EvalConstants.JOB_TYPE_VIEWABLE);
				if(! eval.getResultsPrivate().booleanValue()) {
					if(eval.getInstructorsDate() != null) {
						Date instructorViewDate = eval.getInstructorsDate();
						//schedule results viewable by instructors notification
						evalJobLogic.scheduleJob(eval.getId(), instructorViewDate, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS);
					}
					if(eval.getStudentsDate() != null) {
						Date studentViewDate = eval.getStudentsDate();
						//schedule results viewable by students notification
						evalJobLogic.scheduleJob(eval.getId(), studentViewDate, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS);
					}
				}
			}
			else if(EvalConstants.JOB_TYPE_VIEWABLE.equals(jobType))  {
				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE, eval);
				//send results viewable notification to owner if private, or all if not
				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
			}
			else if(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS.equals(jobType))  {
				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_INSTRUCTORS, eval);
				//send results viewable notification to owner if private, or all if not
				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
			}
			else if(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS.equals(jobType))  {
				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_STUDENTS, eval);
				//send results viewable notification to owner if private, or all if not
				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
			}
		}
		catch(Exception e) {
			log.error("jobAction died horribly:" + e.getMessage(), e);
			throw new RuntimeException(e); // die horribly, as it should -AZ
		}
	}
	/**
	 * Send email to evaluation participants that an evaluation 
	 * is available for taking by clicking the contained URL
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendAvailableEmail(Long evalId) {
		//For now, we always want to include the evaluatees in the evaluations
		boolean includeEvaluatees = true;
		try {
			String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
			if(log.isDebugEnabled())
				log.debug("EvalJobLogicImpl.sendAvailableEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
		}
		catch(Exception e) {
			log.error(this + ".sendAvailableEmail(" + evalId + ")" + e);
		}
	}

	/**
	 *  Send email that an evaluation has been created</br>
	 *  
	 * @param evalId the EvalEvaluation id
	 */
	public void sendCreatedEmail(Long evalId) {
		boolean includeOwner = true;
		try {
			String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
			if(log.isDebugEnabled())
				log.debug("EvalJobLogicImpl.sendCreatedEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
		}
		catch(Exception e) {
			log.error(this + ".sendCreatedEmail(" + evalId + ")" + e);
		}
	}

	/**
	 * Send a reminder that an evaluation is available 
	 * to those who have not responded
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendReminderEmail(Long evalId) {
		try {
			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evalId);
			if(eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE) &&
					eval.getReminderDays().intValue() != 0) {
				externalLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);
				String includeConstant = EvalConstants.EMAIL_INCLUDE_NONTAKERS;
				String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendReminderEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
			}
		}
		catch(Exception e) {
			log.error(this + ".sendReminderEmail(" + evalId + ")" + e);
		}
	}

	/**
	 * Send email that the results of an evaluation may be viewed now.</br>
	 * Notification may be sent to owner only, instructors and students together or
	 * separately.
	 * 
	 * @param evalId the EvalEvaluation id
	 * @param the job type from EvalConstants
	 */
	public void sendViewableEmail(Long evalId, String jobType, Boolean resultsPrivate) {
		/*
		 * TODO when booleans below are set dynamically, replace the use of job type to distinguish
		 * recipients with the setting of these parameters before calling emails.sendEvalResultsNotifications().
		 * Then one job type JOB_TYPE_VIEWABLE can be scheduled as needed.
		 */
		boolean includeEvaluatees = true;
		boolean includeAdmins = true;
		
		try {
			//if results are private, only send notification to owner
			if(resultsPrivate.booleanValue()) {
				includeEvaluatees = false;
				includeAdmins = false;
				emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
			}
			else {
				emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
			}
		}
		catch(Exception e) {
			log.error(this + ".sendViewableEmail(" + evalId + "," +  jobType + "," + includeAdmins + ")" + e);
		}
	}

}

