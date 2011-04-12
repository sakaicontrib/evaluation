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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;


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
        if (responseArray == null) {
            throw new IllegalArgumentException("responseArray cannot be null");
        }
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
        double weightedAverage = 0.0d;
        if (totalAnswers > 0) {
            weightedAverage = (double)totalValue / (double)totalAnswers; // (double)totalWeight;
        }
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

    /**
     * Ensures all rendering calcs work the same way and generate the same general properties
     * 
     * @param dti the dti which holds the template item (wrap the template item if you need to)
     * @param eval (OPTIONAL) the evaluation related to the template item to be rendered
     * @param missingKeys (OPTIONAL) the set of missing keys which should cause the matching items to rendered with an invalid marker
     * @param renderProperties (OPTIONAL) the existing map of rendering properties (one is created if this is null)
     * @return the map of render properties (created if the input map is null)
     */
    public static Map<String,Object> makeRenderProps(DataTemplateItem dti, 
            EvalEvaluation eval, Set<String> missingKeys, 
            Map<String,Object> renderProperties) {
        if (dti == null || dti.templateItem == null) {
            throw new IllegalArgumentException("dti and dti.templateItem cannot be null");
        }

        if (renderProperties == null) {
            renderProperties = new HashMap<String, Object>();
        }

        boolean evalRequiresItems = false;
        if (eval != null) {
            evalRequiresItems = ! EvalUtils.safeBool(eval.getBlankResponsesAllowed(), true);
        }
        if ( dti.isCompulsory() || (evalRequiresItems && dti.isRequireable()) ) {
            renderProperties.put(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED, Boolean.TRUE);
        } else {
            renderProperties.remove(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED);
        }

        if (missingKeys != null && ! missingKeys.isEmpty()) {
            if (missingKeys.contains(dti.getKey())) {
                renderProperties.put(ItemRenderer.EVAL_PROP_RENDER_INVALID, Boolean.TRUE);
            }
        } else {
            renderProperties.remove(ItemRenderer.EVAL_PROP_RENDER_INVALID);
        }

        // loop through and add in children render props
        if (dti.isBlockParent()) {
            List<DataTemplateItem> children = dti.getBlockChildren();
            for (DataTemplateItem childDTI : children) {
                HashMap<String,Object> childRenderProps = new HashMap<String, Object>();
                RenderingUtils.makeRenderProps(childDTI, eval, missingKeys, childRenderProps);
                String key = "child-"+childDTI.templateItem.getId();
                renderProperties.put(key, childRenderProps);
            }
        }

        return renderProperties;
    }

    // NOTE: caching stuff copied from EntityBus project

    public static enum Header {
        EXPIRES ("Expires"),
        DATE ("Date"),
        ETAG ("ETag"),
        LAST_MODIFIED ("Last-Modified"),
        CACHE_CONTROL ("Cache-Control");

        private String value;
        Header(String value) { this.value = value; }
        @Override
        public String toString() {
            return value;
        }
    };

    /**
     * Set the no-cache headers for this response
     * @param httpServletResponse the servlet response
     */
    public static void setNoCacheHeaders(HttpServletResponse res) {
        long currentTime = System.currentTimeMillis();
        res.setDateHeader(Header.DATE.toString(), currentTime);
        res.setDateHeader(Header.EXPIRES.toString(), currentTime + 1000);

        res.setHeader(Header.CACHE_CONTROL.toString(), "no-cache");
        res.addHeader(Header.CACHE_CONTROL.toString(), "no-store");
        res.addHeader(Header.CACHE_CONTROL.toString(), "max-age=0");
        res.addHeader(Header.CACHE_CONTROL.toString(), "must-revalidate");
        res.addHeader(Header.CACHE_CONTROL.toString(), "private");
        res.addHeader(Header.CACHE_CONTROL.toString(), "s-maxage=0");
    }

}
