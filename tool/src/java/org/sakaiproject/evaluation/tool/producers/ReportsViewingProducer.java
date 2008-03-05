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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.reporting.ReportingPermissions;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
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
 * @author Steven Githens
 */

public class ReportsViewingProducer implements ViewComponentProducer, ViewParamsReporter {
   private static Log log = LogFactory.getLog(ReportsViewingProducer.class);
   
   private static final String VIEWMODE_REGULAR = "viewmode_regular";
   private static final String VIEWMODE_ALLESSAYS = "viewmode_allessays";
   private static final String VIEWMODE_SELECTITEMS = "viewmode_selectitems";

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
   String currentViewMode = VIEWMODE_REGULAR;
   boolean collapseEssays = true;
   Long[] itemsToView;

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
      ReportParameters reportViewParams = (ReportParameters) viewparams;
      String currentUserId = externalLogic.getCurrentUserId();
      
      if (VIEWMODE_ALLESSAYS.equals(reportViewParams.viewmode)) {
         currentViewMode = VIEWMODE_ALLESSAYS;
         collapseEssays = false;
      }
      else if (VIEWMODE_SELECTITEMS.equals(reportViewParams.viewmode)) {
         currentViewMode = VIEWMODE_SELECTITEMS;
         collapseEssays = false;
         if (reportViewParams.items == null) {
            itemsToView = new Long[] {};
         }
         else {
            itemsToView = reportViewParams.items;
         }
      }
      else {
         currentViewMode = VIEWMODE_REGULAR;
      }
      
      renderTopLinks(tofill);

      UIMessage.make(tofill, "view-report-title", "viewreport.page.title");

