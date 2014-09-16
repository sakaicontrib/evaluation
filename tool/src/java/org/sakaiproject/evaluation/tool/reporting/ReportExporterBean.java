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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;

import uk.org.ponder.util.UniversalRuntimeException;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportExporterBean {

    private static Log log = LogFactory.getLog(ReportExporterBean.class);

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

    public boolean export(DownloadReportViewParams drvp, HttpServletResponse response) {
        // get evaluation and template from DAO
        EvalEvaluation evaluation = evaluationService.getEvaluationById(drvp.evalId);

        // do a permission check
        if (!reportingPermissions.canViewEvaluationResponses(evaluation, drvp.groupIds)) {
            String currentUserId = commonLogic.getCurrentUserId();
            throw new SecurityException("Invalid user attempting to access report downloads: "
                    + currentUserId);
        }

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
        
        if ("pdfResultsReportIndividual".equals(drvp.viewID)) {
            exporter.buildReport(evaluation, drvp.groupIds, drvp.evaluateeId, resultsOutputStream);
        } else {
            exporter.buildReport(evaluation, drvp.groupIds, resultsOutputStream);
        }

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
