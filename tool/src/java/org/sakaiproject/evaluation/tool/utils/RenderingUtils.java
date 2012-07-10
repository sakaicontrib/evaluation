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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.producers.EvaluationRespondersProducer;
import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * A class to keep sharing rendering logic in
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RenderingUtils {

    private static Log log = LogFactory.getLog(RenderingUtils.class);

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
    public static List<String> getMatrixLabels(String[] scaleOptions) {
    	List<String> list = new ArrayList<String>();
        if (scaleOptions != null && scaleOptions.length > 0) {
        	list.add(scaleOptions[0]);
        	list.add(scaleOptions[scaleOptions.length - 1]);
        	if (scaleOptions.length > 4) {
        		int middleIndex = (scaleOptions.length - 1) / 2;
        		list.add(scaleOptions[middleIndex]);
        	}
        }
    	return list;
    }

    /**
     * Calculate the proper set of scale labels to use for a template item
     * in a report based on the item type (note, this will only return useful data for scale items)
     * 
     * @param templateItem any template item (should be fully populated)
     * @param scaleOptions the array of scale options for this templateItem
     * @return the array of scale labels (or null if this is not scaled/MC/MA/block child)
     */
    public static String[] makeReportingScaleLabels(EvalTemplateItem templateItem, String[] scaleOptions) {
        if (templateItem == null) {
            throw new IllegalArgumentException("templateItem must be set");
        }
        String scaleLabels[] = null;
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
            if (log.isDebugEnabled()) log.debug("templateItem ("+templateItem.getId()+") scaled item rendering check: "+templateItem);
            if (scaleOptions == null || scaleOptions.length == 0) {
                // if scale options are missing then try to get them from the item
                // NOTE: this could throw a NPE - not much we can do about that if it happens
                scaleOptions = templateItem.getItem().getScale().getOptions();
            }
            scaleLabels = scaleOptions.clone(); // default to just using the options array (use a copy)
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
                if (log.isDebugEnabled()) log.debug("templateItem ("+templateItem.getId()+") is a matrix type item: ");
                /* MATRIX - special labels for the matrix items
                 * Show numbers in front (e.g. "blah" becomes "1 - blah")
                 * and only show text if the label was display in take evals (e.g. "1 - blah, 2, 3, 4 - blah, ...)
                 */
                List<String> matrixLabels = RenderingUtils.getMatrixLabels(scaleOptions);
                for (int i = 0; i < scaleLabels.length; i++) {
                    String label = scaleLabels[i];
                    if (matrixLabels.contains(label)) {
                        scaleLabels[i] = (i+1) + " - " + scaleLabels[i];
                    } else {
                        scaleLabels[i] = String.valueOf(i+1);
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


    /**
     * Renders the reports/results column content (since the logic is complex)
     * 
     * @param evalrow the branch container (must contain the following elements):
     *      evalReportDisplay (output)
     *      evalReportDisplayLink (link)
     *      evalRespondentsDisplayLink (link)
     * @param eval the evaluation
     * @param group the eval group
     * @param viewDate the date at which results can be viewed
     * @param df the formatter for the dates
     * @param responsesNeeded responses needed before results can be viewed (0 indicates they can be viewed now)
     * @param showResultsDetails if true then display the results details info instead of the link,
     *      normally this is used when the user is not allowed to view the report yet (not enough responses for example),
     *      if this is rendering for instructors then this should ONLY be true when 
     *      EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS or EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESPONDERS are true
     * @param evalResultsViewable true if the reports can be viewed based on eval state, prefs, and dates, 
     *      usually the result of EvalBeanUtils.checkInstructorViewResultsForEval(),
     *      NOTE: this doesn't guarantee the link is visible as there might not be enough respondents 
     *      or the view date may not be reached yet
     * 
     * Sample rendering html (from summary.html):
      <tr rsf:id="evalResponsesList:">
        ...
        <td nowrap="nowrap">
          <span rsf:id="evalReportDisplay"></span>
          <a rsf:id="evalReportDisplayLink" href="report_view.html">results</a>
          <a rsf:id="evalRespondentsDisplayLink" class="left-separator" href="evaluation_responders.html">respondents</a>
        </td>
      </tr>
     *
     */
    public static void renderResultsColumn(UIBranchContainer evalrow, EvalEvaluation eval, EvalGroup group, 
            Date viewDate, DateFormat df, int responsesNeeded, boolean showResultsDetails, boolean evalResultsViewable) {
        /* the rules here are a little complex but.... the design is as follows:
         * if instructor can email students OR view responders then we show 2 links
         *      the reports link is disabled according to the rules for viewing reports and tooltip show the date or responses required
         *      the responses link is always enabled as there are no limits on when it can be used
         * ELSE only show the reports info BUT instead of only showing the link, 
         *      show either the date at which the report is viewable or responses required directly
         *      and only show the report link when it works
         */
        if (viewDate == null) {
            viewDate = eval.getSafeViewDate(); // ensure no NPE
        }
        String viewableDate = df.format(viewDate);
        if (evalResultsViewable && responsesNeeded <= 0) {
            // reports are viewable so just display the reports link
            ViewParameters viewparams;
            if (group != null) {
                viewparams = new ReportParameters(ReportsViewingProducer.VIEW_ID, eval.getId(), new String[] { group.evalGroupId });
            } else {
                viewparams = new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, eval.getId());
            }
            UIInternalLink.make(evalrow, "evalReportDisplayLink", UIMessage.make("summary.responses.report.link"), viewparams);
        } else if ( responsesNeeded > 0 ) {
            // not viewable yet because there are not enough responses
            if (showResultsDetails) {
                // we only show detailed results if the responders link is not visible
                UIMessage resultOutput = UIMessage.make(evalrow, "evalReportDisplay", "controlevaluations.eval.report.awaiting.responses", 
                        new Object[] { responsesNeeded });
                // indicate the viewable date as well
                resultOutput.decorate(new UITooltipDecorator(
                        UIMessage.make("controlevaluations.eval.report.viewable.on", new Object[] { viewableDate }) ));
            } else {
                // only show the disabled link
                UIMessage resultOutput = UIMessage.make(evalrow, "evalReportDisplay", "summary.responses.report.link");
                // indicate the responses needed
                resultOutput.decorate(new UITooltipDecorator(
                        UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded }) ));
            }
        } else {
            // not viewable yet because of the view date
            if (showResultsDetails) {
                // we only show detailed results if the responders link is not visible
                UIOutput resultOutput = UIOutput.make(evalrow, "evalReportDisplay", viewableDate );
                if ( responsesNeeded == 0 ) {
                    // just show date if we have enough responses
                    resultOutput.decorate(new UITooltipDecorator(
                            UIMessage.make("controlevaluations.eval.report.viewable.on", new Object[] { viewableDate }) ));
                } else {
                    // show if responses are still needed
                    resultOutput.decorate(new UITooltipDecorator( 
                            UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded }) ));
                }
            } else {
                // only show the disabled link
                UIMessage resultOutput = UIMessage.make(evalrow, "evalReportDisplay", "summary.responses.report.link");
                // show the date as a tooltip
                resultOutput.decorate(new UITooltipDecorator(
                        UIMessage.make("controlevaluations.eval.report.viewable.on", new Object[] { viewableDate }) ));
            }
        }
        if (!showResultsDetails) {
            // also show the respondents link if we are not showing the details
            ViewParameters viewparams;
            if (group != null) {
                viewparams = new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, eval.getId(), group.evalGroupId );
            } else {
                viewparams = new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, eval.getId());
            }
            UIInternalLink.make(evalrow, "evalRespondentsDisplayLink", UIMessage.make("summary.responses.respondents.link"), viewparams);
        }
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
