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
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
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
    
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings settings) {
        this.evalSettings = settings;
    }

    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("GroupMembershipSync.execute()");
		String syncServerId = (String) this.evalSettings.get(EvalSettings.SYNC_SERVER);
		String thisServerId = this.externalLogic.getServerId();
		if(thisServerId != null && thisServerId.equals(syncServerId)) {
			JobDetail jobDetail = context.getJobDetail();
			JobDataMap data = jobDetail.getJobDataMap();
			String statusStr = (String) data.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST);
			logger.info("GroupMembershipSync.execute() starting sync of evals by state: " + statusStr);
			if(statusStr == null || statusStr.trim().equals("")) {
				// better throw something?
			} else {
				String[] stateList = statusStr.trim().split(" ");
				
				logger.info("GroupMembershipSync.execute() syncing " + statusStr);
	
				for(String state : stateList) {
					List<EvalEvaluation> evals = evaluationService.getEvaluationsByState(state);
					int count = 0;
					if(evals != null) {
						count = evals.size();
					}
					if(logger.isInfoEnabled()) {
						StringBuilder buf1 = new StringBuilder();
						buf1.append("GroupMembershipSync.execute() syncing ");
						buf1.append(count);
						buf1.append("groups for evals in state: ");
						buf1.append(state);
						logger.info(buf1.toString());
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
			logger.info("GroupMembershipSync.execute() done with sync of evals by state: " + statusStr);
		}
	}
	
	public void init() {
		logger.debug("init()");
	}

}
