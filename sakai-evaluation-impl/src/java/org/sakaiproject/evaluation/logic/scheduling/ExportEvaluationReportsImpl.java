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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalLockManager;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

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
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
    	this.serverConfigurationService = serverConfigurationService;
    }
	protected EvalLockManager lockManager;

	public void setEvalLockManager(EvalLockManager lockManager) {
		this.lockManager = lockManager;
	}

	private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
	public void execute(JobExecutionContext context) throws JobExecutionException {

		Session session = sessionManager.getCurrentSession();
		try {
			session.setUserEid("admin");
			session.setUserId("admin");
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
					//Make the term directories structure
					String dirName = reportPath + "/" + evaluation.getTermId();
					new File(dirName).mkdirs();
					String addDate = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss'.tsv'").format(new Date());
					String outputName = dirName + "/" + evaluation.getTitle() + "_" + addDate;
					logger.info("Writing reports to a basename of "+ outputName);
					outputStream = new FileOutputStream(outputName+".csv", false);
					evaluationService.exportReport(evaluation, null, outputStream, EvalEvaluationService.CSV_RESULTS_REPORT);
					outputStream.close();
					outputStream = new FileOutputStream(outputName+".pdf",false);
					evaluationService.exportReport(evaluation, null, outputStream, EvalEvaluationService.PDF_RESULTS_REPORT);
					outputStream.close();
				}
				catch (FileNotFoundException e) {
					logger.warn("Error writing to file " + outputStream + ". Job aborting");
					return;
				} catch (IOException e) {
					logger.warn("Error writing to file " + outputStream + ". Job aborting");
				}
			}
		} 
		finally {
			session.clear();
		}
	}
	
	public void init() {
		logger.debug("init()");
	}

}