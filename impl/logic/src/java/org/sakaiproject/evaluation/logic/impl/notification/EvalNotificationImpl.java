package org.sakaiproject.evaluation.logic.impl.notification;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;

/**
 * Sends email notification to participants in an evaluation.
 * @author rwellis
 *
 */
public class EvalNotificationImpl implements StatefulJob {
	
	//TODO add to beans in components.xml
	
	//IoC
	private EvalEmailsLogic emails;
	public void setEvalEmailsLogic(EvalEmailsLogic emails) {
		this.emails = emails;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//get JobDataMap from context
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		
		//get notification from JobDataMap
		//get recipents from JobDataMap
		//send notification to each
		//if a reminder, check if evaluation has response from person before sending reminder
		
	}

}
