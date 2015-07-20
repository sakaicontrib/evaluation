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
package org.sakaiproject.evaluation.tool.viewparams;

/**
 * Pass the chosen groups to the view reports page when coming via bread crumbs from essay responses page. 
 * For going staightforward from choose report groups to view reports page, the report bean takes care of it. 
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steven Githens
 */
public class ReportParameters extends BaseViewParameters {

   public Long evaluationId;
   public String[] groupIds;
   public String viewmode;
   public Long[] items;

   public ReportParameters() {}

   public ReportParameters(String viewID, Long evaluationId){
      this.viewID = viewID;
      this.evaluationId = evaluationId;
   }

   public ReportParameters(String viewID, Long evaluationId, String[] groupIds) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.groupIds = groupIds;
   }

}
