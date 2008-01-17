/******************************************************************************
 * ReportHandlerHook.java - created by aaronz@vt.edu
 *
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 *
 * A copy of the Educational Community License has been included in this
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 *
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Image;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;
import java.net.URL;

import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Handles the generation of a CSV file for exporting results
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steven Githens
 *
 */
public class ReportHandlerHook implements HandlerHook {

    private static Log log = LogFactory.getLog(ReportHandlerHook.class);

    private static final char COMMA = ',';

    private HttpServletResponse response;
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    private ViewParameters viewparams;
    public void setViewparams(ViewParameters viewparams) {
        this.viewparams = viewparams;
    }

    private EvalItemsLogic itemsLogic;
    public void setItemsLogic(EvalItemsLogic itemsLogic) {
        this.itemsLogic = itemsLogic;
    }

    private EvalEvaluationsLogic evalsLogic;
    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
    }
    
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalResponsesLogic responsesLogic;
    public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
        this.responsesLogic = responsesLogic;
    }

    private EvalGroupsProvider evalGroupsProvider;
    public void setEvalGroupsProvider(EvalGroupsProvider logic) {
        this.evalGroupsProvider = logic;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.processor.HandlerHook#handle()
     */
    public boolean handle() {
        // get viewparams so we know what to generate
        DownloadReportViewParams drvp;
        if (viewparams instanceof DownloadReportViewParams) {
            drvp = (DownloadReportViewParams) viewparams;
        } else {
            return false;
        }
        log.debug("Handling report");

        // get evaluation and template from DAO
        EvalEvaluation evaluation = evalsLogic.getEvaluationById(drvp.evalId);
        EvalTemplate template = evaluation.getTemplate();

        List<String> topRow = new ArrayList<String>(); //holds top row (item text)
        List<EvalItem> allEvalItems = new ArrayList<EvalItem>(); //holds all expanded eval items (blocks are expanded here)
        List<EvalTemplateItem> allEvalTemplateItems = new ArrayList<EvalTemplateItem>(); 
        List<List<String>> responseRows = new ArrayList<List<String>>();//holds response rows

        /*
         * Getting list of response ids serves 2 purposes:
         * 
         * a) Main purpose: We need to check in the for loop at line 171
         * that which student (i.e. which response) has not submitted
         * the answer for a particular question. This is so that we can 
         * add empty space instead.
         * 
         * b) Side purpose: countResponses method in EvalResponsesLogic
         * does not take array of groups ids.
         *   
         */
        List responseIds = responsesLogic.getEvalResponseIds(drvp.evalId, drvp.groupIds);
        int numOfResponses = responseIds.size();

        //add a row for each response
        for (int i = 0; i < numOfResponses; i++) {
            List currResponseRow = new ArrayList();
            responseRows.add(currResponseRow);
        }

        //get all items
        List allItems = new ArrayList(template.getTemplateItems());

        if (!allItems.isEmpty()) {
            //filter out the block child items, to get a list non-child items
            List ncItemsList = TemplateItemUtils.getNonChildItems(allItems);
            Collections.sort(ncItemsList, new ComparatorsUtils.TemplateItemComparatorByOrder());
            //for each item
            for (int i = 0; i < ncItemsList.size(); i++) {
                //fetch the item
                EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
                allEvalTemplateItems.add(tempItem1);
                EvalItem item1 = tempItem1.getItem();

                //if the item is normal scaled or text (essay)
                if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_SCALED)
                        || TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_TEXT)) {

                    //add the item description to the top row
                    // TODO: This is now rich text, needs flattening/rendering
                    topRow.add(item1.getItemText());
                    allEvalItems.add(item1);

                    //get all answers to this item within this evaluation
                    List itemAnswers = responsesLogic.getEvalAnswers(item1.getId(), drvp.evalId, drvp.groupIds);
                    updateResponseList(numOfResponses, responseIds, responseRows, itemAnswers, tempItem1, item1);

                }
                // block parent type (block child handled inside this)
                else if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
                    //add the block description to the top row
                    topRow.add(item1.getItemText());
                    allEvalItems.add(item1);
                    for (int j = 0; j < numOfResponses; j++) {
                        List currRow = (List) responseRows.get(j);
                        //add blank response to block parent row
                        currRow.add("");
                    }

                    //get child block items
                    List childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(tempItem1.getId(), false);
                    for (int j = 0; j < childList.size(); j++) {
                        EvalTemplateItem tempItemChild = (EvalTemplateItem) childList.get(j);
                        allEvalTemplateItems.add(tempItemChild);
                        EvalItem child = tempItemChild.getItem();
                        //add child's text to top row
                        topRow.add(child.getItemText());
                        allEvalItems.add(child);
                        //get all answers to the child item within this eval
                        List itemAnswers = responsesLogic.getEvalAnswers(child.getId(), drvp.evalId, drvp.groupIds);
                        updateResponseList(numOfResponses, responseIds, responseRows, itemAnswers, tempItemChild, child);
                    }
                }
                // for block child 
                else if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_BLOCK_CHILD)) {
                    // do nothing as they are already handled inside block parent
                }
                // for header type items
                else if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_HEADER)) {
                    // DO nothing for header type
                } else {
                    // SHOULD NOT GET HERE UNLESS WE HAVE an unhandled type which is an error
                    throw new UniversalRuntimeException("Unknown type, cannot do csv export: " + tempItem1.getItem().getClassification());
                }
            }
        }
        
        if (drvp instanceof CSVReportViewParams) {
            respondWithCSV(topRow, responseRows, numOfResponses);
        }
        else if (drvp instanceof ExcelReportViewParams) {
            respondWithExcel(evaluation, template, allEvalItems, 
                allEvalTemplateItems, topRow, responseRows, numOfResponses, drvp.groupIds);
        }
        else if (drvp instanceof PDFReportViewParams) {
            respondWithPDF(evaluation, template, allEvalItems, 
                    allEvalTemplateItems, topRow, responseRows, numOfResponses, drvp.groupIds);
        }
        return true;
    }
    
    private void respondWithPDF(EvalEvaluation evaluation, EvalTemplate template,
            List<EvalItem> allEvalItems, List<EvalTemplateItem> allEvalTemplateItems,
            List<String> topRow, List<List<String>> responseRows, int numOfResponses,
            String[] groupIDs) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            
            // make a standard place in the eval webapp to put your custom image
            Image gif = Image.getInstance(new URL("http://localhost:8080/library/skin/default/images/logo_inst.gif"));
            document.add(gif);
            
            // Title of Survey
            Paragraph title = new Paragraph("Title of Survey");
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Account Name
            Paragraph accountName = new Paragraph("Account Name");
            accountName.setAlignment(Element.ALIGN_CENTER);
            document.add(accountName);
            
            // Table with info on,
            // Carried out:  12th July 07 - 18th July 07
            // Response rate:  76% ( 80 / 126 )
            // Invitees: IB PMS 07/08
            PdfPTable table = new PdfPTable(2);
            table.addCell("Carried out:");
            table.addCell("12th July 07 - 18th July 07");
            table.addCell("Response Rate:");
            table.addCell("76% ( 80 / 126 )");
            table.addCell("Invitees:");
            table.addCell("IB PMS 07/08");
            document.add(table);
            
            for (int i = 0; i < topRow.size(); i++) {
                Paragraph question = new Paragraph(i + ". " + topRow.get(i));
                document.add(question);
            }
            
            document.close();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    private void respondWithExcel(EvalEvaluation evaluation, EvalTemplate template,
            List<EvalItem> allEvalItems, List<EvalTemplateItem> allEvalTemplateItems,
            List<String> topRow, List<List<String>> responseRows, int numOfResponses,
            String[] groupIDs) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("First Sheet");
        
        // Evaluation Title
        HSSFRow row1 = sheet.createRow(0);
        HSSFCell cellA1 = row1.createCell((short)0);
        cellA1.setCellValue(evaluation.getTitle());
        
        HSSFRow row2 = sheet.createRow(1);
        HSSFCell cellA2 = row2.createCell((short)0);
        
        // Response Rate calculation... this is sort of duplicated code from ControlEvaluationsProducer
        // might be good to put it in one of the logic or utility classes.
        int countResponses = responsesLogic.countResponses(evaluation.getId(), null);
        int countEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
        long percentage = 0;
        if (countEnrollments > 0) {
           percentage = Math.round(  (((float)countResponses) / (float)countEnrollments) * 100.0 );
           cellA2.setCellValue(percentage + "% response rate (" + countResponses + "/" + countEnrollments + ")");
           //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "/"
           //      + countEnrollments + " - " + percentage + "%");
        } else {
           // don't bother showing percentage or "out of" when there are no enrollments
           //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "");
           cellA2.setCellValue(countResponses + " responses");
        }
        
        // Participants listing
        List<String> groupTitles = new ArrayList<String>();
        
        //TODO Ask Az the right way to get the Group Titles since I don't see
        // any instantiated beans for EvalGroupProvider
        //for (String groupID: groupIDs) {
        //    EvalGroup group = evalGroupsProvider.getGroupByGroupId(groupID);
        //    if (group != null)
        //        groupTitles.add(group.title);
        //}
        
        //if (groupTitles.size() > 0) {
        if (groupIDs.length > 0) {
            HSSFRow row3 = sheet.createRow(2);
            HSSFCell cellA3 = row3.createCell((short)0);

            String groupsCellContents = "Participants: ";
            for (int groupCounter = 0; groupCounter < groupIDs.length; groupCounter++) {//groupTitles.size(); groupCounter++) {
                groupsCellContents +=  groupIDs[groupCounter]; //groupTitles.get(groupCounter);
                if (groupCounter+1 < groupIDs.length) {//groupTitles.size()) {
                    groupsCellContents += ", ";
                }
            }
            cellA3.setCellValue(groupsCellContents);
        }
        
        // Questions types (just above header row)
        HSSFRow questionTypeRow = sheet.createRow((short)4);
        for (int i = 0; i < allEvalTemplateItems.size(); i++) {
            EvalTemplateItem tempItem = allEvalTemplateItems.get(i);
            HSSFCell cell = questionTypeRow.createCell((short)(i+1));
            if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_SCALED)) {
                cell.setCellValue("Rating scale");
            }
            else if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_TEXT)) {
                cell.setCellValue("Free text / essay question");
            }
            else {
                cell.setCellValue("");
            }
        }
        
        // Header Row
        HSSFRow headerRow = sheet.createRow((short)5);
        for (int i = 0; i < topRow.size(); i++) {
            // Adding one because we want the first column to be a numbered list.
            HSSFCell cell = headerRow.createCell((short)(i+1));
            cell.setCellValue(((String)topRow.get(i)));
        }
        
        // Fill in the rest
        for (int i = 0; i < responseRows.size(); i++) {
            HSSFRow row = sheet.createRow((short)(i+6)); 
            HSSFCell indexCell = row.createCell((short)0);
            indexCell.setCellValue(i+1);
            List<String> responses = (List<String>) responseRows.get(i);
            for (int j = 0 ; j < responses.size(); j++) {
                HSSFCell responseCell = row.createCell((short)(j+1));
                responseCell.setCellValue(responses.get(j));
            }
        }
        
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "inline");
        response.setHeader("filename", "report.xls");

        // dump the output to the response stream
        try {
            wb.write(response.getOutputStream());
        } catch (IOException e) {
            throw UniversalRuntimeException.accumulate(e, "Could not get Writer to dump output to csv");
        }
    }
    
    private void respondWithCSV(List topRow, List responseRows, int numOfResponses) {
        Writer stringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter, COMMA);
        
      //convert the top row to an array
        String[] topRowArray = new String[topRow.size()];
        for (int i = 0; i < topRow.size(); i++) {
            topRowArray[i] = (String) topRow.get(i);
        }
        //write the top row to CSVWriter object
        writer.writeNext(topRowArray);
        
        //for each response
        for (int i = 0; i < numOfResponses; i++) {
            List currRow = (List) responseRows.get(i);
            //convert the current response to an array
            String[] currRowArray = new String[currRow.size()];
            for (int j = 0; j < currRow.size(); j++) {
                currRowArray[j] = (String) currRow.get(j);
            }
            //writer the current response to CSVWriter object
            writer.writeNext(currRowArray);
        }
        
        response.setContentType("text/x-csv");
        response.setHeader("Content-disposition", "inline");
        response.setHeader("filename", "report.csv");
        String myCSV = stringWriter.toString();
        try {
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // dump the output to the response stream
        try {
            Writer w = response.getWriter();
            w.write(myCSV);
        } catch (IOException e) {
            throw UniversalRuntimeException.accumulate(e, "Could not get Writer to dump output to csv");
        }
    }

    /**
     * This method iterates through list of answers for the concerened question 
     * and updates the list of responses.
     * 
     * @param numOfResponses number of responses for the concerned evaluation
     * @param responseIds list of response ids
     * @param responseRows list containing all responses (i.e. list of answers for each question)
     * @param itemAnswers list of answers for the concerened question
     * @param tempItem1 EvalTemplateItem object for which the answers are fetched
     * @param item1 EvalItem object for which the answers are fetched
     */
    private void updateResponseList(int numOfResponses, List responseIds, List responseRows, List itemAnswers,
            EvalTemplateItem tempItem1, EvalItem item1) {

        /* 
         * Fix for EVALSYS-123 i.e. export CSV functionality 
         * fails when answer for a question left unanswered by 
         * student.
         * 
         * Basically we need to check if the particular student 
         * (identified by a response id) has answered a particular
         * question. If yes, then add the answer to the list, else
         * add empty string - kahuja 23rd Apr 2007. 
         */
        int actualIndexOfResponse = 0;
        int idealIndexOfResponse = 0;
        List currRow = null;
        int lengthOfAnswers = itemAnswers.size();
        for (int j = 0; j < lengthOfAnswers; j++) {

            EvalAnswer currAnswer = (EvalAnswer) itemAnswers.get(j);
            actualIndexOfResponse = responseIds.indexOf(currAnswer.getResponse().getId());

            // Fill empty answers if the answer corresponding to a response is not in itemAnswers list. 
            if (actualIndexOfResponse > idealIndexOfResponse) {
                for (int count = idealIndexOfResponse; count < actualIndexOfResponse; count++) {
                    currRow = (List) responseRows.get(idealIndexOfResponse);
                    currRow.add(" ");
                }
            }

            /*
             * Add the answer to item within the current response to the output row.
             * If text/essay type item just add the text 
             * else (scaled type or block child, which is also scaled) item then look up the label
             */
            currRow = (List) responseRows.get(actualIndexOfResponse);
            if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_TEXT)) {
                currRow.add(currAnswer.getText());
            } else {
                String labels[] = item1.getScale().getOptions();
                currRow.add(labels[currAnswer.getNumeric().intValue()]);
            }

            /*
             * Update the ideal index to "actual index + 1" 
             * because now actual answer has been added to list.
             */
            idealIndexOfResponse = actualIndexOfResponse + 1;
        }

        // If empty answers occurs at end such that all responses have not been filled.
        for (int count = idealIndexOfResponse; count < numOfResponses; count++) {
            currRow = (List) responseRows.get(idealIndexOfResponse);
            currRow.add(" ");
        }

    }

    /**
     * More duplicated code from ControlEvaluationsProducer
     * 
     * Gets the total count of enrollments for an evaluation
     * 
     * @param evaluationId
     * @return total number of users with take eval perms in this evaluation
     */
    private int getTotalEnrollmentsForEval(Long evaluationId) {
        int totalEnrollments = 0;
        Map<Long, List<EvalAssignGroup>> evalAssignGroups = evalsLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
        List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
        for (int i=0; i<groups.size(); i++) {
            EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
            String context = eac.getEvalGroupId();
            Set<String> userIds = externalLogic.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
            totalEnrollments = totalEnrollments + userIds.size();
        }
        return totalEnrollments;
    }
}
