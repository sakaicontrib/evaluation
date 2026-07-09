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
package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.toolaccess.EvaluationAccessAPI;
import org.sakaiproject.evaluation.toolaccess.ToolApi;
import org.springframework.context.MessageSource;

import com.opencsv.CSVParser;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class ReportExporterBean implements ToolApi {

    private ReportMessageSource reportMessageSource;

    public void setMessageSource(MessageSource messageSource) {
      this.reportMessageSource = ReportMessageSource.from(messageSource);
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }
    
    private Map<String, ReportExporter> exportersMap;
    public void setExportersMap(Map<String, ReportExporter> exportersMap) {
        this.exportersMap = exportersMap;
    }

    private EvaluationAccessAPI evaluationAccessAPI = null;

    public void setEvaluationAccessAPI(EvaluationAccessAPI s) {
      evaluationAccessAPI = s;
    }

    //Export report with no evaluateeId (for single export)
    public void exportReport(EvalEvaluation evaluation, String groupIds,OutputStream outputStream, String exportType) {
    	exportReport(evaluation,groupIds,null,outputStream,exportType);
    }
    
    //Special convenience method to allow passing of groupIds as a CSV
    public void exportReport(EvalEvaluation evaluation, String groupIds, String evaluateeId, OutputStream outputStream, String exportType) {
        exportReport(evaluation, groupIds, evaluateeId, outputStream, exportType, false);
    }

    public void exportReport(EvalEvaluation evaluation, String groupIds, String evaluateeId, OutputStream outputStream,
                             String exportType, boolean newReportStyle) {
    	String[] groupIdsArray = new String [] {};
    	CSVParser parser= new CSVParser();
    	if (groupIds != null) {
    		try {
    			groupIdsArray = parser.parseLine(groupIds);
    		} catch (IOException e) {
    			//Is fine if this happens, empty array still
    		}
    	}
        exportReport(evaluation, groupIdsArray, evaluateeId, outputStream, exportType, newReportStyle);
    }

    //Allows for general report exporting
    public void exportReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, String exportType) {
        exportReport(evaluation, groupIds, evaluateeId, outputStream, exportType, false);
    }

    public void exportReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream,
                             String exportType, boolean newReportStyle) {
      ReportExporter exporter = exportersMap.get(exportType);
      if (exporter == null) {
        throw new IllegalArgumentException("No exporter found for export type: " + exportType);
      }
      if (log.isDebugEnabled()) {
        log.debug("Found exporter: " + exporter.getClass() + " for export type " + exportType);
      }
      if (groupIds == null || groupIds.length==0) {
        //Get the default groupIds
    	String[] groupIdsArray = new String [] {};
        groupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluation).toArray(groupIdsArray);
      }

      // do a permission check
      if (!reportingPermissions.canViewEvaluationResponses(evaluation, groupIds)) {
        String currentUserId = commonLogic.getCurrentUserId();
        throw new SecurityException("Invalid user attempting to access report downloads: "
            + currentUserId);
      }

      exporter.setMessageSource(reportMessageSource);
      if (EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL.equals(exportType)) {
        exporter.buildReport(evaluation, groupIds, evaluateeId, outputStream, newReportStyle);
      } else {
        exporter.buildReport(evaluation, groupIds, outputStream, newReportStyle);
      }

    }

    public void init() {
      evaluationAccessAPI.setToolApi(this);
    }

}
