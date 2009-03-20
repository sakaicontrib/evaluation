/**
 * $Id$
 * $URL$
 * HierarchyUtils.java - evaluation - Mar 28, 2008 5:20:49 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.utils;

import java.text.DecimalFormat;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;


/**
 * A class to keep sharing rendering logic in
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RenderingUtils {

    /**
     * Calculates the weighted average and number of counted answers from the responseArray
     * (this comes from the {@link TemplateItemDataList#getAnswerChoicesCounts(String, int, List)}) <br/>
     * http://en.wikipedia.org/wiki/Weighted_mean
     * 
     * @param responseArray an array of answers in the order such that 0 weighted answers are in the first array slot, etc.)
     * @return the AnswersMean object which holds the answers count and the mean
     */
    public static AnswersMean calculateMean(int[] responseArray) {
        int responseCount = responseArray.length - 1; // remove the NA count from the end
        int totalAnswers = 0;
        int totalValue = 0;
        int totalWeight = 0;
        for (int i = 0; i < responseCount; i++) {
            int weight = i+1;
            totalWeight += weight;
            totalAnswers += responseArray[i];
            totalValue += (weight * responseArray[i]);
        }
        double weightedAverage = (double)totalValue / (double)totalAnswers; // (double)totalWeight;
        return new AnswersMean(totalAnswers, weightedAverage);
    }

    public static class AnswersMean {
        private DecimalFormat df = new DecimalFormat("#0.00");

        public String meanText;
        /**
         * @return the weighted mean as text
         */
        public String getMeanText() {
            return meanText;
        }
        public double mean;
        /**
         * @return the weighted mean
         */
        public double getMean() {
            return mean;
        }
        public int answersCount;
        /**
         * @return the number of answered items (not counting NA)
         */
        public int getAnswersCount() {
            return answersCount;
        }

        AnswersMean(int answers, double mean) {
            this.answersCount = answers;
            this.mean = mean;
            this.meanText = df.format(mean);
        }
    }

    /**
     * This will produce the valid message key given a category constant
     * @param categoryConstant
     * @return the message key
     */
    public static String getCategoryLabelKey(String categoryConstant) {
        String categoryMessage = "unknown.caps";
        if ( EvalConstants.ITEM_CATEGORY_COURSE.equals(categoryConstant) ) {
            categoryMessage = "modifyitem.course.category";
        } else if ( EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(categoryConstant) ) {
            categoryMessage = "modifyitem.instructor.category";
        } else if ( EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(categoryConstant) ) {
            categoryMessage = "modifyitem.ta.category";
        } else if ( EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(categoryConstant) ) {
            categoryMessage = "modifyitem.environment.category";
        }
        return categoryMessage;
    }

}
