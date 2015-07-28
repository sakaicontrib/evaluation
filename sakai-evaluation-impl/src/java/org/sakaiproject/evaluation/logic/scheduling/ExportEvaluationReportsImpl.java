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
/**
 * 
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Job to export evaluation reports for a term 
 *
 */
public class ExportEvaluationReportsImpl implements ExportEvaluationReports {
	
	private Log logger = LogFactory.getLog(ExportEvaluationReportsImpl.class);
	
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
    
    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationSErvice(ServerConfigurationService serverConfigurationService) {
    	this.serverConfigurationService = serverConfigurationService;
    }

    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("ExportEvaluationReports.execute()");
        String termId = context.getMergedJobDataMap().getString("term.id");
        List<EvalEvaluation> evaluations = evaluationService.getEvaluationsByTermId(termId);
        String reportPath = serverConfigurationService.getString("evaluation.exportjob.outputlocation");
        if (reportPath == null) {
			logger.warn("You need to define the evaluation.exportjob.outputlocation property to be a directory to write these reports before running this job");
			return;
        }
        File f = new File(reportPath);
        if (!f.isDirectory()) {
			logger.warn("You need to define the evaluation.exportjob.outputlocation property to be a directory to write these reports before running this job");
			return;
        }
        
        //Maybe make a termId folder for these to go in?
        for (EvalEvaluation evaluation: evaluations) {
        	OutputStream outputStream = null;
        	try {
        		String outputName = reportPath + "/" + evaluation.getTermId() + evaluation.getTitle() + evaluation.getId();
        		logger.info("Writing files to a basename of "+outputName);
        		outputStream = new FileOutputStream(outputName+".csv", false);
        		evaluationService.exportReport(evaluation, null, outputStream, "csvResultsReport");
        		outputStream.close();
        		outputStream = new FileOutputStream(outputName+".pdf",false);
        		evaluationService.exportReport(evaluation, null, outputStream, "pdfResultsReport");
        		outputStream.close();
        	}
        	catch (FileNotFoundException e) {
        		logger.warn("Error writing to file " + outputStream + ". Job aborting");
        		return;
        	} catch (IOException e) {
        		logger.warn("Error writing to file " + outputStream + ". Job aborting");
			}
        }

		/*
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
					int count = evals.size();
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
		*/
	}
	
	public void init() {
		logger.debug("init()");
	}

}
