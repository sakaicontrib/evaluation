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
package org.sakaiproject.evaluation.tool.utils;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.tool.reporting.ReportHandlerHook;
import org.sakaiproject.evaluation.tool.settings.ExportConfigurationHook;

import uk.org.ponder.rsf.processor.support.RootHandlerBeanBase;

/**
 * This class exists because RootHandlerBeanBase has a bug in that if a request is handled by a DataView or HandlerHook,
 * setupResponseWriter is called and blasts content-type back to text/html. See <a
 * href="http://www.caret.cam.ac.uk/jira/browse/RSF-123">RSF-123</a>
 * 
 * @author andrew
 * @see OverridedServletRootHandlerBean
 */
public class RootHandlerBeanOverride {
  public void handle() throws ParseException {
    String path = request.getRequestURL().toString();
    if (path.contains( ExportConfigurationHook.VIEW_ID )) {
      exportConfigHook.handle();
    } else {
      if (!reportHandlerHook.handle()) {
        rootHandlerBeanBase.handle();
      }
    }
  }

  private ExportConfigurationHook exportConfigHook;
  public void setExportConfigHook(ExportConfigurationHook hook) {
    this.exportConfigHook = hook;
  }

  private HttpServletRequest request;
  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  private ReportHandlerHook reportHandlerHook;
  public void setReportHandlerHook(ReportHandlerHook reportHandlerHook) {
    this.reportHandlerHook = reportHandlerHook;
  }

  private RootHandlerBeanBase rootHandlerBeanBase;
  public void setRootHandlerBeanBase(RootHandlerBeanBase rootHandlerBeanBase) {
    this.rootHandlerBeanBase = rootHandlerBeanBase;
  }
}
