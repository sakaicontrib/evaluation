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
package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
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
public class PDFReportExporterIndividual implements ReportExporter {

    private static final Log LOG = LogFactory.getLog(PDFReportExporterIndividual.class);

    int displayNumber;

    ArrayList<Double> weightedMeansBlocks;
    int blockNumber=0;

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
	public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream, boolean useNewReportStyle) {
		buildReport(evaluation, groupIds, null, outputStream, useNewReportStyle);
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation
     * .model.EvalEvaluation, java.lang.String[], java.lang.String, java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, boolean useNewReportStyle) {
		
    	//Make sure responseAggregator is using this messageLocator
        responseAggregator.setMessageLocator(messageLocator);
		EvalPDFReportBuilder evalPDFReportBuilder = new EvalPDFReportBuilder(outputStream);
        Boolean instructorViewAllResults = (boolean) evaluation.getInstructorViewAllResults();
        String currentUserId = commonLogic.getCurrentUserId();
        String evalOwner = evaluation.getOwner();

        Boolean useBannerImage = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER);
        byte[] bannerImageBytes = null;
        if (useBannerImage != null && useBannerImage == true) {
            String bannerImageLocation = (String) evalSettings
                    .get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
            if (bannerImageLocation != null) {
                bannerImageBytes = commonLogic.getFileContent(bannerImageLocation);
            }
        }

        //EvalUser user = commonLogic.getEvalUserById(commonLogic.getCurrentUserId());

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

        // calculate the response rate
        // int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
        int responsesCount = evaluationService.countResponses(null, new Long[] {evaluation.getId()}, groupIds, null);
        int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), groupIds);

        String groupNames = responseAggregator.getCommaSeparatedGroupNames(groupIds);

        // TODO this is so hard to read it makes me cry, it should not be written as a giant single line like this -AZ
        evalPDFReportBuilder.addTitlePage(
                evaluation.getTitle(), 
                groupNames, 
                messageLocator.getMessage("reporting.pdf.startdatetime", df.format(evaluation.getStartDate())),
                messageLocator.getMessage("reporting.pdf.enddatetime", df.format(evaluation.getDueDate())), 
                messageLocator.getMessage("reporting.pdf.replyrate", new String[] { 
                        EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount) 
                }), 
                bannerImageBytes, 
                messageLocator.getMessage("reporting.pdf.defaultsystemname"),
                messageLocator.getMessage("reporting.pdf.informationTitle")
                );
        
        /**
         * set title and instructions
         * 
         * Note this doesn't go far enough
         * commonLogic.makePlainTextFromHTML removes html tags
         * but it also leaves the text
         */
        evalPDFReportBuilder.addIntroduction(evaluation.getTitle(), 
                htmlContentParser(
                        commonLogic.makePlainTextFromHTML(
                                evaluation.getInstructions())));

        // Reset question numbering
        displayNumber = 0;

        // 1 Make TIDL
        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation.getId(), groupIds);

        // Loop through the major group types: Course Questions, Instructor Questions, etc.
        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            
            if (!instructorViewAllResults   // If the eval is so configured,
              && !commonLogic.isUserAdmin(currentUserId) // and currentUser is not an admin
              && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
              && !EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) 
              && !currentUserId.equals(commonLogic.getEvalUserById(tig.associateId).userId) ) {
                // skip items that aren't for the current user
                continue;
            }
			
			if (!EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) 
			  && !evaluateeId.equals(commonLogic.getEvalUserById(tig.associateId).userId) ) {
				continue;
			}
            
            // Print the type of the next group we're doing
            /*if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
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
            }*/

            for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                // Render the Node title if it's enabled in the admin settings.
                if (hng.node != null) {
                    // Showing the section title is system configurable via the administrate view
                    Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
                    if (showHierSectionTitle) {
                        evalPDFReportBuilder.addSectionHeader(hng.node.title, true, 0);
                    }
                }

                List<DataTemplateItem> dtis = hng.getDataTemplateItems(true); // include block children

                weightedMeansBlocks = this.getWeightedMeansBlocks(dtis);

                for (int i = 0; i < dtis.size(); i++) {
                    DataTemplateItem dti = dtis.get(i);
                    LOG.debug("Item text: "+dti.templateItem.getItem().getItemText());
                    
                    if (!instructorViewAllResults // If the eval is so configured,
                      && !commonLogic.isUserAdmin(currentUserId)  // and currentUser is not an admin
                      && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
                      && !EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
                      && !currentUserId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
                        //skip instructor items that aren't for the current user
                        continue;
                    }
					
					if (!EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
					   && !evaluateeId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
						continue;
					}
                    
                    renderDataTemplateItem(evalPDFReportBuilder, dti);
                }
                blockNumber=0;
                weightedMeansBlocks.clear();
            }
        }

        evalPDFReportBuilder.close();
    }
    
    /**
     * Remove tags & inclusive content
     * 
     * @param html String to parse
     */
    private String htmlContentParser(String html) {
    	Pattern style = Pattern.compile("<style((.|\n|\r)*)?>((.|\n|\r)*)?</style>");
    	Matcher mstyle = style.matcher(html);
    	while (mstyle.find()) { 
    		html = mstyle.replaceAll("");
    	}
		return html;
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
        //HTML has data like color or size. Size is important because we can replicate it in the report.
        float itemSize = this.calculateFontSize(item.getItemText());
        String questionText = commonLogic.makePlainTextFromHTML(item.getItemText());

        boolean lastElementIsHeader=false; //Two close headers or blocks are too separated.

        List<EvalAnswer> itemAnswers = dti.getAnswers();

        String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);

        if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType))
        {
            evalPDFReportBuilder.addSectionHeader(questionText, lastElementIsHeader, itemSize);
        }
        else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItemType))
        {
            evalPDFReportBuilder.addSectionHeader(questionText, lastElementIsHeader, itemSize);

            if (weightedMeansBlocks.get(blockNumber)!=-1.0)
            {
                evalPDFReportBuilder.addBoldText(messageLocator.getMessage("viewreport.blockWeightedMean")+": "+new DecimalFormat("#.##").format(weightedMeansBlocks.get(blockNumber)));
            }
            blockNumber++;
        }
        else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType))
        {
            displayNumber++;
            List<String> essays = new ArrayList<>();
            for (EvalAnswer answer : itemAnswers) {
                essays.add(answer.getText());
            }
            evalPDFReportBuilder.addTextItemsList(displayNumber + ". " + questionText, essays, false, messageLocator.getMessage("viewreport.numberanswers"));
        }
        else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType))
         {
            // always showing percentages for now
            boolean showPercentages = true;
            // boolean showPercentages = false;
            // if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
            // showPercentages = true;
            // }

            int responseNo = itemAnswers.size();
            displayNumber++;
            String[] itemScaleOptions = item.getScale().getOptions();
            int[] responseArray = TemplateItemDataList.getAnswerChoicesCounts(templateItemType,
                    itemScaleOptions.length, itemAnswers);

            String[] optionLabels = RenderingUtils.makeReportingScaleLabels(templateItem, itemScaleOptions);
            if (templateItem.getUsesNA()) {
                // add in the N/A label to the end
                optionLabels = Arrays.copyOf(optionLabels, optionLabels.length+1);
                optionLabels[optionLabels.length - 1] = messageLocator.getMessage("reporting.notapplicable.longlabel");
            }

            // http://www.caret.cam.ac.uk/jira/browse/CTL-1504
            /*
            AnswersMean answersMean = RenderingUtils.calculateMean(responseArray);
            Object[] params = new Object[] { answersMean.getAnswersCount() + "",
                    answersMean.getMeanText() };
            String answersAndMean = messageLocator.getMessage("viewreport.answers.mean", params);

            evalPDFReportBuilder.addLikertResponse(displayNumber + ". " + questionText,
                    optionLabels, responseArray, responseNo, showPercentages, answersAndMean);
             */

             //20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
             double myWeightedMean = weightedMean(optionLabels, responseArray, templateItem.getUsesNA());
             String answersAndMean = messageLocator.getMessage("viewreport.numberanswers")+": "+this.numberAnswersInQuestion(responseArray, templateItem.getUsesNA());

             if (myWeightedMean!=-1.0)
             {
                 answersAndMean = answersAndMean + System.getProperty("line.separator") + messageLocator.getMessage("viewreport.weightedmean")+": "+new DecimalFormat("#.##").format(myWeightedMean);
             }
             else answersAndMean = answersAndMean + " ";

             evalPDFReportBuilder.addLikertResponse(displayNumber + ". " + questionText,
                     optionLabels, responseArray, responseNo, showPercentages, answersAndMean,lastElementIsHeader);

            // handle comments
            if (dti.usesComments()) {
                List<String> comments = dti.getComments();
                evalPDFReportBuilder.addCommentList(
                        messageLocator.getMessage("viewreport.comments.header"), 
                        comments, 
                        messageLocator.getMessage("viewreport.no.comments"),
                        messageLocator.getMessage("viewreport.numbercomments")
                        );
            }

        } else {
            LOG.warn("Trying to add unknown type to PDF: " + templateItemType);
        }
    }

    private float calculateFontSize(String itemText)
    {
        //20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
        Matcher matcher = Pattern.compile("font-size:\\W*([a-zA-Z-]+)?").matcher(itemText);
        if (matcher.find())
        {
            String itemSize = matcher.group(1);

            //Switch not available for Strings until Java 1.7
            if ("xx-large".equals(itemSize)) {
                return 24.0f;
            }
            else if ("x-large".equals(itemSize)) {
                return 18.0f;
            }
            else if ("large".equals(itemSize)) {
                return 14.0f;
            }
            else if ("medium".equals(itemSize)) {
                return 12.0f;
            }
            else if ("small".equals(itemSize)) {
                return 10.0f;
            }

            // If we have a font size but can't work out what it is attempt to model it on the medium which is
            // slightly bigger than the default font size.
            return 12.0f;
        }

        return 10.0f;
    }

    private int numberAnswersInQuestion(int [] values, boolean usaNA)
    {
        //20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
        int temporal=0;
        int responseCount = (usaNA) ? values.length -1 : values.length; // remove the NA count from the end
        for (int i=0;i<responseCount;i++) temporal=temporal+values[i];
        return temporal;
    }

    private double weightedMean(String [] options, int [] values, boolean usaNA)
    {
    	/*
    	 * 20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	 * Weighted mean for the only numerical choices in the pdf report.
    	 * options: 2,4,6,8,10 || A,B,C,D,E || 1,2,3,4,5,N/A  || A,B,C,D,E,N/A
    	 * values: 3,7,2,9,5,3 (votes of each option)
    	 */
        Integer myNumber;
        int totalValues=0, totalNumbers=0;
        int responseCount = (usaNA) ? options.length -1 : options.length; // remove the NA count from the end
        boolean numerico=true;

        for (int i=0;i<responseCount;i++)
        {
            try
            {
                myNumber=new Integer(options[i]);
                totalNumbers=totalNumbers+(myNumber*values[i]);
                totalValues=totalValues+values[i];
            }
            catch (Exception e)
            {
                numerico=false;
                break;
            }
        }

        if (!numerico)
        {
            totalNumbers=0;
            totalValues=0;
            for (int i=0;i<responseCount;i++)
            {
                totalNumbers=totalNumbers+((i+1)*values[i]);
                totalValues=totalValues+values[i];
            }
        }

        return ((double)totalNumbers / (double)totalValues);
    }

    public double calculateBlockWeightedMean(ArrayList<Integer> collectedValues, String[] answers)
    {
        //20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
        int accumulator=0, numeroValores=0, answer;
        for (int n=0;n<collectedValues.size();n++)
        {
            numeroValores=numeroValores+collectedValues.get(n);
            try
            {
            	answer=new Integer(answers[n]);
            }
            catch (NumberFormatException e)
            {
            	answer=n+1;
            }
            accumulator=accumulator+((answer)*collectedValues.get(n));
        }
        double blockWeightedMean;
        if (numeroValores!=0) blockWeightedMean = (double)accumulator / (double)numeroValores;
        else blockWeightedMean=-1;

        return blockWeightedMean;
    }

    private ArrayList<Double> getWeightedMeansBlocks(List<DataTemplateItem> dtis)
    {
        //20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
        ArrayList<Double> alTemporal = new ArrayList<>();  //List of means of each block.

        //Number of answers for the block we are working in
        //If is not the same for every block element, mean is invalidated.
        ArrayList<Integer> collectedValues = new ArrayList<>();

        DataTemplateItem dti;
        EvalTemplateItem templateItem;
        EvalItem item;

        boolean processingBlock=false;
        int numberOfChildren=0;

        String[] optionLabels = null; //Answers of each item.

        for (int i = 0; i < dtis.size(); i++)
        {
            dti = dtis.get(i);
            templateItem = dti.templateItem;
            item = templateItem.getItem();

            List<EvalAnswer> itemAnswers = dti.getAnswers();
            String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);

            if ((processingBlock) && (numberOfChildren==0))
            {
                //A block is being processed and there are no more children, time to do the block weighted mean.

                double blockWeightedMean= calculateBlockWeightedMean(collectedValues, optionLabels);
                alTemporal.add(blockWeightedMean);

                //Reset.
                processingBlock=false;
                collectedValues = new ArrayList<>();
            }

            if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItemType))
            {
                processingBlock=true;
                numberOfChildren = templateItem.childTemplateItems.size();
            }
            else if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType))
            {}
            else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType))
            {}
            else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType) ||
                    EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType) ||
                    EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType) ||
                    EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType))
            {
                if (processingBlock)
                {
                    if (numberOfChildren>0) numberOfChildren--;

                    int[] responseArray = TemplateItemDataList.getAnswerChoicesCounts(templateItemType, item.getScale().getOptions().length, itemAnswers);
                    int temporal;

                    optionLabels = item.getScale().getOptions();

                    try
                    {
                        for (int n=0;n<optionLabels.length;n++)
                        {
                            if (n>=collectedValues.size())
                            {
                                collectedValues.add(responseArray[n]);
                            }
                            else
                            {
                                temporal=(collectedValues.get(n));
                                collectedValues.set(n,temporal+responseArray[n]);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        //This exception is raised if there is a non numerical answer among the available choices.
                        //So mean is not valid for answers but for answer numbers.
                    }
                }
            }
            else {
                LOG.warn("Trying to add unknown type to PDF: " + templateItemType);
            }
        }

        //Just in case the last element is a block.
        if ((processingBlock) && (numberOfChildren==0))
        {
            double blockWeightedMean= calculateBlockWeightedMean(collectedValues, optionLabels);
            alTemporal.add(blockWeightedMean);
        }

        return alTemporal;
    }


    public String getContentType() {
        return "application/pdf";
    }
}
