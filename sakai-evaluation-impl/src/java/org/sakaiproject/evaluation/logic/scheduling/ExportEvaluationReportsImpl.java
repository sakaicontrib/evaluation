/**
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalLockManager;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Job to export evaluation reports for a term 
 *
 */
@Slf4j
public class ExportEvaluationReportsImpl implements ExportEvaluationReports {


    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
    	this.serverConfigurationService = serverConfigurationService;
    }
    
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	protected EvalLockManager lockManager;

	public void setEvalLockManager(EvalLockManager lockManager) {
		this.lockManager = lockManager;
	}

	private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
    
    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
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
			log.debug("ExportEvaluationReports.execute()");
			String termId = context.getMergedJobDataMap().getString("term.id");
			Boolean mergeGroups = context.getMergedJobDataMap().getBoolean("merge.groups");
			List<EvalEvaluation> evaluations = evaluationService.getEvaluationsByTermId(termId);
			String reportPath = serverConfigurationService.getString("evaluation.exportjob.outputlocation");
			if (reportPath == null) {
				log.warn("You need to define the evaluation.exportjob.outputlocation property to be a directory to write these reports before running this job");
				return;
			}
			File f = new File(reportPath);
			if (!f.isDirectory()) {
				log.warn("You need to define the evaluation.exportjob.outputlocation property to be a directory to write these reports before running this job");
				return;
			}
			
			log.info("Evaluation query returned" + evaluations.size() + " results to export for " + termId);
			

			//Maybe make a termId folder for these to go in?
			for (EvalEvaluation evaluation: evaluations) {
				OutputStream outputStream = null;
				try {
					String [] evalGroupIds;
					evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluation).toArray(new String[] {});
					
					//Make the term directories structure
					String dirName = reportPath + "/" + evaluation.getTermId();
					new File(dirName).mkdirs();
					String addDate = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
					//Clean up non-alpha characters from title
					String evaluationTitle = evaluation.getTitle();
					evaluationTitle = evaluationTitle.replaceAll("\\W+","_");

					/* This is where merged and non-merged groups will differ */
					if (mergeGroups == true) {
						String outputName = dirName + "/" + evaluationTitle + "_" + addDate;
						log.info("Writing reports to a basename of "+ outputName);
						outputStream = new FileOutputStream(outputName+".csv", false);
						evaluationService.exportReport(evaluation, evalGroupIds, null, outputStream, EvalEvaluationService.CSV_RESULTS_REPORT);
						outputStream.close();
						outputStream = new FileOutputStream(outputName+".pdf",false);
						evaluationService.exportReport(evaluation, evalGroupIds, null, outputStream, EvalEvaluationService.PDF_RESULTS_REPORT);
					}
					else {
						//Export each group in it's own file
						for (String groupId: evalGroupIds) {
							Group group = siteService.findGroup(groupId);		
							String groupTitle = groupId;
							//If it's not null the group exists in the system, so look up the title
							if (group != null) {
								groupTitle = group.getTitle();
							}
							groupTitle = groupTitle.replaceAll("\\W+","_");
							String outputName = dirName + "/" + evaluationTitle + "_" + groupTitle + "_" + addDate;
							log.info("Writing reports to a basename of "+ outputName);
							outputStream = new FileOutputStream(outputName+".csv", false);
							evaluationService.exportReport(evaluation, new String[] {groupId}, null, outputStream, EvalEvaluationService.CSV_RESULTS_REPORT);
							outputStream.close();
							outputStream = new FileOutputStream(outputName+".pdf",false);
							evaluationService.exportReport(evaluation, new String[] {groupId}, null, outputStream, EvalEvaluationService.PDF_RESULTS_REPORT);
						}
					}
				}
				catch (FileNotFoundException e) {
					log.warn("Error writing to file " + outputStream + ". Job aborting");
					return;
				} catch (IOException e) {
					log.warn("Error writing to file " + outputStream + ". Job aborting");
					return;
				} 
				catch (SecurityException e) {
					log.info("Security exception thrown for evaluation (" + evaluation.getId() + ") skipping");
				}
				catch (Exception e) {
					log.warn("Unknown exception " + e.getMessage() + " found. Job aborting");
					return;
				}

			}
		} 
		finally {
			session.clear();
		}
	}
	
	public void init() {
		log.debug("init()");
	}

}