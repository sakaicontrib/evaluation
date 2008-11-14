package org.sakaiproject.evaluation.logic.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalSettings;

public class EvalDisableSendingQueuedEmail implements Job{
	
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	//diagnostics
	private static Log log = LogFactory.getLog(EvalDisableSendingQueuedEmail.class);

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		evalSettings.set("EMAIL_SEND_QUEUED_ENABLED", "false");
		
		if(log.isInfoEnabled())
			log.info("EvalDisableSendingQueuedEmail.execute() set EMAIL_SEND_QUEUED_ENABLED to false.");
	}
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	public void destroy() {
		if(log.isDebugEnabled()) log.debug("destroy()");
	}
}
