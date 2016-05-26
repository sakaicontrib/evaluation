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

   private static final Log LOG = LogFactory.getLog(ReportHandlerHook.class);

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
          LOG.debug("Handing viewparams and response off to the reportExporter");
          return reportExporterBean.export((DownloadReportViewParams) viewparams, response);
      }
      return false;
   }

}
