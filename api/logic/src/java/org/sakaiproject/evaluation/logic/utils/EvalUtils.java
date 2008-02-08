/**
 * $Id: EvalUtils.java 1000 Dec 25, 2006 12:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalUtils.java - evaluation - Dec 28, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This class contains various model related utils needed in many areas
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalUtils {

   /**
    * Get the state of an evaluation object<br/>
    * <b>WARNING:</b> do NOT use this outside the logic or DAO layers
    * 
    * @param eval the evaluation object
    * @return the EVALUATION_STATE constant
    */
   public static String getEvaluationState(EvalEvaluation eval) {
      Date today = new Date();
      String state = EvalConstants.EVALUATION_STATE_UNKNOWN;
      try {
         if ( eval.getStartDate().after(today) ) {
            state = EvalConstants.EVALUATION_STATE_INQUEUE;
         } else if ( eval.getDueDate().after(today) ) {
            state = EvalConstants.EVALUATION_STATE_ACTIVE;
         } else if ( eval.getStopDate().after(today) ) {
            state = EvalConstants.EVALUATION_STATE_DUE;
         } else if ( eval.getViewDate().after(today) ) {
            state = EvalConstants.EVALUATION_STATE_CLOSED;
         } else {
            state = EvalConstants.EVALUATION_STATE_VIEWABLE;
         }
      } catch (NullPointerException e) {
         state = EvalConstants.EVALUATION_STATE_UNKNOWN;
      }
      return state;
   }

   /**
    * Checks if a sharing constant is valid or null
    * 
    * @param sharingConstant a sharing constant from EvalConstants.SHARING_*
    * @throws IllegalArgumentException is the constant is null or does not match the set
    */
   public static boolean validateSharingConstant(String sharingConstant) {
      if ( EvalConstants.SHARING_OWNER.equals(sharingConstant) ||
            EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ||
            EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ||
            EvalConstants.SHARING_SHARED.equals(sharingConstant) ||
            EvalConstants.SHARING_VISIBLE.equals(sharingConstant)) {
         // all is ok
      } else {
         throw new IllegalArgumentException("Invalid sharing constant ("+sharingConstant+"), " +
               "must be one of EvalConstants.SHARING_*");
      }
      return true;
   }

   /**
    * Checks if an email include constant is valid or null
    * 
    * @param includeConstant an email include constant from EvalConstants.EMAIL_INCLUDE_*
    * @throws IllegalArgumentException is the constant is null or does not match the set
    */
   public static boolean validateEmailIncludeConstant(String includeConstant) {
      if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant) ||
            EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant) ||
            EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant) ) {
         // all is ok
      } else {
         throw new IllegalArgumentException("Invalid include constant ("+includeConstant+"), " +
               "must use one of the ones from EvalConstants.EMAIL_INCLUDE_*");
      }
      return true;
   }

   /**
    * Remove all duplicate objects from a list
    * 
    * @param list
    * @return the original list with the duplicate objects removed
    */
   public static <T> List<T> removeDuplicates(List<T> list) {
      Set<T> s = new HashSet<T>();
      for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
         T element = (T) iter.next();
         if (! s.add(element)) {
            iter.remove();
         }
      }
      return list;
   }

   /**
    * Takes 2 lists of group types, {@link EvalGroup} and {@link EvalAssignGroup}, and
    * merges the groups in common and then returns an array of the common groups,
    * comparison is on the evalGroupId
    * 
    * @param evalGroups a list of {@link EvalGroup}
    * @param assignGroups a list of {@link EvalAssignGroup}
    * @return an array of the groups that are in common between the 2 lists
    */
   public static EvalGroup[] getGroupsInCommon(List<EvalGroup> evalGroups, List<EvalAssignGroup> assignGroups) {
      List<EvalGroup> groups = new ArrayList<EvalGroup>();
      for (int i=0; i<evalGroups.size(); i++) {
         EvalGroup group = (EvalGroup) evalGroups.get(i);
         for (int j=0; j<assignGroups.size(); j++) {
            EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(j);
            if (group.evalGroupId.equals(assignGroup.getEvalGroupId())) {
               groups.add(group);
               break;
            }
         }
      }
      return (EvalGroup[]) groups.toArray(new EvalGroup[] {});
   }

   /**
    * Creates a unique title for an adhoc scale
    * @return 
    */
   public static String makeUniqueIdentifier(int maxLength) {
      String newTitle = UUID.randomUUID().toString();
      if (newTitle.length() > maxLength) {
         newTitle = newTitle.substring(0, maxLength);
      }
      return newTitle;
   }

   /**
    * Get a map of answers for the given response, where the key to
    * access a given response is the unique pairing of templateItemId and
    * the associated field of the answer (instructor id, environment key, etc.)
    * 
    * @param response the response we want to get the answers for
    * @return a hashmap of answers, where key = templateItemId + answer.associatedType + answer.associatedId
    */
   public static Map<String, EvalAnswer> getAnswersMapByTempItemAndAssociated(EvalResponse response) {
      Map<String, EvalAnswer> map = new HashMap<String, EvalAnswer>();
      Set<EvalAnswer> answers = response.getAnswers();
      for (Iterator<EvalAnswer> it = answers.iterator(); it.hasNext();) {
         EvalAnswer answer = it.next();
         // decode the stored answers into the int array
         answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
         // place the answers into a map which uses the TI, assocType, and assocId as a key
         map.put(answer.getTemplateItem().getId().toString() + answer.getAssociatedType()
               + answer.getAssociatedId(), answer);
      }
      return map;
   }

   public static String SEPARATOR = ":";

   /**
    * Encodes an array of integers into a string so it can be stored in a format like so:
    * :0:1:4:7:<br/>It will sort the numbers in ascending order<br/>
    * This allows searches to continue to work without use having to resort to another table<br/>
    * Pairs with the {@link #decodeMultipleAnswers(String)} method
    * 
    * @param answerKeys an array of integers, can be null or empty
    * @return the encoded string, will be null if the input is null
    */
   public static String encodeMultipleAnswers(Integer[] answerKeys) {
      String encoded = null;
      if (answerKeys != null && answerKeys.length > 0) {
         Arrays.sort(answerKeys); // sort the keys first
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < answerKeys.length; i++) {
            sb.append(SEPARATOR);
            sb.append(answerKeys[i]);
         }
         sb.append(SEPARATOR);
         encoded = sb.toString();
      }
      return encoded;
   }

   /**
    * Decodes an encoded multiple answer string (e.g. :1:3:) into an array of integers<br/>
    * Array will always be returned in sorted order<br/>
    * Behavior is undefined if the string is not encoded correctly as this method will not
    * attempt to validate the string beforehand
    * 
    * @param encodedAnswers a string encoded using {@link #encodeMultipleAnswers(Integer[])}
    * @return the decoded array of integers or an empty array if the encoded string is empty
    * @throws IllegalArgumentException if the string cannot be decoded correctly
    */
   public static Integer[] decodeMultipleAnswers(String encodedAnswers) {
      Integer[] decoded = null;
      if (encodedAnswers != null && encodedAnswers.length() > 0) {
         if (encodedAnswers.startsWith(SEPARATOR) &&
               encodedAnswers.endsWith(SEPARATOR)) {
            String[] split = encodedAnswers.split(SEPARATOR);
            if (split.length > 2) {
               decoded = new Integer[split.length - 1];
               for (int i = 1; i < (split.length); i++) {
                  if ("".equals(split[i])) {
                     throw new IllegalArgumentException("This encoded string ("+encodedAnswers+") is invalid, it must have integers in it, example: :0:3:4:");
                  }
                  decoded[i-1] = Integer.valueOf(split[i]).intValue();
               }
               Arrays.sort(decoded); // make sure it is sorted before returning the array
            }
         } else {
            throw new IllegalArgumentException("This encoded string ("+encodedAnswers+") is invalid, must adhere to the right format, example: :0:3:4:");
         }
      }
      if (decoded == null) {
         decoded = new Integer[0];
      }
      return decoded;
   }

}