      Long evaluationId = reportViewParams.evaluationId;
      if (evaluationId != null) {

         /*
          * We only need to show the choose groups breadcrumb if it's actually 
          * possible for us to view more than one group.
          */
         String[] viewableGroups = reportingPermissions.chooseGroupsPartialCheck(evaluationId);
         if (viewableGroups.length > 1) {
            UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 
               new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, reportViewParams.evaluationId));
         }

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
               groupIds = reportViewParams.groupIds;
              
               if (VIEWMODE_ALLESSAYS.equals(reportViewParams.viewmode)) {
                  currentViewMode = VIEWMODE_ALLESSAYS;
               }
            //}
            String groupsDebug = "";
            for (String debug: groupIds) {
               groupsDebug += ", " + debug;
            }
            
            // Render Reporting links such as xls, pdf output, and options for viewing essays and stuff
            renderReportingOptionsTopLinks(tofill, template, reportViewParams);
            
            // Evaluation Info
            UIOutput.make(tofill, "evaluationTitle", evaluation.getTitle());
            String groupsString = "";
            String[] groupIds = reportViewParams.groupIds == null ? new String[] {} : reportViewParams.groupIds;
            for (int groupCounter = 0; groupCounter < reportViewParams.groupIds.length; groupCounter++) {
               groupsString +=  externalLogic.getDisplayTitle(groupIds[groupCounter]);
               if (groupCounter+1 < groupIds.length) {
                  groupsString += ", ";
               }
            }
            UIMessage.make(tofill, "selectedGroups", "viewreport.viewinggroups", new String[] {groupsString});

            List<EvalTemplateItem> answerableItemsList = TemplateItemUtils.orderTemplateItems(allTemplateItems);

            UIBranchContainer courseSection = null;
            UIBranchContainer instructorSection = null;

            // handle showing all course type items
            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, answerableItemsList)) {
               courseSection = UIBranchContainer.make(tofill, "courseSection:");
               UIMessage.make(courseSection, "report-course-questions", "viewreport.itemlist.coursequestions");
               for (int i = 0; i < answerableItemsList.size(); i++) {
                  EvalTemplateItem templateItem = answerableItemsList.get(i);

                  if (EvalConstants.ITEM_CATEGORY_COURSE.equals(templateItem.getCategory())
                        && renderBasedOffOptions(templateItem)) {
                     UIBranchContainer branch = UIBranchContainer.make(courseSection, "itemrow:first", i + "");
                     if (i % 2 == 1)
                        branch.decorators = new DecoratorList( new UIStyleDecorator("") ); // must match the existing CSS class
                     renderTemplateItemResults(templateItem, evaluation.getId(), displayNumber, branch);  
                  }
                  displayNumber++;
               }
            }

            // handle showing all instructor type items
            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, answerableItemsList)) {
               instructorSection = UIBranchContainer.make(tofill, "instructorSection:");
               UIMessage.make(instructorSection, "report-instructor-questions", "viewreport.itemlist.instructorquestions");
               for (int i = 0; i < answerableItemsList.size(); i++) {
                  EvalTemplateItem templateItem = answerableItemsList.get(i);

                  if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(templateItem.getCategory())
                        && renderBasedOffOptions(templateItem)) {
                     UIBranchContainer branch = UIBranchContainer.make(instructorSection, "itemrow:first", i + "");
                     if (i % 2 == 1)
                        branch.decorators = new DecoratorList( new UIStyleDecorator("") ); // must match the existing CSS class
                     renderTemplateItemResults(templateItem, evaluation.getId(), i, branch);
                  }
                  displayNumber++;
               } 				
            }
         }
      } else {
         // invalid view params
         throw new IllegalArgumentException("Evaluation id is required to view report");
      }

   }
   
   /**
    * Should we render this item based off the passed in paramters?  (ie. Should
    * we only render certain template items)
    */
   private boolean renderBasedOffOptions(EvalTemplateItem templateItem) {
      EvalItem item = templateItem.getItem();

      String templateItemType = item.getClassification();
      
      boolean togo = false;
      if (VIEWMODE_SELECTITEMS.equals(currentViewMode)) {
         // If this item isn't in the list of items to view, don't render it.
         boolean contains = false;
         for (int i = 0; i < itemsToView.length; i++) {
            if (templateItem.getId().equals(new Long(itemsToView[i]))) {
               contains = true;
            }
         }
         togo = contains;
      }
      else if (VIEWMODE_ALLESSAYS.equals(currentViewMode)) {
         if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType))
         togo = true;
      }
      else if (VIEWMODE_REGULAR.equals(currentViewMode)) {
         togo = true;
      }
      else {
         togo = false;
      }
      return togo;
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
      if ((templateItemType.equals(EvalConstants.ITEM_TYPE_SCALED) || 
          templateItemType.equals(EvalConstants.ITEM_TYPE_MULTIPLEANSWER) ||
          templateItemType.equals(EvalConstants.ITEM_TYPE_MULTIPLECHOICE))) {
         //normal scaled type
         EvalScale scale = item.getScale();
         String[] scaleOptions = scale.getOptions();
         int optionCount = scaleOptions.length;
         String scaleLabels[] = new String[optionCount];

         UIBranchContainer scaled = UIBranchContainer.make(branch, "scaledSurvey:");

         UIOutput.make(scaled, "itemNum", displayNum+"");
         UIVerbatim.make(scaled, "itemText", item.getItemText());

         // SWG FIXME TODO We need to handle having zero groups for anonymous surveys,
         // but we can't just pass in an empty groupID array because that will give
         // us *all* the groups.  Huge Security hole.
         List<EvalAnswer> itemAnswers = new ArrayList<EvalAnswer>();
         if (groupIds != null && groupIds.length != 0) {
            itemAnswers = deliveryService.getEvalAnswers(item.getId(), evalId, groupIds);
         }

         int[] responseNumbers = responseAggregator.countResponseChoices(templateItemType, scaleLabels.length, itemAnswers);
         
         for (int x = 0; x < scaleLabels.length; x++) {
            UIBranchContainer answerbranch = UIBranchContainer.make(scaled, "answers:", x + "");
            UIOutput.make(answerbranch, "responseText", scaleOptions[x]);
            UIOutput.make(answerbranch, "responseTotal", responseNumbers[x]+"");
         }
         
         if (templateItem.getUsesNA()) {
            UIBranchContainer answerbranch = UIBranchContainer.make(scaled, "answers:");
            UIMessage.make(answerbranch, "responseText", "reporting.notapplicable.longlabel");
            UIOutput.make(answerbranch, "responseTotal", responseNumbers[responseNumbers.length-1]+"");
         }

      } 
      else if (templateItemType.equals(EvalConstants.ITEM_TYPE_TEXT)) { //"Short Answer/Essay"
         if (collapseEssays) {
            UIBranchContainer essay = UIBranchContainer.make(branch, "essayType:");
            UIOutput.make(essay, "itemNum", displayNum + "");
            UIVerbatim.make(essay, "itemText", item.getItemText());

            //UIInternalLink.make(essay, "essayResponse", 
            //      new EssayResponseParams(ReportsViewEssaysProducer.VIEW_ID, evalId, templateItem.getId(), groupIds));
            ReportParameters params = new ReportParameters(VIEW_ID, evalId);
            params.viewmode = VIEWMODE_SELECTITEMS;
            params.items = new Long[] {templateItem.getId()};
            params.groupIds = groupIds;
            UIInternalLink.make(essay, "essayResponse", UIMessage.make("viewreport.view.viewresponses"),
                  params);
         }
         else {
            UIBranchContainer essay = UIBranchContainer.make(branch, "expandedEssayType:");
            UIOutput.make(essay, "itemNum", displayNum + "");
            UIOutput.make(essay, "itemText", FormattedText.convertFormattedTextToPlaintext(item.getItemText()));

            List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(item.getId(), evalId, groupIds);

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
      } else {
         log.warn("Skipped invalid item type ("+templateItemType+"): TI: " + templateItem.getId() + ", Item: " + item.getId() );
      }
   }

   public ViewParameters getViewParameters() {
      return new ReportParameters();
   }
   
   /**
    * Moved this top link rendering out of the main producer code. 
    * TODO FIXME: Is there not some standard Evalution Util class to render 
    * all these top links?  Find out. sgithens 2008-03-01 7:13PM Central Time
    *
    * @param tofill The parent UIContainer.
    */
   private void renderTopLinks(UIContainer tofill) {
      
      
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
   }
   
   /**
    * Renders the reporting specific links like XLS and PDF download, etc.
    * 
    * @param tofill
    * @param template
    * @param reportViewParams
    */
   private void renderReportingOptionsTopLinks(UIContainer tofill, EvalTemplate template, ReportParameters reportViewParams) {
      // Render the link to show "All Essays" or "All Questions"
         ReportParameters viewEssaysParams = (ReportParameters) reportViewParams.copyBase();
         if (VIEWMODE_ALLESSAYS.equals(currentViewMode)) {
            viewEssaysParams.viewmode = VIEWMODE_REGULAR; 
            UIInternalLink.make(tofill, "fullEssayResponse", UIMessage.make("viewreport.view.allquestions"), 
                 viewEssaysParams);
         }
         else {
            viewEssaysParams.viewmode = VIEWMODE_ALLESSAYS;
            UIInternalLink.make(tofill, "fullEssayResponse",  UIMessage.make("viewreport.view.essays"), 
                 viewEssaysParams);
         }
         
         /*
          * sgithens March 4, 2008 8:49 PM Central Time
          * 
          * Fixed the Viewparameters to have real file names in EVALSYS-452
          * Working on removing these hardcoded filenames and giving them survey
          * specific names in EVALSYS-445
          */
         Boolean allowCSVExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_CSV_REPORT_EXPORT);
         if (allowCSVExport != null && allowCSVExport == true) {
            UIInternalLink.make(tofill, "csvResultsReport", UIMessage.make("viewreport.view.csv"), new CSVReportViewParams(
                  "csvResultsReport", template.getId(), reportViewParams.evaluationId, groupIds, "SurveyResults.csv"));
         }

         Boolean allowXLSExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_XLS_REPORT_EXPORT);
         if (allowXLSExport != null && allowXLSExport == true) {
            UIInternalLink.make(tofill, "xlsResultsReport", UIMessage.make("viewreport.view.xls"), new ExcelReportViewParams(
                  "xlsResultsReport", template.getId(), reportViewParams.evaluationId, groupIds, "SurveyResults.xls"));
         }

         Boolean allowPDFExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT);
         if (allowPDFExport != null && allowPDFExport == true) {
            UIInternalLink.make(tofill, "pdfResultsReport", UIMessage.make("viewreport.view.pdf"), new PDFReportViewParams(
                  "pdfResultsReport", template.getId(), reportViewParams.evaluationId, groupIds, "SurveyResults.pdf"));
         }
      }
}
