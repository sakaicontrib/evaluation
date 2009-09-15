
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

package org.sakaiproject.evaluation.logic.scheduling;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.util.ResourceLoader;

/**
 * This job begins the process of sending a single email (rather, one email per email template) 
 * to a student with one or more active evaluations. This job queues groups that should 
 * receive announcements or reminders of active evaluations. 
 * 
 * A TimerTask looks for groups to receive email and formats individual emails to be sent. 
 * When run in a cluster the work is distributed equally among cluster nodes for improved
 * performance. Parameters for execution are set in Control Email Settings.
 * 
 * Note: This job must be scheduled to run using a Job Scheduler cron trigger.  It is run 
 * once daily when there are active evaluations.
 * 
 * @author rwellis
 *
 */
public class EvalSingleEmailImpl implements Job{
	
	private EvalEmailsLogic evalEmailsLogic;
	public void setEvalEmailsLogic(EvalEmailsLogic evalEmailsLogic) {
		this.evalEmailsLogic = evalEmailsLogic;
	}
	
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}
	
	private static Log log = LogFactory.getLog(EvalSingleEmailImpl.class);
	private static final Log metric = LogFactory.getLog("metrics." + EvalSingleEmailImpl.class.getName());
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	/**
	 * execute is the main method of a Quartz Job.
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long start = System.currentTimeMillis();
		int groups = 0;
		if (metric.isInfoEnabled()) {
			metric.info("metric " + this + ".execute(): Start.");
		}
		try {
			if (settingsValid() && isActiveEvaluation()) {
				// queue groups to receive email
				groups = evalEmailsLogic.queueEmailGroups();
			}
		}
		catch(Exception e) {
			log.error(this + ".execute() " + e);
		}
		long end = System.currentTimeMillis();
		float seconds = (end - start) / 1000;
		if (metric.isInfoEnabled()) {
			// TODO log number of groups written to table
			metric.info("metric " + this + ".execute() took " + seconds + " seconds to queue " + groups + " group(s) to receive email. Done.");
		}
	}
	
	/**
	 * Check that there is at least one active evaluation in the system.
	 * 
	 * @return true if an active evaluation was found, false otherwise
	 */
	private boolean isActiveEvaluation() {
		boolean active = evaluationService.isEvaluationWithState(EvalConstants.EVALUATION_STATE_ACTIVE);
		if(!active) {
			if (log.isInfoEnabled()) {
				log.info("EvalSingleEmailImpl.execute() found no active evaluations.");
			}
		}
		return active;
	}
	
	/**
	 * Check settings that affect single email notification.
	 * 
	 * @return true if execution should proceed, false otherwise
	 */
	private boolean settingsValid() {
		boolean check = true;
		Boolean singleEmail = (Boolean)evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL);
		if(singleEmail == null) {
			log.error(this + ".settingsValid(): EvalSettings.ENABLE_SINGLE_EMAIL is null.");
			check = false;
		}
		if(!singleEmail.booleanValue()) {
			log.error(this + ".settingsValid(): EvalSettings.ENABLE_SINGLE_EMAIL is false.");
			check = false;
		}
		if(((String)evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION)).equals(EvalConstants.EMAIL_DELIVERY_NONE)) {
			if(!((Boolean)evalSettings.get(EvalSettings.LOG_EMAIL_RECIPIENTS)).booleanValue()) {
				log.warn(this + ".settingsValid(): EvalSettings.EMAIL_DELIVERY_OPTION is EMAIL_DELIVERY_NONE " +
						"and EvalSettings.LOG_EMAIL_RECIPIENTS is false: EvalSingleEmail has no work to do.");
				check = false;
			}
		}
		if(!check) {
			log.error(this + ".settingsValid(): settings are inconsistent with job execution.");
		}
		return check;
	}
}
