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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

    // Section awareness/new report style bindings
    public String   viewID          = "";
    public String   fileName        = "";
    public String[] groupIDs        = new String[]{};
    public Long     templateID      = 0L;
    public Long     evalID          = 0L;
    public boolean  newReportStyle  = false;

    private MessageSource messageSource;
    
    // the real MessageLocator won't work except in an RSAC session, which we can't reasonably create
    // this is a reasonable fake, given that we have no way to get a locale when exporting without one
    // Taken from LessonBuilder
    // This probably could be fixed in RSF to avoid the error and use a simpler version

    public MessageSource getMessageSource() {
      return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
      this.messageSource = messageSource;
    }

    public class MyMessageLocator extends EvalMessageLocator {
      @Override
      public Object[] getMessages(String[] codes, Object[] args) {
        if (codes != null) {
          for (String code : codes) {
            try {
              return new Object[]{ messageSource.getMessage(code, args, Locale.getDefault()) };
            } catch (Exception e) {
              log.warn(e.getLocalizedMessage(), e);
            }
          }
          return new Object[]{ codes[0] };
        }
        return new Object[]{ "" };
      }
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }
    
    private Map<String, ReportExporter> exportersMap;
    public void setExportersMap(Map<String, ReportExporter> exportersMap) {
        this.exportersMap = exportersMap;
    }

    EvaluationAccessAPI evaluationAccessAPI = null;

    public void setEvaluationAccessAPI(EvaluationAccessAPI s) {
      evaluationAccessAPI = s;
    }

    //Export report with no evaluateeId (for single export)
    public void exportReport(EvalEvaluation evaluation, String groupIds,OutputStream outputStream, String exportType) {
    	exportReport(evaluation,groupIds,null,outputStream,exportType);
    }
    
    //Special convenience method to allow passing of groupIds as a CSV
    public void exportReport(EvalEvaluation evaluation, String groupIds, String evaluateeId, OutputStream outputStream, String exportType) {
    	String[] groupIdsArray = new String [] {};
    	CSVParser parser= new CSVParser();
    	if (groupIds != null) {
    		try {
    			groupIdsArray = parser.parseLine(groupIds);
    		} catch (IOException e) {
    			//Is fine if this happens, empty array still
    		}
    	}
    	exportReport(evaluation,groupIdsArray,evaluateeId,outputStream,exportType);
    }

    //Allows for general report exporting
    public void exportReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, String exportType) {
      ReportExporter exporter = exportersMap.get(exportType);
      if (exporter == null) {
        throw new IllegalArgumentException("No exporter found for ViewID: " + exportType);
      }
      if (log.isDebugEnabled()) {
        log.debug("Found exporter: " + exporter.getClass() + " for drvp.viewID " + exportType);
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

      MyMessageLocator messageLocator = new MyMessageLocator();
      exporter.setEvalMessageLocator(messageLocator);
      if (EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL.equals(exportType)) {
        exporter.buildReport(evaluation, groupIds, evaluateeId, outputStream, newReportStyle);
      } else {
        exporter.buildReport(evaluation, groupIds, outputStream, newReportStyle);
      }

    }

    public void init() {
      evaluationAccessAPI.setToolApi(this);
    }

    // Utility methods
    private boolean isCSVTakers ( String viewID ) { return viewID.equals( EvalEvaluationService.CSV_TAKERS_REPORT ); }
    private boolean isCSV       ( String viewID ) { return viewID.equals( EvalEvaluationService.CSV_RESULTS_REPORT ); }
    private boolean isPDF       ( String viewID ) { return (viewID.equals( EvalEvaluationService.PDF_RESULTS_REPORT ) || 
                                                           (viewID.equals( EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL ))); }

    private OutputStream getOutputStream(HttpServletResponse response){
    	try {
            return response.getOutputStream();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to get response stream for Evaluation Results Export", ioe);
        }
    }

}
