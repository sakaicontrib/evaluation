package org.sakaiproject.evaluation.tool.utils;

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

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

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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
    // FIXME UNUSED method
    //   private void updateResponseList(int numOfResponses, List<Long> responseIds, List<List<String>> responseRows, List<EvalAnswer> itemAnswers,
    //         EvalTemplateItem templateItem) {
    //
    //      /* 
    //       * Fix for EVALSYS-123 i.e. export CSV functionality 
    //       * fails when answer for a question left unanswered by 
    //       * student.
    //       * 
    //       * Basically we need to check if the particular student 
    //       * (identified by a response id) has answered a particular
    //       * question. If yes, then add the answer to the list, else
    //       * add empty string - kahuja 23rd Apr 2007. 
    //       */
    //      int actualIndexOfResponse = 0;
    //      int idealIndexOfResponse = 0;
    //      List<String> currRow = null;
    //      int lengthOfAnswers = itemAnswers.size();
    //      for (int j = 0; j < lengthOfAnswers; j++) {
    //
    //         EvalAnswer currAnswer = (EvalAnswer) itemAnswers.get(j);
    //         actualIndexOfResponse = responseIds.indexOf(currAnswer.getResponse().getId());
    //
    //         EvalUtils.decodeAnswerNA(currAnswer);
    //
    //         // Fill empty answers if the answer corresponding to a response is not in itemAnswers list. 
    //         if (actualIndexOfResponse > idealIndexOfResponse) {
    //            for (int count = idealIndexOfResponse; count < actualIndexOfResponse; count++) {
    //               currRow = responseRows.get(idealIndexOfResponse);
    //               currRow.add(" ");
    //            }
    //         }
    //
    //         /*
    //          * Add the answer to item within the current response to the output row.
    //          * If text/essay type item just add the text 
    //          * else (scaled type or block child, which is also scaled) item then look up the label
    //          */
    //         String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
    //         currRow = responseRows.get(actualIndexOfResponse);
    //         if (currAnswer.NA) {
    //            currRow.add(messageLocator.getMessage("reporting.notapplicable.shortlabel"));
    //         }
    //         else if (EvalConstants.ITEM_TYPE_TEXT.equals(itemType)) {
    //            currRow.add(currAnswer.getText());
    //         } 
    //         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType)) {
    //            String labels[] = templateItem.getItem().getScale().getOptions();
    //            StringBuilder sb = new StringBuilder();
    //            Integer[] decoded = EvalUtils.decodeMultipleAnswers(currAnswer.getMultiAnswerCode());
    //            for (int k = 0; k < decoded.length; k++) {
    //               sb.append(labels[decoded[k].intValue()]);
    //               if (k+1 < decoded.length) 
    //                  sb.append(",");
    //            }
    //            currRow.add(sb.toString());
    //         }
    //         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType) 
    //               || EvalConstants.ITEM_TYPE_SCALED.equals(itemType)
    //               || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(itemType)) {
    //            String labels[] = templateItem.getItem().getScale().getOptions();
    //            currRow.add(labels[currAnswer.getNumeric().intValue()]);
    //         }
    //         else {
    //            throw new UniversalRuntimeException("Trying to add an unsupported question type ("+itemType+") " 
    //                  + "for template item ("+templateItem.getId()+") to the Spreadsheet Data Lists");
    //         }
    //
    //         /*
    //          * Update the ideal index to "actual index + 1" 
    //          * because now actual answer has been added to list.
    //          */
    //         idealIndexOfResponse = actualIndexOfResponse + 1;
    //      }
    //
    //      // If empty answers occurs at end such that all responses have not been filled.
    //      for (int count = idealIndexOfResponse; count < numOfResponses; count++) {
    //         currRow = responseRows.get(idealIndexOfResponse);
    //         currRow.add(" ");
    //      }
    //
    //   }

    public String formatForSpreadSheet(EvalTemplateItem templateItem, EvalAnswer answer) {
        String togo = "";

        String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
        EvalUtils.decodeAnswerNA(answer);
        if (answer.NA) {
            togo = messageLocator.getMessage("reporting.notapplicable.shortlabel");
        }
        else if (EvalConstants.ITEM_TYPE_TEXT.equals(itemType)) {
            togo = commonLogic.makePlainTextFromHTML( answer.getText() );
        } 
        else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType)) {
            String labels[] = templateItem.getItem().getScale().getOptions();
            StringBuilder sb = new StringBuilder();
            Integer[] decoded = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
            for (int k = 0; k < decoded.length; k++) {
                if (k > 0) {
                    sb.append(",");
                }
                int decode = decoded[k].intValue();
                if (decode >= 0 && decode < labels.length) {
                    sb.append( labels[decoded[k].intValue()] );
                } else {
                    sb.append(decode);
                }
            }
            togo = sb.toString();
        }
        else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType) 
                || EvalConstants.ITEM_TYPE_SCALED.equals(itemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(itemType)) {
            String labels[] = templateItem.getItem().getScale().getOptions();
            int value = answer.getNumeric().intValue();
            if (value >= 0 && value < labels.length) {
                togo = labels[value];
            } else {
                togo = value + "";
            }
        }
        else {
            throw new UniversalRuntimeException("Trying to add an unsupported question type ("+itemType+") " 
                    + "for template item ("+templateItem.getId()+") to the Spreadsheet Data Lists");
        }
        return togo;
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
     * @deprecated use {@link TemplateItemDataList#getAnswerChoicesCounts(String, int, List)}
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
                    // special handling for the multiple answer items
                    if (! EvalConstants.NO_MULTIPLE_ANSWER.equals(answer.getMultiAnswerCode())) {
                        Integer[] decoded = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
                        for (Integer decodedAnswer: decoded) {
                            int answerValue = decodedAnswer.intValue();
                            if (answerValue >= 0 && answerValue < togo.length) {
                                // answer will fit in the array
                                togo[answerValue]++;
                            } else {
                                // put it in the NA slot
                                togo[togo.length-1]++;
                            }
                        }
                    }
                }
                else {
                    // standard handling for single answer items
                    int answerValue = answer.getNumeric().intValue();
                    if (! EvalConstants.NO_NUMERIC_ANSWER.equals(answerValue)) {
                        // this numeric answer is not one that should be ignored
                        if (answerValue >= 0 && answerValue < togo.length) {
                            // answer will fit in the array
                            togo[answerValue]++;
                        } else {
                            // put it in the NA slot
                            togo[togo.length-1]++;
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The itemType needs to be one that has numeric answers, this one is invalid: " + templateItemType);
        }

        return togo;
    }


    /**
     * Does the preparation work for getting the TIDL.  this is just a passthrough to 
     * {@link TemplateItemDataList#TemplateItemDataList(Long, String[], EvalAuthoringService, EvalDeliveryService, ExternalHierarchyLogic)} 
     * 
     * @param evaluationId
     * @param groupIds
     * @return a TIDL which is built for the given eval and group ids
     */
    public TemplateItemDataList prepareTemplateItemDataStructure(Long evaluationId, String[] groupIds) {
        TemplateItemDataList tidl = new TemplateItemDataList(evaluationId, groupIds,
                authoringService, deliveryService, hierarchyLogic);
        return tidl;
    }

    /**
     * Returns a comma separated list of the human readable names for the array
     * of group ids.  This is used in a number of the reporting classes.
     * 
     * @param groupIds
     * @return a human readable string
     */
    public String getCommaSeparatedGroupNames(String[] groupIds) {
        StringBuilder groupsString = new StringBuilder();
        for (int groupCounter = 0; groupCounter < groupIds.length; groupCounter++) {
            if (groupCounter > 0) {
                groupsString.append(", ");
            }
            groupsString.append( commonLogic.getDisplayTitle(groupIds[groupCounter]) );
        }
        return groupsString.toString();
    }

    /**
     * Get the translated label for the template item type destined for the 
     * spreadsheet header.
     * 
     * @param templateItemType
     * @return the header label string
     */
    public String getHeaderLabelForItemType(String templateItemType) {
        String togo;
        if (EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.scaled");
        }
        else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.text");
        }
        else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.multianswer");
        }
        else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.multichoice");
        }
        else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.block");
        }
        else if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType)) {
            togo = messageLocator.getMessage("item.classification.header");
        }
        else {
            togo = "";
        }
        return togo;
    }

}
