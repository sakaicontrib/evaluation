
package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils.AnswersMean;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PDFReportExporter implements ReportExporter {

    private static Log log = LogFactory.getLog(PDFReportExporter.class);

    int displayNumber;

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
        this.responseAggregator = bean;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation
     * .model.EvalEvaluation, java.lang.String[], java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {

        EvalPDFReportBuilder evalPDFReportBuilder = new EvalPDFReportBuilder(outputStream);

        Boolean useBannerImage = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER);
        byte[] bannerImageBytes = null;
        if (useBannerImage != null && useBannerImage == true) {
            String bannerImageLocation = (String) evalSettings
                    .get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
            if (bannerImageLocation != null) {
                bannerImageBytes = commonLogic.getFileContent(bannerImageLocation);
            }
        }

//        EvalUser user = commonLogic.getEvalUserById(commonLogic.getCurrentUserId());

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

        // calculate the response rate
        int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
        int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);

        String groupNames = responseAggregator.getCommaSeparatedGroupNames(groupIds);

        // TODO this is so hard to read it makes me cry, it should not be written as a giant single line like this -AZ
        evalPDFReportBuilder.addTitlePage(evaluation.getTitle(), groupNames, messageLocator
                .getMessage("reporting.pdf.startdatetime", df.format(evaluation.getStartDate())),
                messageLocator.getMessage("reporting.pdf.enddatetime", df.format(evaluation
                        .getDueDate())), messageLocator.getMessage("reporting.pdf.replyrate",
                                new String[] { EvalUtils.makeResponseRateStringFromCounts(responsesCount,
                                        enrollmentsCount) }), bannerImageBytes, messageLocator
                                        .getMessage("reporting.pdf.defaultsystemname"));

        // set title and instructions
        evalPDFReportBuilder.addIntroduction(evaluation.getTitle(), commonLogic
                .makePlainTextFromHTML(evaluation.getInstructions()));

        // Reset question numbering
        displayNumber = 0;

        // 1 Make TIDL
        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation.getId(), groupIds);

        // Loop through the major group types: Course Questions, Instructor Questions, etc.
        int renderedItemCount = 0;
        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            // Print the type of the next group we're doing
            if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
                evalPDFReportBuilder.addSectionHeader(messageLocator
                        .getMessage("viewreport.itemlist.course"));
            } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                String instructorMsg = messageLocator.getMessage("reporting.spreadsheet.instructor", 
                        new Object[] {user.displayName});
                evalPDFReportBuilder.addSectionHeader( instructorMsg );
            } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                String assistantMsg = messageLocator.getMessage("reporting.spreadsheet.ta", 
                        new Object[] {user.displayName});
                evalPDFReportBuilder.addSectionHeader( assistantMsg );
            } else {
                evalPDFReportBuilder.addSectionHeader(messageLocator.getMessage("unknown.caps"));
            }

            for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                // Render the Node title if it's enabled in the admin settings.
                if (hng.node != null) {
                    // Showing the section title is system configurable via the administrate view
                    Boolean showHierSectionTitle = (Boolean) evalSettings
                            .get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
                    if (showHierSectionTitle) {
                        evalPDFReportBuilder.addSectionHeader(hng.node.title);
                    }
                }

                List<DataTemplateItem> dtis = hng.getDataTemplateItems(true); // include block
                                                                              // children
                for (int i = 0; i < dtis.size(); i++) {
                    DataTemplateItem dti = dtis.get(i);
                    renderDataTemplateItem(evalPDFReportBuilder, dti);
                    renderedItemCount++;
                }
            }
        }

        evalPDFReportBuilder.close();
    }

    /**
     * Renders a single question given the DataTemplateItem.
     * 
     * @param evalPDFReportBuilder
     * @param dti
     *            the data template item
     */
    private void renderDataTemplateItem(EvalPDFReportBuilder evalPDFReportBuilder,
            DataTemplateItem dti) {
        EvalTemplateItem templateItem = dti.templateItem;
        EvalItem item = templateItem.getItem();
        String questionText = commonLogic.makePlainTextFromHTML(item.getItemText());

        List<EvalAnswer> itemAnswers = dti.getAnswers();

        String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);

        if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType)) {
            evalPDFReportBuilder.addSectionHeader(questionText);
        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItemType)) {
            evalPDFReportBuilder.addSectionHeader(questionText);
        } else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType)) {
            displayNumber++;
            List<String> essays = new ArrayList<String>();
            for (EvalAnswer answer : itemAnswers) {
                essays.add(answer.getText());
            }
            evalPDFReportBuilder.addTextItemsList(displayNumber + ". " + questionText, essays);
        } else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType)) {
            // always showing percentages for now
            boolean showPercentages = true;
            // boolean showPercentages = false;
            // if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
            // showPercentages = true;
            // }

            displayNumber++;
            String[] itemScaleOptions = item.getScale().getOptions();
            int[] responseArray = TemplateItemDataList.getAnswerChoicesCounts(templateItemType,
                    itemScaleOptions.length, itemAnswers);

            String[] optionLabels;
            if (templateItem.getUsesNA()) {
                optionLabels = new String[itemScaleOptions.length + 1];
                for (int m = 0; m < itemScaleOptions.length; m++) {
                    optionLabels[m] = itemScaleOptions[m];
                }
                optionLabels[optionLabels.length - 1] = messageLocator
                        .getMessage("reporting.notapplicable.longlabel");
            } else {
                optionLabels = itemScaleOptions;
            }

            // http://www.caret.cam.ac.uk/jira/browse/CTL-1504
            AnswersMean answersMean = RenderingUtils.calculateMean(responseArray);
            Object[] params = new Object[] { answersMean.getAnswersCount() + "",
                    answersMean.getMeanText() };
            String answersAndMean = messageLocator.getMessage("viewreport.answers.mean", params);

            evalPDFReportBuilder.addLikertResponse(displayNumber + ". " + questionText,
                    optionLabels, responseArray, showPercentages, answersAndMean);

            // handle comments
            if (dti.usesComments()) {
                List<String> comments = dti.getComments();
                evalPDFReportBuilder.addCommentList(messageLocator
                        .getMessage("viewreport.comments.header"), comments, messageLocator
                        .getMessage("viewreport.no.comments"));
            }

        } else {
            log.warn("Trying to add unknown type to PDF: " + templateItemType);
        }
    }

    public String getContentType() {
        return "application/pdf";
    }
}
