package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * This utility class is responsible for creating convenient arrays and other 
 * things that collect all the responses for an evaluation. An example is a 2D
 * List containing all the responses ready to be fed into an excel or csv file.
 * 
 * @author Steven Githens (swgithen@mtu.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
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

   private EvalExternalLogic externalLogic;
   public void setEvalExternalLogic(EvalExternalLogic logic) {
      this.externalLogic = logic;
   }


   /**
    * FIXME - this should us the new {@link DataTemplateItem}s and the {@link TemplateItemDataList}
    * 
    * @param evaluation
    * @param groupIds
    * @return
    */
   public EvalAggregatedResponses getAggregatedResponses(EvalEvaluation evaluation, String[] groupIds) {
      List<String> topRow = new ArrayList<String>(); //holds top row (item text)
      List<EvalItem> allEvalItems = new ArrayList<EvalItem>(); //holds all expanded eval items (blocks are expanded here)
      List<EvalTemplateItem> allEvalTemplateItems = new ArrayList<EvalTemplateItem>(); 
      List<List<String>> responseRows = new ArrayList<List<String>>();//holds response rows

      EvalTemplate template = evaluation.getTemplate(); // LAZY LOAD

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

      // get all answerable template items
      List<EvalTemplateItem> allTemplateItems = new ArrayList<EvalTemplateItem>(template.getTemplateItems()); // LAZY LOAD
      // FIXME - do this using the authoringService
//    allItems = authoringService.getTemplateItemsForEvaluation(evaluationId, hierarchyNodeIDs, 
//    instructorIds, new String[] {evalGroupId});

      if (! allTemplateItems.isEmpty()) {
         //filter out the block child items, to get a list non-child items
         List<EvalTemplateItem> ncItemsList = TemplateItemUtils.getNonChildItems(allTemplateItems);
         Collections.sort(ncItemsList, new ComparatorsUtils.TemplateItemComparatorByOrder());

         // for each templateItem
         for (int i = 0; i < ncItemsList.size(); i++) {
            //fetch the item
            EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);
            String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
            allEvalTemplateItems.add(templateItem);
            EvalItem item = templateItem.getItem();

            // if this is a non-block type
            if (EvalConstants.ITEM_TYPE_HEADER.equals(itemType)
                  || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(itemType)) {
               // do nothing with these types, children handled by the parents and header not needed for export
            } else if (TemplateItemUtils.isBlockParent(templateItem)) {
               //add the block description to the top row
               topRow.add(item.getItemText());
               allEvalItems.add(item);
               for (int j = 0; j < numOfResponses; j++) {
                  List<String> currRow = responseRows.get(j);
                  //add blank response to block parent row
                  currRow.add("");
               }

               //get child block items
               List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
               for (int j = 0; j < childList.size(); j++) {
                  EvalTemplateItem tempItemChild = (EvalTemplateItem) childList.get(j);
                  allEvalTemplateItems.add(tempItemChild);
                  EvalItem child = tempItemChild.getItem();
                  //add child's text to top row
                  topRow.add(child.getItemText());
                  allEvalItems.add(child);
                  //get all answers to the child item within this eval
                  List<EvalAnswer> itemAnswers = deliveryService.getAnswersForEval(evaluation.getId(), groupIds, new Long[] {tempItemChild.getId()});
                  updateResponseList(numOfResponses, responseIds, responseRows, itemAnswers, tempItemChild);
               }
            } else {
               // this should be one of the non-block answerable types (updateResponseList will check the types)

               //add the item description to the top row
               // This is rich text, each particular output format can decide if it needs to be flattened.
               topRow.add(item.getItemText());
               allEvalItems.add(item);

               //get all answers to this item within this evaluation
               List<EvalAnswer> itemAnswers = deliveryService.getAnswersForEval(evaluation.getId(), groupIds, new Long[] {templateItem.getId()});
               updateResponseList(numOfResponses, responseIds, responseRows, itemAnswers, templateItem);

            }
         }
      }

      return new EvalAggregatedResponses(evaluation,groupIds,allEvalItems,allEvalTemplateItems,
            topRow, responseRows, numOfResponses);
   }


   /**
    * This method will go through a list of template items, looking at items that
    * are of Instructor type, and create a Map of all the instructors userId's and
    * EvalUsers. This is useful for all the reporting types that need to show
    * the instructors seperately, and need to sort them before hand, or generally
    * know who they are.
    * 
    * @param answers a list of all the answers to pull out the instructor userIds from
    * @return Returns a Map of the instructors as EvalUsers keyed by userId
    */
   public Map<String, EvalUser> getInstructorsForAnsweredItems(List<EvalAnswer> answers) {
      Map<String,EvalUser> instructors = new HashMap<String,EvalUser>();
      for (EvalAnswer answer: answers) {
         // If the answers associated type is instructor and we haven't added
         // it to the Map yet, we should do so.
         if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(answer.getAssociatedType()) &&
               ! instructors.containsKey(answer.getAssociatedId())) {
            if (answer.getAssociatedId() == null) {
               log.warn("EvalAnswer has associated type instructor but no associated id. EvalAnswer.id="+answer.getId());
            }
            else {
               instructors.put(answer.getAssociatedId(), externalLogic.getEvalUserById(answer.getAssociatedId()));
            }
         }
      }
      return instructors;
   }


   /**
    * This method iterates through list of answers for the concerned question 
    * and updates the list of responses.
    * 
    * @param numOfResponses number of responses for the concerned evaluation
    * @param responseIds list of response ids
    * @param responseRows list containing all responses (i.e. list of answers for each question)
    * @param itemAnswers list of answers for the concerened question
    * @param templateItem EvalTemplateItem object for which the answers are fetched
    */
   private void updateResponseList(int numOfResponses, List<Long> responseIds, List<List<String>> responseRows, List<EvalAnswer> itemAnswers,
         EvalTemplateItem templateItem) {

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
         String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
         currRow = responseRows.get(actualIndexOfResponse);
         if (currAnswer.NA) {
            currRow.add(messageLocator.getMessage("reporting.notapplicable.shortlabel"));
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(itemType)) {
            currRow.add(currAnswer.getText());
         } 
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType)) {
            String labels[] = templateItem.getItem().getScale().getOptions();
            StringBuilder sb = new StringBuilder();
            Integer[] decoded = EvalUtils.decodeMultipleAnswers(currAnswer.getMultiAnswerCode());
            for (int k = 0; k < decoded.length; k++) {
               sb.append(labels[decoded[k].intValue()]);
               if (k+1 < decoded.length) 
                  sb.append(",");
            }
            currRow.add(sb.toString());
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType) 
               || EvalConstants.ITEM_TYPE_SCALED.equals(itemType)
               || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(itemType)) {
            String labels[] = templateItem.getItem().getScale().getOptions();
            currRow.add(labels[currAnswer.getNumeric().intValue()]);
         }
         else {
            throw new UniversalRuntimeException("Trying to add an unsupported question type ("+itemType+") " 
                  + "for template item ("+templateItem.getId()+") to the Spreadsheet Data Lists");
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

   // STATIC METHODS

   /**
    * This static method deals with producing an array of total number of responses for
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
    * @param templateItemType The template item type. Should be like EvalConstants.ITEM_TYPE_SCALED
    * @param scaleSize The size of the scale items. The returned integer array will
    * be this big (+1 for NA). With each index being a count of responses for that scale type.
    * @param answers The List of EvalAnswers to work with.
    */
   public static int[] countResponseChoices(String templateItemType, int scaleSize, List<EvalAnswer> itemAnswers) {
      // Make the array one size larger in case we need to add N/A tallies.
      int[] togo = new int[scaleSize+1];

      if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType) 
            || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType) 
            || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)
            || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType) ) {
         
         for (EvalAnswer answer: itemAnswers) {
            EvalUtils.decodeAnswerNA(answer);
            if (answer.NA) {
               togo[togo.length-1]++;
            }
            else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
               Integer[] decoded = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
               for (Integer decodedAnswer: decoded) {
                  togo[decodedAnswer.intValue()]++;
               }
            }
            else {
               if (answer.getNumeric().intValue() > 0) {
                  togo[answer.getNumeric().intValue()]++;
               }
            }
         }
      } else {
         throw new IllegalArgumentException("The itemType needs to be one that has numeric answers, this one is invalid: " + templateItemType);
      }

      return togo;
   }

}
