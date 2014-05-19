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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;

import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the generation of files for exporting results
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportHandlerHook {

   private static Log log = LogFactory.getLog(ReportHandlerHook.class);

   private ViewParameters viewparams;
   public void setViewparams(ViewParameters viewparams) {
      this.viewparams = viewparams;
   }
   
   private HttpServletResponse response;
   public void setResponse(HttpServletResponse response) {
      this.response = response;
   }
   
   private ReportExporterBean reportExporterBean;
   public void setReportExporterBean(ReportExporterBean reportExporterBean) {
       this.reportExporterBean = reportExporterBean;
   }
   
   
   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.processor.HandlerHook#handle()
    */
   public boolean handle() {
      if (viewparams instanceof DownloadReportViewParams) {
          log.debug("Handing viewparams and response off to the reportExporter");
          return reportExporterBean.export((DownloadReportViewParams) viewparams, response);
      }
      return false;
   }

}
