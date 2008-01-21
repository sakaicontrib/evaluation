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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
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
 * @author:Will Humphries (whumphri@vt.edu)
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

    private EvalEvaluationsLogic evalsLogic;
    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
    }

    private EvalResponsesLogic responsesLogic;
    public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
        this.responsesLogic = responsesLogic;
    }

    private EvalItemsLogic itemsLogic;
    public void setItemsLogic(EvalItemsLogic itemsLogic) {
        this.itemsLogic = itemsLogic;
    }

    public ReportsBean reportsBean;
    public void setReportsBean(ReportsBean reportsBean) {
        this.reportsBean = reportsBean;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    int displayNumber = 1;

    String[] groupIds;

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        String currentUserId = externalLogic.getCurrentUserId();

        UIMessage.make(tofill, "view-report-title", "viewreport.page.title");

        ReportParameters reportViewParams = (ReportParameters) viewparams;
        Long evaluationId = reportViewParams.evaluationId;
        if (evaluationId != null) {

            // bread crumbs
            UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));
            UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 
                    new TemplateViewParameters(ReportChooseGroupsProducer.VIEW_ID, reportViewParams.evaluationId));

            EvalEvaluation evaluation = evalsLogic.getEvaluationById(reportViewParams.evaluationId);

            // do a permission check
            if (!currentUserId.equals(evaluation.getOwner()) && 
                    !externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
                throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
            }

            // get template from DAO 
            EvalTemplate template = evaluation.getTemplate();

            // TODO - this should respect the user
            //List allTemplateItems = itemsLogic.getTemplateItemsForEvaluation(evaluationId, null, null);
            List allTemplateItems = new ArrayList(template.getTemplateItems());
            if (!allTemplateItems.isEmpty()) {
                if (reportViewParams.groupIds == null || reportViewParams.groupIds.length == 0) {
                    // TODO - this is a security hole -AZ
                    // no passed in groups so just list all of them
                    Map evalGroups = evalsLogic.getEvaluationGroups(new Long[] { evaluationId }, false);
                    List groups = (List) evalGroups.get(evaluationId);
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
                List answerableItemsList = TemplateItemUtils.getAnswerableTemplateItems(allTemplateItems);

                UIBranchContainer courseSection = null;
                UIBranchContainer instructorSection = null;

                // handle showing all course type items
                if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, answerableItemsList)) {
                    courseSection = UIBranchContainer.make(tofill, "courseSection:");
                    UIMessage.make(courseSection, "report-course-questions", "viewreport.itemlist.coursequestions");
                    for (int i = 0; i < answerableItemsList.size(); i++) {
                        EvalTemplateItem templateItem = (EvalTemplateItem) answerableItemsList.get(i);

                        if (EvalConstants.ITEM_CATEGORY_COURSE.equals(templateItem.getItemCategory())) {
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
                        EvalTemplateItem templateItem = (EvalTemplateItem) answerableItemsList.get(i);

                        if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(templateItem.getItemCategory())) {
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
        if (templateItemType.equals(EvalConstants.ITEM_TYPE_SCALED)) {
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

            List itemAnswers = responsesLogic.getEvalAnswers(item.getId(), evalId, groupIds);

            for (int x = 0; x < scaleLabels.length; x++) {
                UIBranchContainer answerbranch = UIBranchContainer.make(scaled, "answers:", x + "");
                UIOutput.make(answerbranch, "responseText", scaleOptions[x]);
                int answers = 0;
                //count the number of answers that match this one
                for (int y = 0; y < itemAnswers.size(); y++) {
                    EvalAnswer curr = (EvalAnswer) itemAnswers.get(y);
                    if (curr.getNumeric().intValue() == x) {
                        answers++;
                    }
                }
                UIOutput.make(answerbranch, "responseTotal", answers + "", x + "");
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
