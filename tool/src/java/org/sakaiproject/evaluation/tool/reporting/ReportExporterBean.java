
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

        ReportExporter exporter = exportersMap.get(drvp.viewID);

        if (exporter == null) {
            throw new IllegalArgumentException("No exporter found for ViewID: " + drvp.viewID);
        }
        if (log.isDebugEnabled()) {
            log.debug("Found exporter: " + exporter.getClass() + " for drvp.viewID " + drvp.viewID);
        }

        OutputStream resultsOutputStream = null;
        try {
            resultsOutputStream = response.getOutputStream();
        } catch (IOException ioe) {
            throw UniversalRuntimeException.accumulate(ioe,
                    "Unable to get response stream for Evaluation Results Export");
        }

        // Response Headers that are the same for all Output types
        response.setHeader("Content-disposition", "inline");
        response.setHeader("filename", drvp.filename);
        response.setContentType(exporter.getContentType());
        exporter.buildReport(evaluation, drvp.groupIds, resultsOutputStream);

        return true;
    }

}
