/******************************************************************************
 * ReportsViewEssaysProducer.java - created on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.reporting.ReportingPermissions;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * rendering the Short Answer/Essay part report of an evaluation
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 */

public class ReportsViewEssaysProducer implements ViewComponentProducer, NavigationCaseReporter,
      ViewParamsReporter {

   public static final String VIEW_ID = "view_essay_response";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   private ReportingPermissions reportingPermissions;
   public void setReportingPermissions(ReportingPermissions perms) {
      this.reportingPermissions = perms;
   }

   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
      boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
      boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      if (userAdmin) {
         UIInternalLink.make(tofill, "administrate-link", 
               UIMessage.make("administrate.page.title"),
               new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-scales-link",
               UIMessage.make("controlscales.page.title"),
               new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
      }

      if (createTemplate) {
         UIInternalLink.make(tofill, "control-templates-link",
               UIMessage.make("controltemplates.page.title"), 
               new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controlitems.page.title"), 
               new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
      } else {
         throw new SecurityException("User attempted to access " + 
               VIEW_ID + " when they are not allowed");
      }

      if (beginEvaluation) {
         UIInternalLink.make(tofill, "control-evaluations-link",
               UIMessage.make("controlevaluations.page.title"),
            new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
      }

      UIMessage.make(tofill, "view-essay-title", "viewessay.page.title");

      EssayResponseParams ervps = (EssayResponseParams) viewparams;
      UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"),
            new TemplateViewParameters(ReportChooseGroupsProducer.VIEW_ID, ervps.evalId));

      UIInternalLink.make(tofill, "viewReportLink", UIMessage.make("viewreport.page.title"),
            new ReportParameters(ReportsViewingProducer.VIEW_ID, ervps.evalId, ervps.groupIds));

      // Note: The groups id's should always be passed whether it is for single item or all items

      if (ervps.evalId != null) {
         EvalEvaluation evaluation = evaluationService.getEvaluationById(ervps.evalId);

         // do a permission check
         if (!reportingPermissions.canViewEvaluationResponses(evaluation, ervps.groupIds)) { 
            throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
         }

         // get template from DAO
         EvalTemplate template = evaluation.getTemplate();

         // output single set of essay responses
         if (ervps.itemId != null) {
            // we are actually passing EvalTemplateItem ID
            EvalTemplateItem myTempItem = authoringService.getTemplateItemById(ervps.itemId);
            EvalItem myItem = myTempItem.getItem();

            String cat = myTempItem.getCategory();

            UIBranchContainer radiobranch = null;
            UIBranchContainer courseSection = null;
            UIBranchContainer instructorSection = null;
            if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {// "Course"
               courseSection = UIBranchContainer.make(tofill, "courseSection:");
               UIMessage.make(courseSection, "course-questions-header",
                     "takeeval.group.questions.header");
               radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", "0");
               this.doFillComponent(myItem, ervps.evalId, 0, ervps.groupIds, radiobranch,
                     courseSection);
            } else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) {// "Instructor"
               instructorSection = UIBranchContainer.make(tofill, "instructorSection:");
               UIMessage.make(instructorSection, "instructor-questions-header",
                     "takeeval.instructor.questions.header");
               radiobranch = UIBranchContainer.make(instructorSection, "itemrow:first", "0");
               this.doFillComponent(myItem, ervps.evalId, 0, ervps.groupIds, radiobranch,
                     instructorSection);
            }
         } else {
            // get all items since one is not specified
            List<EvalTemplateItem> allItems = 
               new ArrayList<EvalTemplateItem>(template.getTemplateItems()); // LAZY LOAD

            if (!allItems.isEmpty()) {
               // non-child items are sorted by display order
               List<EvalTemplateItem> ncItemsList = TemplateItemUtils.getNonChildItems(allItems);

               // check if there are any "Course" items or "Instructor" items;
               UIBranchContainer courseSection = null;
               UIBranchContainer instructorSection = null;

               if (TemplateItemUtils.checkTemplateItemsCategoryExists(
                     EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList)) {
                  courseSection = UIBranchContainer.make(tofill, "courseSection:"); //$NON-NLS-1$
               }

               if (TemplateItemUtils.checkTemplateItemsCategoryExists(
                     EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList)) {
                  instructorSection = UIBranchContainer.make(tofill, "instructorSection:"); //$NON-NLS-1$
               }

               for (int i = 0; i < ncItemsList.size(); i++) {
                  EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
                  EvalItem item1 = tempItem1.getItem();
                  String cat = tempItem1.getCategory();

                  UIBranchContainer radiobranch = null;
                  if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)
                        && item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
                     // "Course","Short Answer/Essay"
                     radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", i + "");
                     // need the alt row highlights between essays, not groups of essays
                     // if (i % 2 == 1)
                     // radiobranch.decorators = new DecoratorList(
                     // new UIColourDecorator(null,
                     // Color.decode(EvalToolConstants.LIGHT_GRAY_COLOR)));

                     this.doFillComponent(item1, evaluation.getId(), i, ervps.groupIds,
                           radiobranch, courseSection);
                  } else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)
                        && item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
                     // "Instructor","Short Answer/Essay"
                     radiobranch = UIBranchContainer.make(instructorSection, "itemrow:first", i
                           + "");
                     // need the alt row highlights between essays, not groups of essays
                     // if (i % 2 == 1)
                     // radiobranch.decorators = new DecoratorList(
                     // new UIColourDecorator(null,
                     // Color.decode(EvalToolConstants.LIGHT_GRAY_COLOR)));
                     this.doFillComponent(item1, evaluation.getId(), i, ervps.groupIds,
                           radiobranch, instructorSection);
                  }
               } // end of for loop
            }
         }
      }
   }

   /**
    * @param myItem
    * @param evalId
    * @param i
    * @param groupIds
    * @param radiobranch
    * @param tofill
    */
   private void doFillComponent(EvalItem myItem, Long evalId, int i, String[] groupIds,
         UIBranchContainer radiobranch, UIContainer tofill) {

      if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
         // "Short Answer/Essay"
         UIBranchContainer essay = UIBranchContainer.make(radiobranch, "essayType:");
         UIOutput.make(essay, "itemNum", (i + 1) + "");
         UIOutput.make(essay, "itemText", FormattedText.convertFormattedTextToPlaintext(myItem.getItemText()));

         List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(myItem.getId(), evalId, groupIds);

         //count the number of answers that match this one
         for (int y = 0; y < itemAnswers.size(); y++) {
            UIBranchContainer answerbranch = UIBranchContainer.make(essay, "answers:", y + "");
            if (y % 2 == 1) {
               answerbranch.decorators = new DecoratorList(new UIStyleDecorator("itemsListOddLine")); // must match the existing CSS class
            }
            EvalAnswer curr = itemAnswers.get(y);
            UIOutput.make(answerbranch, "answerNum", new Integer(y + 1).toString());
            UIOutput.make(answerbranch, "itemAnswer", curr.getText());
         }
      }
   }

   public ViewParameters getViewParameters() {
      return new EssayResponseParams(VIEW_ID, null, null, null);
   }

   public List reportNavigationCases() {
      List i = new ArrayList();
      return i;
   }

}
