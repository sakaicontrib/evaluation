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

   // special params for live item previews
   /**
    * Live Preview
    * The scale display setting for live previews
    * EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED;
    */
   public String scaleDisplay;
   /**
    * Live Preview
    * Current text for live preview (might need to truncate this)
    */
   public String text;
   /**
    * Live Preview
    * Whether or not to show the N/A
    */
   public Boolean na;
   /**
    * Live Preview
    * whether to show the user comments box
    */
   public Boolean showComment;
   /**
    * Live Preview
    * Is this item required
    */
   public Boolean compulsory;
   /**
    * Live Preview
    * How many lines of text to show for essay
    */
   public Integer textLines;


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
