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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import org.sakaiproject.evaluation.toolaccess.EvaluationAccessAPI;
import org.sakaiproject.evaluation.toolaccess.ToolApi;
import org.springframework.context.MessageSource;

import au.com.bytecode.opencsv.CSVParser;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportExporterBean implements ToolApi {

    private static Log log = LogFactory.getLog(ReportExporterBean.class);

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

    public class MyMessageLocator extends MessageLocator {
      public String getMessage(String[] code, Object[] args) {
        if (code != null) {
          for (String s: code) {
            try {
              return messageSource.getMessage(s, args, Locale.getDefault());
            } catch (Exception e) {
              // message not found, one presumes
            }
          }
          // if none found, just use the code
          return code[0];
        } else
          return "";
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

    //Export report with no evaluateeId (for sinngle export)
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
    private void exportReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, String exportType) {
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
      exporter.setMessageLocator(messageLocator);
      if (EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL.equals(exportType)) {
        exporter.buildReport(evaluation, groupIds, evaluateeId, outputStream);
      } else {
        exporter.buildReport(evaluation, groupIds, outputStream);
      }

    }

    public void init() {
      evaluationAccessAPI.setToolApi(this);
    }

    public boolean export(DownloadReportViewParams drvp, HttpServletResponse response) {
        // get evaluation and template from DAO
        EvalEvaluation evaluation = evaluationService.getEvaluationById(drvp.evalId);

        OutputStream resultsOutputStream = null;
        
        ReportExporter exporter = exportersMap.get(drvp.viewID);
	
        if (exporter == null) {
            throw new IllegalArgumentException("No exporter found for ViewID: " + drvp.viewID);
        }
        if (log.isDebugEnabled()) {
            log.debug("Found exporter: " + exporter.getClass() + " for drvp.viewID " + drvp.viewID);
        }
	        
        resultsOutputStream = getOutputStream(response);
        response.setHeader("Content-disposition", "inline; filename=\"" + drvp.filename+"\"");
        response.setContentType(exporter.getContentType());

        //Support drvp.evaluateeId
        this.exportReport(evaluation,drvp.groupIds,drvp.evaluateeId,resultsOutputStream,drvp.viewID);

        return true;
    }
    
    private OutputStream getOutputStream(HttpServletResponse response){
    	try {
            return response.getOutputStream();
        } catch (IOException ioe) {
            throw UniversalRuntimeException.accumulate(ioe,
                    "Unable to get response stream for Evaluation Results Export");
        }
    }

}
