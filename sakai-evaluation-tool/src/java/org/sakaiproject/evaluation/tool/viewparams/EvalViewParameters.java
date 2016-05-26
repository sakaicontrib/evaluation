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
 * Allows for passing of information needed for previewing templates or evaluations,
 * also for taking evaluations page, also used for controlling evaluations
 * (rewrite of original)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalViewParameters extends BaseViewParameters {

   public Long evaluationId;
   public Long templateId; 
   public String evalGroupId;
   public Long responseId;
   public String evalCategory;

   /**
    * if this is true then all the other values are ignored and this is assigned to the
    * default "no group" assignGroup, if false or null then it gets ignored
    */
   public Boolean noGroups;
   /**
    * Holds the selected group ids to assign to this evaluation
    */
   public String[] selectedGroupIDs = new String[] {};
   
   /**
    * Holds the selected node ids to assign to this evaluation
    */
   public String[] selectedHierarchyNodeIDs = new String[] {};

   /**
    * Set to true if we are reopening this evaluation
    */
   public boolean reOpening = false;
   
   /** 
    * Whether to navigate to administrate_search view after saving settings
    */
   public boolean returnToSearchResults = false;

   /** 
    * The value of searchString needed by AdministrateSearchProducer.fillCommponents() 
    * to restore administrate_search view after saving settings
    */
   public String adminSearchString = null;

   /** 
    * The value of page needed by AdministrateSearchProducer.fillCommponents() 
    * to restore administrate_search view after saving settings
    */ 
   public int adminSearchPage = 0;
   
   public String[] expanded = null;
   public String nodeClicked = null;
   
   public EvalViewParameters() { }

   public EvalViewParameters(String viewID, Long evaluationId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
   }
   
   /**
    * Special constructor used for making the VP when generating a URL for reopening evals
    * @param viewID
    * @param evaluationId
    * @param reOpening
    */
   public EvalViewParameters(String viewID, Long evaluationId, boolean reOpening) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.reOpening = reOpening;
   }

   public EvalViewParameters(String viewID, Long evaluationId, String evalGroupId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long templateId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;	
      this.templateId = templateId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long templateId, String evalGroupId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;	
      this.templateId = templateId;
      this.evalGroupId = evalGroupId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, String evalGroupId, String evalCategory) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
      this.evalCategory = evalCategory;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long responseId, String evalGroupId, String evalCategory) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
      this.responseId = responseId;
      this.evalCategory = evalCategory;
   }

}
