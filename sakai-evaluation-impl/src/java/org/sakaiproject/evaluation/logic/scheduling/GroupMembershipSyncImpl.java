/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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

public class GroupMembershipSyncImpl implements GroupMembershipSync {
	
	private static final Log LOG = LogFactory.getLog(GroupMembershipSyncImpl.class);
	
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
		LOG.debug("GroupMembershipSync.execute()");
		String syncServerId = (String) this.evalSettings.get(EvalSettings.SYNC_SERVER);
		String thisServerId = this.externalLogic.getServerId();
		if(thisServerId != null && thisServerId.equals(syncServerId)) {
			JobDetail jobDetail = context.getJobDetail();
			JobDataMap data = jobDetail.getJobDataMap();
			String statusStr = (String) data.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST);
			LOG.info("GroupMembershipSync.execute() starting sync of evals by state: " + statusStr);
			if(statusStr == null || statusStr.trim().equals("")) {
				// better throw something?
			} else {
				String[] stateList = statusStr.trim().split(" ");
				
				LOG.info("GroupMembershipSync.execute() syncing " + statusStr);
	
				for(String state : stateList) {
					List<EvalEvaluation> evals = evaluationService.getEvaluationsByState(state);
					int count = evals.size();
					if(LOG.isInfoEnabled()) {
						StringBuilder buf1 = new StringBuilder();
						buf1.append("GroupMembershipSync.execute() syncing ");
						buf1.append(count);
						buf1.append("groups for evals in state: ");
						buf1.append(state);
						LOG.info(buf1.toString());
					}
					for(EvalEvaluation eval : evals) {
						if(this.evaluationSetupService instanceof EvalEvaluationSetupServiceImpl) {
							if(LOG.isDebugEnabled()) {
								StringBuilder buf = new StringBuilder();
								buf.append("====> ");
								buf.append(state);
								buf.append("          ==> ");
								buf.append(eval.getEid());
								buf.append(" using impl");
								LOG.debug(buf.toString());
							}
							try {
								((EvalEvaluationSetupServiceImpl) this.evaluationSetupService).synchronizeUserAssignmentsForced(eval, null, true);
							} catch(IllegalStateException e) {
								StringBuilder buf = new StringBuilder();
								buf.append("Unable to user assignments for eval (");
								buf.append(eval.getId());
								buf.append(") due to IllegalStateException: ");
								buf.append(e.getMessage());
								LOG.warn(buf.toString());
								
								// TODO: should update the state so it is not selected next time ??
							}
						} else {
							if(LOG.isDebugEnabled()) {
								StringBuilder buf = new StringBuilder();
								buf.append("====> ");
								buf.append(state);
								buf.append("          ==> ");
								buf.append(eval.getEid());
								buf.append(" using api");
								LOG.debug(buf.toString());
							}
							this.evaluationSetupService.synchronizeUserAssignments(eval.getId(), null);
						}
					}
				}
			}
			LOG.info("GroupMembershipSync.execute() done with sync of evals by state: " + statusStr);
		}
	}
	
	public void init() {
		LOG.debug("init()");
	}

}
