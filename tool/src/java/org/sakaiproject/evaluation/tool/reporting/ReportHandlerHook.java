/**
 * $Id$
 * $URL$
 * ReportHandlerHook.java - evaluation - 23 Jan 2007 11:35:56 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException;

import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Handles the generation of files for exporting results
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 * @author Steven Githens
 */
public class ReportHandlerHook implements HandlerHook {

   private static Log log = LogFactory.getLog(ReportHandlerHook.class);

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private CSVReportExporter csvReportExporter;
   public void setCsvReportExporter(CSVReportExporter csvReportExporter) {
      this.csvReportExporter = csvReportExporter;
   }

   private XLSReportExporter xlsReportExporter;
   public void setXlsReportExporter(XLSReportExporter xlsReportExporter) {
      this.xlsReportExporter = xlsReportExporter;
   }

   private PDFReportExporter pdfReportExporter;
   public void setPdfReportExporter(PDFReportExporter pdfReportExporter) {
      this.pdfReportExporter = pdfReportExporter;
   }

   private ViewParameters viewparams;
   public void setViewparams(ViewParameters viewparams) {
      this.viewparams = viewparams;
   }
   
   private HttpServletResponse response;
   public void setResponse(HttpServletResponse response) {
      this.response = response;
   }
   
   private EvalResponseAggregatorUtil responseAggregatorUtil; 
   public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
      this.responseAggregatorUtil = bean;
   }
   
   private ReportingPermissions reportingPermissions;
   public void setReportingPermissions(ReportingPermissions perms) {
      this.reportingPermissions = perms;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.processor.HandlerHook#handle()
    */
   public boolean handle() {
      // get viewparams so we know what to generate
      DownloadReportViewParams drvp;
      if (viewparams instanceof DownloadReportViewParams) {
         drvp = (DownloadReportViewParams) viewparams;
      } else {
         return false;
      }
      log.debug("Handling report");

      // get evaluation and template from DAO
      EvalEvaluation evaluation = evaluationService.getEvaluationById(drvp.evalId);
      EvalTemplate template = evaluation.getTemplate();

      String currentUserId = externalLogic.getCurrentUserId();

      // do a permission check
      if (!reportingPermissions.canViewEvaluationResponses(evaluation, drvp.groupIds)) {
         throw new SecurityException("Invalid user attempting to access report downloads: " + currentUserId);
      }

      OutputStream resultsOutputStream = null;
      try {
         resultsOutputStream = response.getOutputStream(); 
      }
      catch (IOException ioe) {
         throw UniversalRuntimeException.accumulate(ioe, "Unable to get response stream for Evaluation Results Export");
      }
      
      // Response Headers that are the same for all Output types
      response.setHeader("Content-disposition", "inline");
      
      EvalAggregatedResponses responses = responseAggregatorUtil.getAggregatedResponses(evaluation, drvp.groupIds);
      
      if (drvp instanceof CSVReportViewParams) {
         response.setContentType("text/x-csv");
         response.setHeader("filename", "report.csv");
         csvReportExporter.formatResponses(responses, resultsOutputStream);
      }
      else if (drvp instanceof ExcelReportViewParams) {
         response.setContentType("application/vnd.ms-excel");
         response.setHeader("filename", "report.xls");
         xlsReportExporter.formatResponses(responses, resultsOutputStream);
      }
      else if (drvp instanceof PDFReportViewParams) {
         response.setContentType("application/pdf");
         response.setHeader("filename", "report.pdf");
         pdfReportExporter.formatResponses(responses, resultsOutputStream);
      }
      return true;
   }

}
