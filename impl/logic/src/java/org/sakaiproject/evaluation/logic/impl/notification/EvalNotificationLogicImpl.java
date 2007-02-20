package org.sakaiproject.evaluation.logic.impl.notification;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Logic and actions related to email notification sent to those
 * participating in an evaluation.
 * 
 * @author rwellis
 *
 */
public class EvalNotificationLogicImpl {
	private static Log log = LogFactory.getLog(EvalNotificationLogicImpl.class);
	
	//job parameters
	private static final String JOB_DATA_MAP_NOTIFICATION = "Notification";
	private static final String JOB_DATA_MAP_RECIPIENTS = "Recipients";
	
	//well-known group names
	private static final String QRTZ_GRP_EVAL_CREATED = "EvalCreated";
	private static final String QRTZ_GRP_EVAL_DUE = "EvalDue";
	private static final String QRTZ_GRP_EVAL_REMINDER = "EvalReminder";
	private static final String QRTZ_GRP_EVAL_CLOSED = "EvalClosed";
	private static final String QRTZ_GRP_EVAL_VIEWABLE = "EvalViewable";
	
	Scheduler scheduler;
	EvalNotificationImpl notificationJob = new EvalNotificationImpl();
	
	//	TODO add to beans in components.xml
	
	//IoC
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	/**
	 * Create a notification scheduler if one is not running.
	 * @throws SchedulerException
	 */
	public void init() throws SchedulerException {
		log.debug("init");
		
		if(scheduler == null) {
			try
			{
				scheduler = StdSchedulerFactory.getDefaultScheduler();
			}
			catch(SchedulerException e)
			{
				throw new SchedulerException("Notification scheduler could not be created. "+ e);
			}
		}
	}
	
	/**
	 * No argument constructor.
	 *
	 */
	public EvalNotificationLogicImpl() {
		
	}
	
	/**
	 * Method to begin the series of notifications associated with an evaluation when
	 * an evaluation is first saved.
	 * @param eval
	 * @throws Exception
	 */
	public void processNewEvaluation(EvalEvaluation eval) throws Exception {
		
		//TODO pass evaluation id and get evaluation from EvalEvaluationLogic
		if(eval == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation was null.");
		
		String state = EvalUtils.getEvaluationState(eval);
		if(state == null)
		{
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation state was null.");
		}
		
		if (!EvalConstants.EVALUATION_STATE_INQUEUE.equals(state))
		{
			throw new Exception("Notification of a new evaluation failed, because the evaluation state was unknown.");
		}
		
		//TODO use AZ classes
		String notification = "what goes here?";
		String[] recipients = null;
		
		//parameters passed to New Evaluation notification job
		JobDetail jobDetail = new JobDetail(eval.getId().toString(), QRTZ_GRP_EVAL_CREATED, notificationJob.getClass());
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put(JOB_DATA_MAP_NOTIFICATION,notification);
		jobDataMap.put(JOB_DATA_MAP_RECIPIENTS, recipients);
		
		Date startTime = new Date();
		startTime.setTime(startTime.getTime() + 600000);
		
		//delay Evaluation Created notification for 10 minutes as a grace period for final changes
		SimpleTrigger trigger = new SimpleTrigger(eval.getId().toString(), QRTZ_GRP_EVAL_CREATED, startTime);
		
		//set trigger's misfire instructions
		scheduleStatefulJob(jobDetail, trigger);

	}

	/** 
	 * Check and make an adjustment if a change in an evaluation's properties requires that a notification
	 * schedule be changed to match a new evaluation date.
	 * @param eval
	 */
	public void processEvaluationChange(EvalEvaluation eval) {
		
		String state = EvalUtils.getEvaluationState(eval);

		//dispatch based on current evaluation status
		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
			
			if(log.isWarnEnabled())
				log.warn("Evaluation ("+eval.getTitle()+") in UNKNOWN state");
			
		}
		else if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(state)){
			
			/* get trigger from scheduler using well-known id (group + name) Scheduler.getTrigger()
			 * if not trigger it must have run and been removed
			 */
			if(log.isWarnEnabled())
				log.warn("Evaluation ("+eval.getTitle()+") in UNKNOWN state");
			
		}
		else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
			
		}
		else if(EvalConstants.EVALUATION_STATE_DUE.equals(state)) {
			
		}
		else if(EvalConstants.EVALUATION_STATE_CLOSED.equals(state)) {
			
		}
		else if(EvalConstants.EVALUATION_STATE_VIEWABLE.equals(state)) {
			
		}
		else {
			
		}
	}
		
	/**
	 * Schedule a notification job.
	 * @param jobDetail The parameters passed to the job.
	 * @param trigger The schedule to use for running the job.
	 * @throws SchedulerException
	 */
	private void scheduleStatefulJob(JobDetail jobDetail, SimpleTrigger trigger)
		throws SchedulerException
		{
			
			if(scheduler == null) {
				try
				{
					scheduler = StdSchedulerFactory.getDefaultScheduler();
				}
				catch(SchedulerException e)
				{
					throw new SchedulerException("Notification scheduler could not be created. "+ e);
				}
			}
		
		scheduler.scheduleJob(jobDetail, trigger);
		scheduler.start();
	}
}
