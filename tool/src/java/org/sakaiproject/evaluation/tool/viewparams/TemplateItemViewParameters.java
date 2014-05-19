/******************************************************************************
 * class TemplateItemViewParameters.java - created by whumphri@vt.edu on Oct 23, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.viewparams;

/**
 * This is a view parameters class which defines the variables that are passed
 * from one page to another
 * 
 * @author Sakai App Builder -AZ
 */
public class TemplateItemViewParameters extends TemplateViewParameters {

  public Long templateItemId;

  public TemplateItemViewParameters() {
  }

  public TemplateItemViewParameters(String viewID, Long templateId,
      Long templateItemId) {
    this.viewID = viewID;
    this.templateId = templateId;
    this.templateItemId = templateItemId;
  }

}
