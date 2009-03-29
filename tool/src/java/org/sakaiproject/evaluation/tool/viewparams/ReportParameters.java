/******************************************************************************
 * ReportParameters.java - created by whumphri on 20 Mar 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu) - primary
 * 
 *****************************************************************************/

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
