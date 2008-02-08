/**
 * $Id: ReportHandlerHook.java 11234 Jan 21, 2008 11:35:56 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * ReportHandlerHook.java - evaluation - 23 Jan 2007 11:35:56 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ExcelReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.PDFReportViewParams;

import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Handles the generation of files for exporting results
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 * @author Steven Githens
 */
public class ReportHandlerHook implements HandlerHook {

   private static Log log = LogFactory.getLog(ReportHandlerHook.class);

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   private CSVReportExporter csvReportExporter;
   public void setCsvReportExporter(CSVReportExporter csvReportExporter) {
      this.csvReportExporter = csvReportExporter;
   }

   private XLSReportExporter xlsReportExporter;
   public void setXlsReportExporter(XLSReportExporter xlsReportExporter) {
      this.xlsReportExporter = xlsReportExporter;
   }

   private PDFReportExporter pdfReportExporter;
   public void setPdfReportExporter(PDFReportExporter pdfReportExporter) {
      this.pdfReportExporter = pdfReportExporter;
   }

   private ViewParameters viewparams;
   public void setViewparams(ViewParameters viewparams) {
      this.viewparams = viewparams;
   }
   
   private HttpServletResponse response;
   public void setResponse(HttpServletResponse response) {
      this.response = response;
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
      EvalEvaluation evaluation = evaluationService.getEvaluationById(drvp.evalId);
      EvalTemplate template = evaluation.getTemplate();

      //SWG Copying the lame permission check for now, to make sure there is at least something here.
      String currentUserId = externalLogic.getCurrentUserId();
      // do a permission check
      if (!currentUserId.equals(evaluation.getOwner()) && 
            !externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
         throw new SecurityException("Invalid user attempting to access report downloads: " + currentUserId);
      }

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
       * add empty space instead
       * 
       * b) Side purpose: countResponses method in EvalDeliveryService
       * does not take array of groups ids
       */
      List<Long> responseIds = evaluationService.getEvalResponseIds(drvp.evalId, drvp.groupIds, true);
      int numOfResponses = responseIds.size();

      //add a row for each response
      for (int i = 0; i < numOfResponses; i++) {
         List<String> currResponseRow = new ArrayList<String>();
         responseRows.add(currResponseRow);
      }

      //get all items
      List<EvalTemplateItem> allItems = new ArrayList<EvalTemplateItem>(template.getTemplateItems());

      if (!allItems.isEmpty()) {
         //filter out the block child items, to get a list non-child items
         List<EvalTemplateItem> ncItemsList = TemplateItemUtils.getNonChildItems(allItems);
         Collections.sort(ncItemsList, new ComparatorsUtils.TemplateItemComparatorByOrder());
         //for each item
         for (int i = 0; i < ncItemsList.size(); i++) {
            //fetch the item
            EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
            allEvalTemplateItems.add(tempItem1);
            EvalItem item1 = tempItem1.getItem();

            //if the item is normal scaled or text (essay)
            if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_SCALED)
                  || TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_TEXT)
                  || TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_MULTIPLEANSWER)
                  || TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_MULTIPLECHOICE)) {

               //add the item description to the top row
               // This is rich text, each particular output format can decide if it needs to be flattened.
               topRow.add(item1.getItemText());
               allEvalItems.add(item1);

               //get all answers to this item within this evaluation
               List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(item1.getId(), drvp.evalId, drvp.groupIds);
               updateResponseList(numOfResponses, responseIds, responseRows, itemAnswers, tempItem1, item1);

            }
            // block parent type (block child handled inside this)
            else if (TemplateItemUtils.getTemplateItemType(tempItem1).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
               //add the block description to the top row
               topRow.add(item1.getItemText());
               allEvalItems.add(item1);
               for (int j = 0; j < numOfResponses; j++) {
                  List<String> currRow = responseRows.get(j);
                  //add blank response to block parent row
                  currRow.add("");
               }

               //get child block items
               List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allItems, tempItem1.getId());
               for (int j = 0; j < childList.size(); j++) {
                  EvalTemplateItem tempItemChild = (EvalTemplateItem) childList.get(j);
                  allEvalTemplateItems.add(tempItemChild);
                  EvalItem child = tempItemChild.getItem();
                  //add child's text to top row
                  topRow.add(child.getItemText());
                  allEvalItems.add(child);
                  //get all answers to the child item within this eval
                  List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(child.getId(), drvp.evalId, drvp.groupIds);
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

      OutputStream resultsOutputStream = null;
      try {
         resultsOutputStream = response.getOutputStream(); 
      }
      catch (IOException ioe) {
         throw UniversalRuntimeException.accumulate(ioe, "Unable to get response stream for Evaluation Results Export");
      }
      
      // Response Headers that are the same for all Output types
      response.setHeader("Content-disposition", "inline");
      
      if (drvp instanceof CSVReportViewParams) {
         response.setContentType("text/x-csv");
         response.setHeader("filename", "report.csv");
         csvReportExporter.respondWithCSV(topRow, responseRows, numOfResponses, 
               resultsOutputStream);
      }
      else if (drvp instanceof ExcelReportViewParams) {
         response.setContentType("application/vnd.ms-excel");
         response.setHeader("filename", "report.xls");
         xlsReportExporter.respondWithExcel(evaluation, template, allEvalItems, 
               allEvalTemplateItems, topRow, responseRows, numOfResponses, drvp.groupIds,
               resultsOutputStream);
      }
      else if (drvp instanceof PDFReportViewParams) {
         response.setContentType("application/pdf");
         response.setHeader("filename", "report.pdf");
         pdfReportExporter.respondWithPDF(evaluation, template, allEvalItems, 
               allEvalTemplateItems, topRow, responseRows, numOfResponses, drvp.groupIds,
               resultsOutputStream);
      }
      return true;
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
   private void updateResponseList(int numOfResponses, List<Long> responseIds, List<List<String>> responseRows, List<EvalAnswer> itemAnswers,
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
      List<String> currRow = null;
      int lengthOfAnswers = itemAnswers.size();
      for (int j = 0; j < lengthOfAnswers; j++) {

         EvalAnswer currAnswer = (EvalAnswer) itemAnswers.get(j);
         actualIndexOfResponse = responseIds.indexOf(currAnswer.getResponse().getId());

         // Fill empty answers if the answer corresponding to a response is not in itemAnswers list. 
         if (actualIndexOfResponse > idealIndexOfResponse) {
            for (int count = idealIndexOfResponse; count < actualIndexOfResponse; count++) {
               currRow = responseRows.get(idealIndexOfResponse);
               currRow.add(" ");
            }
         }

         /*
          * Add the answer to item within the current response to the output row.
          * If text/essay type item just add the text 
          * else (scaled type or block child, which is also scaled) item then look up the label
          */
         currRow = responseRows.get(actualIndexOfResponse);
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
         currRow = responseRows.get(idealIndexOfResponse);
         currRow.add(" ");
      }

   }

}
