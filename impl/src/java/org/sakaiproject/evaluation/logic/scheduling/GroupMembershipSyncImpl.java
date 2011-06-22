/**
 * 
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl;
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

    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("GroupMembershipSync.execute()");
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap data = jobDetail.getJobDataMap();
		String statusStr = (String) data.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST);
		if(statusStr == null || statusStr.trim().equals("")) {
			// better throw something?
		} else {
			String[] stateList = statusStr.trim().split(" ");

			for(String state : stateList) {
				List<EvalEvaluation> evals = evaluationService.getEvaluationsByState(state);
				if(logger.isDebugEnabled()) {
					StringBuilder buf = new StringBuilder();
					buf.append("====> ");
					buf.append(state);
					buf.append(" evals  ==> ");
					buf.append(evals.size());
					logger.debug(buf.toString());
				}
				 
				for(EvalEvaluation eval : evals) {
					if(this.evaluationSetupService instanceof EvalEvaluationSetupServiceImpl) {
						if(logger.isDebugEnabled()) {
							StringBuilder buf = new StringBuilder();
							buf.append("====> ");
							buf.append(state);
							buf.append("          ==> ");
							buf.append(eval.getEid());
							buf.append(" using impl");
							logger.debug(buf.toString());
						}
						try {
							((EvalEvaluationSetupServiceImpl) this.evaluationSetupService).synchronizeUserAssignmentsForced(eval, null, true);
						} catch(IllegalStateException e) {
							StringBuilder buf = new StringBuilder();
							buf.append("Unable to user assignments for eval (");
							buf.append(eval.getId());
							buf.append(") due to IllegalStateException: ");
							buf.append(e.getMessage());
							logger.warn(buf.toString());
							
							// TODO: should update the state so it is not selected next time ??
						}
					} else {
						if(logger.isDebugEnabled()) {
							StringBuilder buf = new StringBuilder();
							buf.append("====> ");
							buf.append(state);
							buf.append("          ==> ");
							buf.append(eval.getEid());
							buf.append(" using api");
							logger.debug(buf.toString());
						}
						this.evaluationSetupService.synchronizeUserAssignments(eval.getId(), null);
					}
				}
			}
		}
	}
	
	public void init() {
		logger.debug("init()");
	}

}
