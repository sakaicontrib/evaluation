/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.AdminSearchViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * AdministrateSearchProducer
 * 
 */
public class AdministrateSearchProducer implements ViewComponentProducer, ViewParamsReporter 
{
	public static int PAGE_SIZE = 20;

    /**
     * This is used for navigation within the system.
     */
    public static final String VIEW_ID = "administrate_search";
    public String getViewID() {
        return VIEW_ID;
    }

    // Spring injection 
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }
    
    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
        this.evalBeanUtils = evalBeanUtils;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
    {
    	String searchString = "";
    	int page = 0;
    	if(viewparams instanceof AdminSearchViewParameters)
    	{
    		AdminSearchViewParameters asvp = (AdminSearchViewParameters) viewparams;
    		searchString = asvp.searchString;
    		page = asvp.page;
    	}
    	
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        if (! userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

        UIMessage.make(tofill, "search-page-title", "administrate.search.page.title");

        // TOP LINKS
        UIInternalLink.make(tofill, "administrate-link",
                UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateSearchProducer.VIEW_ID));

        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"), 
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        }

        UIInternalLink.make(tofill, "control-templates-link",
                UIMessage.make("controltemplates.page.title"), 
                new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));

        if (!((Boolean)evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
            UIInternalLink.make(tofill, "control-items-link",
                    UIMessage.make("controlitems.page.title"),
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
        }

        UIInternalLink.make(tofill, "control-scales-link",
                UIMessage.make("controlscales.page.title"),
                new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

        //System Settings
        UIForm searchForm = UIForm.make(tofill, "search-form");
        UIInput searchInput = UIInput.make(searchForm, "search-input", 
        		"#{administrateSearchBean.searchString}", searchString);
        
        UICommand.make(searchForm, "search-text", 
        		UIMessage.make("administrate.search.submit.title"), 
        		"administrateSearchBean.processSearch");
        
        // this is search by title sorted in ascending order by title
        // other possible search fields or sorts: type, owner, termId, startDate, dueDate, stopDate, 
        // 		viewDate, studentViewResults, instructorViewResults, etc
        
        if(searchString != null && ! searchString.trim().equals(""));
        {
        	int startResult = page * PAGE_SIZE;
        	int maxResults = PAGE_SIZE;
           	String order = "title";

        	
        	int count = this.evaluationService.countEvaluations(searchString);
        	if(count > 0)
        	{
        		// do the search and get the results
        		List<EvalEvaluation> evals = this.evaluationService.getEvaluations(searchString, order, startResult, maxResults);
        		int actualStart = startResult + 1;
        		int actualEnd = startResult + evals.size();
        		if(count > PAGE_SIZE)
        		{
        			// show count and pager
            		// show x - y of z message
        			UIMessage.make(tofill, "pager-count-message", "administrate.search.pager.label", new String[]{ Integer.toString(actualStart), Integer.toString(actualEnd), Integer.toString(count) });
        			// show pager
               		if(page > 0)
            		{
            			// show "previous" pager
               			UIInternalLink.make(tofill, "previous", new AdminSearchViewParameters(VIEW_ID, searchString, page - 1));
            		}
            		else
            		{
            			// show disabled "previous" pager
            			UIOutput.make(tofill, "no-previous");
            		}
            		if(count > startResult + maxResults)
            		{
            			// show "next" pager
               			UIInternalLink.make(tofill, "next", new AdminSearchViewParameters(VIEW_ID, searchString, page + 1));
            		}
            		else
            		{
            			//show disabled "next" pager
            			UIOutput.make(tofill, "no-next");
            		}
        		}
         		
        		// show results
        		UIBranchContainer searchResults = UIBranchContainer.make(tofill, "searchResults:");
        		UIMessage.make(searchResults, "item-title-title", "administrate.search.list.title.title");
        		UIMessage.make(searchResults, "item-status-title", "administrate.search.list.status.title");
        		UIMessage.make(searchResults, "item-date-title", "administrate.search.list.date.title");
        		
        		for(EvalEvaluation eval : evals)
        		{
                    UIBranchContainer evalrow = UIBranchContainer.make(searchResults, "evalAdminList:", eval.getId().toString());

                    String evalState = evaluationService.returnAndFixEvalState(eval, true);
                    if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState)) {
                        // If we are in the queue we are yet to start,
                        // so say when we will
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.starts");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getStartDate()));

                        UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                    } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)) {
                        // Active evaluations can either be open forever or close at
                        // some point:
                        if (eval.getDueDate() != null) {
                            UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.due");
                            UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getDueDate()));
                            // Should probably add something here if there's a grace period
                        } else {
                            UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.nevercloses");
                        }

                        UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                    } else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
                        // Evaluations can have a grace period, if so that must
                        // close at some point;
                        // Grace periods never remain open forever
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.gracetill");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getSafeStopDate()));

                        UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                    } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
                        // if an evaluation is closed then it is not yet viewable
                        // and ViewDate must have been set
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.resultsviewableon");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getSafeViewDate()));

                        UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                    } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
                        // TODO if an evaluation is viewable we may want to notify
                        // if there are instructor/student dates
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.resultsviewablesince");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getSafeViewDate()));

                        int responsesCount = deliveryService.countResponses(eval.getId(), null, true);
                        int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), null);
                        int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                        if (responsesNeeded == 0) {
                            UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"), new ReportParameters(
                                    ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
                        } else {
                            UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState).decorate(
                                    new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded })));
                        }
                    } else {
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.fallback");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getStartDate()));
                    }

                    /*
                     * 1) if a evaluation is queued, title link go to EditSettings
                     * page with populated data 2) if a evaluation is active, title
                     * link go to EditSettings page with populated data but start
                     * date should be disabled 3) if a evaluation is closed, title
                     * link go to previewEval page with populated data
                     */
                    if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                        UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", EvalUtils.makeMaxLengthString(eval.getTitle(), 70), new EvalViewParameters(
                                PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
                    } else {
                        UIInternalLink.make(evalrow, "evalAdminTitleLink_edit", EvalUtils.makeMaxLengthString(eval.getTitle(), 70), new EvalViewParameters(
                                EvaluationSettingsProducer.VIEW_ID, eval.getId()));
                    }
        		}
        	}
        	else
        	{
        		// Show a message saying there are no results for that query
        		UIBranchContainer.make(tofill, "no-items:");
        	}
        }
        
    }
    
    public ViewParameters getViewParameters()
    {
    	return new AdminSearchViewParameters(VIEW_ID);
    }

}
