/******************************************************************************
 * ViewReportProducer.java - created by on Oct 05, 2006
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

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.reporting.ReportingPermissions;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * rendering the report results from an evaluation
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */

public class ReportsViewingProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

   private static Log log = LogFactory.getLog(EvaluationBean.class);

   public static final String VIEW_ID = "report_view";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   public ReportsBean reportsBean;
   public void setReportsBean(ReportsBean reportsBean) {
      this.reportsBean = reportsBean;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }
   
   private EvalResponseAggregatorUtil responseAggregator;
   public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
       this.responseAggregator = bean;
   }
   
   private ReportingPermissions reportingPermissions;
   public void setReportingPermissions(ReportingPermissions perms) {
      this.reportingPermissions = perms;
   }

   int displayNumber = 1;

   String[] groupIds;

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
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

      UIMessage.make(tofill, "view-report-title", "viewreport.page.title");

      ReportParameters reportViewParams = (ReportParameters) viewparams;
      Long evaluationId = reportViewParams.evaluationId;
      if (evaluationId != null) {

         // bread crumbs
         UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 
               new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, reportViewParams.evaluationId));

         EvalEvaluation evaluation = evaluationService.getEvaluationById(reportViewParams.evaluationId);

         // do a permission check
         if (!reportingPermissions.canViewEvaluationResponses(evaluation, reportViewParams.groupIds)) {
            throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
         }

         // get template from DAO 
         EvalTemplate template = evaluation.getTemplate();

         // TODO - this should respect the user
         //List allTemplateItems = itemsLogic.getTemplateItemsForEvaluation(evaluationId, null, null);
         List<EvalTemplateItem> allTemplateItems = new ArrayList<EvalTemplateItem>(template.getTemplateItems());
         if (!allTemplateItems.isEmpty()) {
            if (reportViewParams.groupIds == null || reportViewParams.groupIds.length == 0) {
               // TODO - this is a security hole -AZ
               // no passed in groups so just list all of them
               Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvaluationGroups(new Long[] { evaluationId }, false);
               List<EvalGroup> groups = evalGroups.get(evaluationId);
               groupIds = new String[groups.size()];
               for (int i = 0; i < groups.size(); i++) {
                  EvalGroup currGroup = (EvalGroup) groups.get(i);
                  groupIds[i] = currGroup.evalGroupId;
               }
            } else {
               // use passed in group ids
               groupIds = reportViewParams.groupIds;
            }

            UIInternalLink.make(tofill, "fullEssayResponse", UIMessage.make("viewreport.view.essays"), new EssayResponseParams(
                  ReportsViewEssaysProducer.VIEW_ID, reportViewParams.evaluationId, groupIds));

            Boolean allowCSVExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_CSV_REPORT_EXPORT);
            if (allowCSVExport != null && allowCSVExport == true) {
               UIInternalLink.make(tofill, "csvResultsReport", UIMessage.make("viewreport.view.csv"), new CSVReportViewParams(
                     "csvResultsReport", template.getId(), reportViewParams.evaluationId, groupIds));
            }

            Boolean allowXLSExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_XLS_REPORT_EXPORT);
            if (allowXLSExport != null && allowXLSExport == true) {
               UIInternalLink.make(tofill, "xlsResultsReport", UIMessage.make("viewreport.view.xls"), new ExcelReportViewParams(
                     "xlsResultsReport", template.getId(), reportViewParams.evaluationId, groupIds));
            }

            Boolean allowPDFExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT);
            if (allowPDFExport != null && allowPDFExport == true) {
               UIInternalLink.make(tofill, "pdfResultsReport", UIMessage.make("viewreport.view.pdf"), new PDFReportViewParams(
                     "pdfResultsReport", template.getId(), reportViewParams.evaluationId, groupIds));
            }
            // filter out items that cannot be answered (header, etc.)
            //List<EvalTemplateItem> answerableItemsList = TemplateItemUtils.getAnswerableTemplateItems(allTemplateItems);
            List<EvalTemplateItem> answerableItemsList = TemplateItemUtils.orderTemplateItems(allTemplateItems);

            UIBranchContainer courseSection = null;
            UIBranchContainer instructorSection = null;

            // handle showing all course type items
            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, answerableItemsList)) {
               courseSection = UIBranchContainer.make(tofill, "courseSection:");
               UIMessage.make(courseSection, "report-course-questions", "viewreport.itemlist.coursequestions");
               for (int i = 0; i < answerableItemsList.size(); i++) {
                  EvalTemplateItem templateItem = answerableItemsList.get(i);

                  if (EvalConstants.ITEM_CATEGORY_COURSE.equals(templateItem.getCategory())) {
                     UIBranchContainer branch = UIBranchContainer.make(courseSection, "itemrow:first", i + "");
                     if (i % 2 == 1)
                        branch.decorators = new DecoratorList( new UIStyleDecorator("") ); // must match the existing CSS class
                     renderTemplateItemResults(templateItem, evaluation.getId(), displayNumber, branch);
                     displayNumber++;
                  }
               }
            }

            // handle showing all instructor type items
            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, answerableItemsList)) {
               instructorSection = UIBranchContainer.make(tofill, "instructorSection:");
               UIMessage.make(instructorSection, "report-instructor-questions", "viewreport.itemlist.instructorquestions");
               for (int i = 0; i < answerableItemsList.size(); i++) {
                  EvalTemplateItem templateItem = answerableItemsList.get(i);

                  if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(templateItem.getCategory())) {
                     UIBranchContainer branch = UIBranchContainer.make(instructorSection, "itemrow:first", i + "");
                     if (i % 2 == 1)
                        branch.decorators = new DecoratorList( new UIStyleDecorator("") ); // must match the existing CSS class
                     renderTemplateItemResults(templateItem, evaluation.getId(), i, branch);
                     displayNumber++;
                  }
               } // end of for loop				
               //}
            }
         }
      } else {
         // invalid view params
         throw new IllegalArgumentException("Evaluation id is required to view report");
      }

   }

   /**
    * @param templateItem
    * @param evalId
    * @param displayNum
    * @param branch
    */
   private void renderTemplateItemResults(EvalTemplateItem templateItem, Long evalId, int displayNum, UIBranchContainer branch) {

      EvalItem item = templateItem.getItem();

      String templateItemType = item.getClassification();
      if (templateItemType.equals(EvalConstants.ITEM_TYPE_SCALED) || 
          templateItemType.equals(EvalConstants.ITEM_TYPE_MULTIPLEANSWER) ||
          templateItemType.equals(EvalConstants.ITEM_TYPE_MULTIPLECHOICE)) {
         //normal scaled type
         EvalScale scale = item.getScale();
         String[] scaleOptions = scale.getOptions();
         int optionCount = scaleOptions.length;
         String scaleLabels[] = new String[optionCount];

         Boolean useNA = templateItem.getUsesNA();

         UIBranchContainer scaled = UIBranchContainer.make(branch, "scaledSurvey:");

         UIOutput.make(scaled, "itemNum", displayNum+"");
         UIVerbatim.make(scaled, "itemText", item.getItemText());

         if (useNA.booleanValue() == true) {
            UIBranchContainer radiobranch3 = UIBranchContainer.make(scaled, "showNA:");
            UIBoundBoolean.make(radiobranch3, "itemNA", useNA);
         }

         List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(item.getId(), evalId, groupIds);

         int[] responseNumbers = responseAggregator.countResponseChoices(templateItemType, scaleLabels.length, itemAnswers);
         
         for (int x = 0; x < scaleLabels.length; x++) {
            UIBranchContainer answerbranch = UIBranchContainer.make(scaled, "answers:", x + "");
            UIOutput.make(answerbranch, "responseText", scaleOptions[x]);
            UIOutput.make(answerbranch, "responseTotal", responseNumbers[x]+"");
         }

      } else if (templateItemType.equals(EvalConstants.ITEM_TYPE_TEXT)) { //"Short Answer/Essay"
         UIBranchContainer essay = UIBranchContainer.make(branch, "essayType:");
         UIOutput.make(essay, "itemNum", displayNum + "");
         UIVerbatim.make(essay, "itemText", item.getItemText());

         UIInternalLink.make(essay, "essayResponse", 
               new EssayResponseParams(ReportsViewEssaysProducer.VIEW_ID, evalId, templateItem.getId(), groupIds));
      } else {
         log.warn("Skipped invalid item type ("+templateItemType+"): TI: " + templateItem.getId() + ", Item: " + item.getId() );
      }
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new ReportParameters();
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   public List reportNavigationCases() {
      List i = new ArrayList();
      return i;
   }

}
