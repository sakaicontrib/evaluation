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

import java.io.OutputStream;

import org.sakaiproject.evaluation.model.EvalEvaluation;

import uk.org.ponder.messageutil.MessageLocator;


/**
 * An interface to implement when exporting reports
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ReportExporter {

   /**
    * Generates the export which will be placed into the OutputStream for sending to the user via an HTTP response
    * 
    * @param evaluation the {@link EvalEvaluation} object to build the report for
    * @param groupIds the set of groups to include results data from
    * @param outputStream the resulting data will be placed into this
    * @param newReportStyle toggle new report style on/off
    */
   public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream, boolean newReportStyle);
   
   /**
    * Generates the export which will be placed into the OutputStream for sending to the user via an HTTP response
    * 
    * @param evaluation the {@link EvalEvaluation} object to build the report for
    * @param groupIds the set of groups to include results data from
	* @param evaluateeId restrict this report to only the results for this instructor
    * @param outputStream the resulting data will be placed into this
    * @param newReportStyle toggle new report style on/off
    */
   public void buildReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, boolean newReportStyle);
   public void setMessageLocator(MessageLocator locator);

   
   public String getContentType();

}
