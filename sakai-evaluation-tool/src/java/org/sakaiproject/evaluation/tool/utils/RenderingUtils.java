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
package org.sakaiproject.evaluation.tool.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * A class to keep sharing rendering logic in
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public class RenderingUtils {

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

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
        //int totalWeight = 0;
        for (int i = 0; i < responseCount; i++) {
            int weight = i+1;
            //totalWeight += weight;
            totalAnswers += responseArray[i];
            totalValue += (weight * responseArray[i]);
        }
        double weightedAverage = 0.0d;
        if (totalAnswers > 0) {
            weightedAverage = (double)totalValue / (double)totalAnswers; // (double)totalWeight;
        }
        return new AnswersMean(totalAnswers, weightedAverage);
    }

    public static AnswersMean calculateAnswersMean(int[] responseArray, List<String> answersArray, boolean usaNA)
    {
    	/* 20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	 * Calculate weighted mean of answers or of answer numbers depending if values are numeric.
    	 * In all cases, N/A value is excluded.
    	 * responseArray: 3,7,2,9,5,3 (votes of each answer)
    	 * answersArray: 2,4,6,8,10 || A,B,C,D,E || 1,2,3,4,5,N/A  || A,B,C,D,E,N/A
    	 */
        if (responseArray == null) {
            throw new IllegalArgumentException("responseArray cannot be null");
        }
        int responseCount = responseArray.length - 1; // remove the NA count from the end
        int totalAnswers = 0;
        int totalAnswersWithNA;
        int totalValue = 0;
        
        int [] realValues = new int[responseCount];
        boolean numerico=true; //If there is a non-numeric value, mean of indexes is made.
        
        //We take all answers. If N/A is used, all but the last one.
        for (int i=0; i<responseCount;i++)
        {
        	try
        	{
	        	realValues[i]=new Integer(answersArray.get(i));
        	}
        	catch (Exception e)
        	{
        		numerico=false;
        		break;
        	}
        }
        
        for (int i = 0; i < responseCount; i++)
        {
            if (!numerico)
            {
            	//Not numeric values. Mean of answers indexes.
            	int weight = i+1;
            	totalAnswers += responseArray[i];
            	totalValue += (weight * responseArray[i]);
            }
            else
            {
            	//Numeric values. Mean of answers.
            	totalAnswers += responseArray[i];
            	totalValue += (realValues[i] * responseArray[i]);
            }
        }
        if (usaNA) totalAnswersWithNA = totalAnswers + responseArray[responseArray.length-1];
        else totalAnswersWithNA = totalAnswers;
        
        double weightedAverage = 0.0d;
        if (totalAnswers > 0) {
            weightedAverage = (double)totalValue / (double)totalAnswers;
        }
        return new AnswersMean(totalAnswersWithNA, weightedAverage);
    }

    public static class AnswersMean {
        private static final DecimalFormat DF = new DecimalFormat("#0.00");

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
            this.meanText = DF.format(mean);
        }

    }
    
    /**
     * getMatrixLabels() creates a list of either 2 or 3 labels that
     * will be displayed above the Matrix rendered scale.  By definition,
     * no scales will have 0 or 1 entries; there will always be at least 2.
     * The third entry will only be included if there are 5 or more
     * entries.  
     * <p>If the list contains a 3rd element, the 3rd element will be the middle
     * label.  We always know that the 1st element is the beginning and the 
     * second element is the end.
     * <p>2 entries in returns 2 entries (beginning and end)
     * <br>3 entries in returns 2 entries (beginning and end)
     * <br>4 entries in returns 2 entries (beginning and end)
     * <br>5 entries or more returns 3 entries (beginning, end, and middle)
     * <p>For scales with 5 or more entries, the middle entry of the scale will
     * be returned.  For lists with an even number of elements, the element before
     * the middle will be returned (i.e. a 6 element scale will return 1st, 3rd, and 6th)
     * 
     * @param scaleOptions the array of scale options for a matrix templateItem
     * @return List (see method comment)
     */
    public static List<String> getMatrixLabels(List<String> scaleOptions) {
        List<String> list = new ArrayList<>();
        if (scaleOptions != null && scaleOptions.size() > 0) {
            list.add(scaleOptions.get(0));
            list.add(scaleOptions.get(scaleOptions.size() - 1));
            if (scaleOptions.size() > 4) {
                int middleIndex = (scaleOptions.size() - 1) / 2;
                list.add(scaleOptions.get(middleIndex));
            }
        }
    	return list;
    }

    /**
     * Matrix display always shows the option's 1-based position as its column header
     * (e.g. "1", "2", "3"), regardless of the scale's actual option text. This is only
     * self-explanatory when the scale options are themselves numeric; for text scales
     * (e.g. "Strongly disagree" .. "Strongly agree") the position numbers need a legend
     * to be meaningful.
     *
     * @param scaleOptions the array of scale options for a matrix templateItem
     * @return false if every option parses as a number, true otherwise
     */
    public static boolean isNumericScale(List<String> scaleOptions) {
        if (scaleOptions == null || scaleOptions.isEmpty()) {
            return false;
        }
        for (String option : scaleOptions) {
            if (!org.apache.commons.lang3.math.NumberUtils.isParsable(option)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate the proper set of scale labels to use for a template item
     * in a report based on the item type (note, this will only return useful data for scale items)
     * 
     * @param templateItem any template item (should be fully populated)
     * @param scaleOptions the array of scale options for this templateItem
     * @return the array of scale labels (or null if this is not scaled/MC/MA/block child)
     */
    public static List<String> makeReportingScaleLabels(EvalTemplateItem templateItem, List<String> scaleOptions) {
        if (templateItem == null) {
            throw new IllegalArgumentException("templateItem must be set");
        }
        List<String> scaleLabels = new ArrayList<>();
        String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
        if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemType)
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType)
        ) {
            // default to scale options for MC and MA
            scaleLabels = scaleOptions;
        } else if (EvalConstants.ITEM_TYPE_SCALED.equals(itemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(itemType) // since BLOCK_CHILD is always a scaled item
        ) {
            // only do something here if this item type can handle a scale
            if (log.isDebugEnabled()) {
                log.debug("templateItem ("+templateItem.getId()+") scaled item rendering check: "+templateItem);
            }
            if (scaleOptions == null || scaleOptions.isEmpty()) {
                // if scale options are missing then try to get them from the item
                // NOTE: this could throw a NPE - not much we can do about that if it happens
                scaleOptions = templateItem.getItem().getScale().getOptions();
            }
            scaleLabels.addAll(scaleOptions); // default to just using the options array
            String scaleDisplaySetting = templateItem.getScaleDisplaySetting();
            if (scaleDisplaySetting == null && templateItem.getItem() != null) {
                scaleDisplaySetting = templateItem.getItem().getScaleDisplaySetting();
            }
            if (scaleDisplaySetting == null) {
                // this should not happen but just in case it does, we want to trap and warn about it
                log.warn("templateItem ("+templateItem.getId()+") without a scale display setting, using defaults for rendering: "+templateItem);
            } else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_MATRIX)
                    || scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED)
            ) {
                if (log.isDebugEnabled()) {
                    log.debug("templateItem ("+templateItem.getId()+") is a matrix type item: ");
                }
                /* MATRIX - special labels for the matrix items
                 * Show numbers in front (e.g. "blah" becomes "1 - blah")
                 * and only show text if the label was display in take evals (e.g. "1 - blah, 2, 3, 4 - blah, ...)
                 */
                List<String> matrixLabels = RenderingUtils.getMatrixLabels(scaleOptions);
                for (int i = 0; i < scaleLabels.size(); i++) {
                    String label = scaleLabels.get(i);
                    if (matrixLabels.contains(label)) {
                        scaleLabels.set(i, (i+1) + " - " + scaleLabels.get(i));
                    } else {
                        scaleLabels.set(i, String.valueOf(i+1));
                    }
                }
            }
        }
        return scaleLabels;
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
     * @param res the servlet response
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

    /**
     * Get a list of categories (a.k.a. associateTypes) that have items in this template. Categories are listed in {@link EvalConstants#ITEM_CATEGORY_ORDER}
     * and are like {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR}. {@link EvalConstants#ITEM_CATEGORY_COURSE} is always part of the returned list
     * @param templateId
     * @return
     */
    public List<String> extractCategoriesInTemplate(long templateId){
      List<String> categories = new ArrayList<>();
      //Fetch all templateItems to find out what categories we have
      List<EvalTemplateItem> templateItems = authoringService.getTemplateItemsForTemplate(templateId, new String[]{}, new String[]{}, new String[]{});
      // make the TI data structure
      Map<String, List<String>> assiciates = new HashMap<>();
      List<String> fakeInstructor = new ArrayList<>();
      fakeInstructor.add("fakeinstructor");
      List<String> fakeAssistant = new ArrayList<>();
      fakeAssistant.add("fakeAssistant");
      assiciates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, fakeInstructor);
      assiciates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, fakeAssistant);

      TemplateItemDataList tidl = new TemplateItemDataList(templateItems, null, assiciates, null);

      for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
        // check which category we have
        if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) ) {
          categories.add(EvalConstants.ITEM_CATEGORY_COURSE);
        } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
          categories.add(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
        } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
          categories.add(EvalConstants.ITEM_CATEGORY_ASSISTANT);
        }
      }

      return categories;

    }

}
