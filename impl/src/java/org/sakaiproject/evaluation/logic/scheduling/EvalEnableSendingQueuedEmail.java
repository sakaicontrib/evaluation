package org.sakaiproject.evaluation.logic.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalSettings;

public class EvalEnableSendingQueuedEmail implements Job{
	
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	//diagnostics
	private static Log log = LogFactory.getLog(EvalEnableSendingQueuedEmail.class);

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		evalSettings.set("EMAIL_SEND_QUEUED_ENABLED", "true");
		
		if(log.isInfoEnabled())
			log.info("EvalEnableSendingQueuedEmail.execute() set EMAIL_SEND_QUEUED_ENABLED to true.");
	}
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	public void destroy() {
		if(log.isDebugEnabled()) log.debug("destroy()");
	}
}
