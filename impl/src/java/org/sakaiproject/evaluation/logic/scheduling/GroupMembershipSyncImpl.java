/**
 * 
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl;
import org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * 
 *
 */
public class GroupMembershipSyncImpl implements GroupMembershipSync {
	
	private Log logger = LogFactory.getLog(GroupMembershipSyncImpl.class);
	
    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("GroupMembershipSync.execute()");
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap data = jobDetail.getJobDataMap();
		String statusStr = (String) data.get("StatusList");
		if(statusStr == null || statusStr.trim().equals("")) {
			// better throw something?
		} else {
			String[] stateList = statusStr.trim().split(" ");

			for(String state : stateList) {
				List<EvalEvaluation> evals = evaluationService.getEvaluationsByState(state);
				logger.info("====> " + state + " evals  ==> " + evals.size());
				 
				for(EvalEvaluation eval : evals) {
					if(this.evaluationSetupService instanceof EvalEvaluationSetupServiceImpl) {
						logger.info("====> " + state + "          ==> " + eval.getEid() + " using impl");
						((EvalEvaluationSetupServiceImpl) this.evaluationSetupService).synchronizeUserAssignmentsForced(eval, null, true);
					} else {
						logger.info("====> " + state + "          ==> " + eval.getEid() + " using api");
						this.evaluationSetupService.synchronizeUserAssignments(eval.getId(), null);
					}
				}
			}
		}
	}
	
	public void init() {
		logger.info("init()");
	}

}
