package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import uk.org.ponder.util.UniversalRuntimeException;
import uk.org.ponder.messageutil.MessageLocator;

/*
 * This utility class is responsible for creating convenient arrays and other 
 * things that collect all the responses for an evaluation. An example is a 2D
 * List containing all the responses ready to be fed into an excel or csv file.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 * @author Steven Githens (swgithen@mtu.edu)
 */
public class EvalResponseAggregatorUtil {
   private static Log log = LogFactory.getLog(EvalResponseAggregatorUtil.class);
   
   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }
   
   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }
   
   private MessageLocator messageLocator;
   public void setMessageLocator(MessageLocator locator) {
      this.messageLocator = locator;
   }

   public EvalAggregatedResponses getAggregatedResponses(EvalEvaluation evaluation, String[] groupIds) {
      List<String> topRow = new ArrayList<String>(); //holds top row (item text)
      List<EvalItem> allEvalItems = new ArrayList<EvalItem>(); //holds all expanded eval items (blocks are expanded here)
      List<EvalTemplateItem> allEvalTemplateItems = new ArrayList<EvalTemplateItem>(); 
      List<List<String>> responseRows = new ArrayList<List<String>>();//holds response rows

      EvalTemplate template = evaluation.getTemplate();
      
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
      List<Long> responseIds = evaluationService.getResponseIds(evaluation.getId(), groupIds, true);
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
               List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(item1.getId(), evaluation.getId(), groupIds);
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
                  List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(child.getId(), evaluation.getId(), groupIds);
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
      
      return new EvalAggregatedResponses(evaluation,groupIds,allEvalItems,allEvalTemplateItems,
            topRow, responseRows, numOfResponses);
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
         
         EvalUtils.decodeAnswerNA(currAnswer);
         
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
         if (currAnswer.NA) {
            currRow.add(messageLocator.getMessage("reporting.notapplicable.shortlabel"));
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(TemplateItemUtils.getTemplateItemType(tempItem1))) {
            currRow.add(currAnswer.getText());
         } 
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(TemplateItemUtils.getTemplateItemType(tempItem1))) {
            String labels[] = item1.getScale().getOptions();
            StringBuilder sb = new StringBuilder();
            Integer[] decoded = EvalUtils.decodeMultipleAnswers(currAnswer.getMultiAnswerCode());
            for (int k = 0; k < decoded.length; k++) {
               sb.append(labels[decoded[k].intValue()]);
               if (k+1 < decoded.length) 
                  sb.append(",");
            }
            currRow.add(sb.toString());
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(TemplateItemUtils.getTemplateItemType(tempItem1)) ||
               EvalConstants.ITEM_TYPE_SCALED.equals(TemplateItemUtils.getTemplateItemType(tempItem1))) {
            String labels[] = item1.getScale().getOptions();
            currRow.add(labels[currAnswer.getNumeric().intValue()]);
         }
         else {
            throw new UniversalRuntimeException("Trying to add an unsupported question type to the Spreadsheet Data Lists.");
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
   
   /*
    * This method deals with producing an array of total number of responses for
    * a list of answers.  It does not deal with any of the logic (such as groups,
    * etc.) it takes to get a list of answers.
    * 
    * The item type should be the expected constant from EvalConstants. For 
    * scaled and multiple choice questions, this will count the answers numeric
    * field for each scale.  For multiple answers, it will aggregate all the responses
    * for each answer to their scale item.
    * 
    * If the item allows Not Applicable answers, this tally will be included as 
    * the very last item of the array, allowing you to still match up the indexes
    * with the original Scale options.
    * 
    * @param itemType The Item type. Should be one of EvalConstants.ITEM_TYPE_SCALED,
    * EvalConstants.ITEM_TYPE_MULTIPLECHOICE, or EvalConstants.ITEM_TYPE_MULTIPLEANSWER
    * @param scaleSize The size of the scale items. The returned integer array will
    * be this big. With each index being a count of responses for that scale type.
    * @param answers The List of EvalAnswers to work with.
    */
   public int[] countResponseChoices(String itemType, int scaleSize, List<EvalAnswer> itemAnswers) {
       // Make the array one size larger in case we need to add N/A tallies.
       int[] togo = new int[scaleSize+1];

       if (!EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType) &&
             !EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType) &&
             !EvalConstants.ITEM_TYPE_SCALED.equals(itemType)) {
          throw new IllegalArgumentException("The itemType needs to be ITEM_TYPE_MULTIPLEANSWER, ITEM_TYPE_MULTIPLECHOICE, or ITEM_TYPE_SCALED");
       }
       
       for (EvalAnswer answer: itemAnswers) {
          EvalUtils.decodeAnswerNA(answer);
          if (answer.NA) {
             togo[togo.length-1]++;
          }
          else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType)) {
             Integer[] decoded = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
             for (Integer decodedAnswer: decoded) {
                togo[decodedAnswer.intValue()]++;
             }
          }
          else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType) || 
               EvalConstants.ITEM_TYPE_SCALED.equals(itemType)) {
             if (answer.getNumeric().intValue() > 0) {
                togo[answer.getNumeric().intValue()]++;
             }
          }
          else {
             throw new RuntimeException("This shouldn't happen");
          }
       }

       return togo;
   }
}
