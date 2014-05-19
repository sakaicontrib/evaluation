/******************************************************************************
 * ItemViewParameters.java - created by aaronz on 21 Mar 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;


/**
 * View params for passing item/templateItem ids to allow removing/previewing of single items,
 * only one of these should be populated, any page that uses this VP should know how to handle
 * both types
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemViewParameters extends TemplateViewParameters {

   public Long itemId;
   public Long templateItemId;
   public String itemClassification;
   public Long groupItemId;

   public ItemViewParameters() { }

   public ItemViewParameters(String viewID, Long itemId, Long templateItemId) {
      this.viewID = viewID;
      this.itemId = itemId;
      this.templateItemId = templateItemId;
   }

   public ItemViewParameters(String viewID, String itemClassification) {
      this.viewID = viewID;
      this.itemClassification = itemClassification;
   }

   public ItemViewParameters(String viewID, String itemClassification, Long templateId) {
      this.viewID = viewID;
      this.itemClassification = itemClassification;
      this.templateId = templateId;
   }

   public ItemViewParameters(String viewID, Long itemId, Long templateItemId, Long templateId) {
      this.viewID = viewID;
      this.itemId = itemId;
      this.templateItemId = templateItemId;
      this.templateId = templateId;
   }

   public ItemViewParameters(String viewID, String itemClassification, Long templateId, Long templateItemId) {
	      this.viewID = viewID;
	      this.itemClassification = itemClassification;
	      this.templateId = templateId;
	      this.templateItemId = templateItemId;
	   }

   public ItemViewParameters(String viewID, String itemClassification, Long templateId, Long templateItemId, Long groupItemId) {
	      this.viewID = viewID;
	      this.itemClassification = itemClassification;
	      this.templateId = templateId;
	      this.templateItemId = templateItemId;
	      this.groupItemId = groupItemId;
	   }

   public ItemViewParameters(String viewID, Long itemId, Long templateItemId, String itemClassification, Long templateId) {
      this.viewID = viewID;
      this.itemId = itemId;
      this.templateItemId = templateItemId;
      this.itemClassification = itemClassification;
      this.templateId = templateId;
   }

}
