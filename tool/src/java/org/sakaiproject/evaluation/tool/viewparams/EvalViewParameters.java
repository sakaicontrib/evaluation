/******************************************************************************
 * EvalViewParameters.java - created by aaronz on 31 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *****************************************************************************/

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
   
   public EvalViewParameters() { }

   public EvalViewParameters(String viewID, Long evaluationId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
   }
   
   /**
    * Special constructor used for making the VP when generating a URL for reopening evals
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
