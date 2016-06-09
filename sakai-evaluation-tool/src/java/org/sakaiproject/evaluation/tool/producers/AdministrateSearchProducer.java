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
package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.AdminSearchViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.util.RSFUtil;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * AdministrateSearchProducer
 */
public class AdministrateSearchProducer extends EvalCommonProducer implements ViewParamsReporter {
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
	
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
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
		//DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		//NumberFormat nf = NumberFormat.getInstance(locale);
		NumberFormat nf = NumberFormat.getInstance();

		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}

		UIMessage.make(tofill, "search-page-title", "administrate.search.page.title");

		// TOP LINKS
		navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
		
		//System Settings
		UIForm searchForm = UIForm.make(tofill, "search-form");
		UIInput.make(searchForm, "search-input", 
				"#{administrateSearchBean.searchString}", searchString);
		RSFUtil.addResultingViewBinding(searchForm, "searchString", "#{administrateSearchBean.searchString}");
		UICommand.make(searchForm, "search-text", 
				UIMessage.make("administrate.search.submit.title"), 
				"administrateSearchBean.processSearch");
		searchForm.parameters.add(new UIELBinding("administrateSearchBean.page", 0));
		RSFUtil.addResultingViewBinding(searchForm, "page", "#{administrateSearchBean.page}");

		// this is search by title sorted in ascending order by title
		// other possible search fields or sorts: type, owner, termId, startDate, dueDate, stopDate, 
		// 		viewDate, studentViewResults, instructorViewResults, etc

		if(searchString != null && ! searchString.trim().equals(""))
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
					UIMessage.make(tofill, "pager-count-message", "administrate.search.pager.label", new String[]{ nf.format(actualStart), nf.format(actualEnd), nf.format(count) });
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
				UIMessage.make(searchResults, "item-group-id-title", "administrate.search.list.group.id.title");
				UIMessage.make(searchResults, "item-start-date-title", "administrate.search.list.start.date.title");
				UIMessage.make(searchResults, "item-due-date-title", "administrate.search.list.due.date.title");
				
				//loop through the evaluations and get whatever we need for use in retrieving users, groups etc.. later
				List<Long> evalIds = new ArrayList<>();  //keep the eval ids
				Map<String, EvalUser> evalOwners = new HashMap<>();  //keep the owner's Id and Sort name
				
				for(EvalEvaluation eval : evals){
					evalOwners.put(eval.getOwner(), null);
					evalIds.add(eval.getId());
				}
				
				//in one DB query, get the eval owners sort names
				List<EvalUser> evalOwnersFull = commonLogic.getEvalUsersByIds(evalOwners.keySet().toArray(new String[evalOwners.size()]));
				for(EvalUser evalUser : evalOwnersFull){
					evalOwners.put(evalUser.userId, evalUser);
				}
				
				//in one DB query, get the eval groups
				Map<Long, List<EvalAssignGroup>> evalAssignGroupsFull = this.evaluationService.getAssignGroupsForEvals(evalIds.toArray(new Long[evalIds.size()]), false, false);
				
				for(EvalEvaluation eval : evals)
				{
					UIBranchContainer evalrow = UIBranchContainer.make(searchResults,
							"evalAdminList:", eval.getId().toString() );
					
					UIOutput.make(evalrow, "evalAdminTitle", EvalUtils.makeMaxLengthString(eval.getTitle(), 70));

					UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", 
								UIMessage.make("administrate.search.list.preview"),
								new EvalViewParameters(PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
					EvalViewParameters newviewparams = new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, eval.getId());
					newviewparams.returnToSearchResults = true;
					newviewparams.adminSearchString = searchString;
					newviewparams.adminSearchPage = page;
					UIInternalLink.make(evalrow, "evalAdminTitleLink_edit", 
								UIMessage.make("administrate.search.list.revise"),
								newviewparams );
					
					List<EvalAssignGroup> list = evalAssignGroupsFull.get(eval.getId());
					// vary the display depending on the number of groups assigned
					int groupsCount = list.size();
					if (groupsCount == 1){
						UIInternalLink.make(evalrow, "evalAdminGroup", getTitleForFirstEvalGroup(list.get(0).getEvalGroupId()), 
			                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, eval.getId()) );
					}else{
						UIInternalLink.make(evalrow, "evalAdminGroup", 
			                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { groupsCount }), 
			                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, eval.getId()) );
					}
					
					UIOutput.make(evalrow, "evalAdminStartDate", df.format(eval.getStartDate()));
					UIOutput.make(evalrow, "evalAdminDueDate", df.format(eval.getDueDate()));
					
					String ownerOutput = "-------";
					EvalUser owner = evalOwners.get(eval.getOwner());
					if(owner != null){
						if (owner.username.equals(owner.sortName)){
							ownerOutput = owner.username;
						}else{
							ownerOutput = owner.sortName + " (" + owner.username+ ")";
						}
					}
					UIOutput.make(evalrow, "evalAdminOwner", ownerOutput);
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
	
	/**
    * Gets the title for the first returned evalGroupId for this evaluation,
    * should only be used when there is only one evalGroupId assigned to an eval
    * 
    * @param evaluationId
    * @return title of first evalGroupId returned
    */
   private String getTitleForFirstEvalGroup(String evalGroupId) {
      return commonLogic.getDisplayTitle( evalGroupId );
   }

}
