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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.HumanDateRenderer;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalListParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This lists evaluations for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEvaluationsProducer extends EvalCommonProducer implements ViewParamsReporter {

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
    */
   public static String VIEW_ID = "control_evaluations";
   public String getViewID() {
      return VIEW_ID;
   }

   private Locale locale;
   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   private EvalBeanUtils evalBeanUtils;
   public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
      this.evalBeanUtils = evalBeanUtils;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }
   
   private NavBarRenderer navBarRenderer;
   public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
   
   private HumanDateRenderer humanDateRenderer;
   public void setHumanDateRenderer(HumanDateRenderer humanDateRenderer) {
       this.humanDateRenderer = humanDateRenderer;
   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      EvalListParameters evalListParams = (EvalListParameters) viewparams;
      int maxAgeToDisplay = evalListParams.maxAgeToDisplay; // max age in months to display closed evals
      
      
      // use a date which is related to the current users locale
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

      // page title
      UIMessage.make(tofill, "page-title", "controlevaluations.page.title");

      // local variables used in the render logic
      String actionBean = "setupEvalBean.";
      boolean earlyCloseAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_EARLY_CLOSE);
      boolean reopeningAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);
      boolean viewResultsIgnoreDates = (Boolean) settings.get(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES);
      int responsesRequired = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS));

      String currentUserId = commonLogic.getCurrentUserId();

      /*
       * top links here
       */
      navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

      // get all the visible evaluations for the current user
      List<EvalEvaluation> partialEvals = new ArrayList<>();
      List<EvalEvaluation> inqueueEvals = new ArrayList<>();
      List<EvalEvaluation> activeEvals = new ArrayList<>();
      List<EvalEvaluation> closedEvals = new ArrayList<>();

      // UM specific code for checking unpublished groups - collect evaluation Ids for evaluations that are pending and active ONLY
      List<Long> takableEvaluationIds = new ArrayList<>();

      List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(commonLogic.getCurrentUserId(), false, false, true, maxAgeToDisplay);
      for (int j = 0; j < evals.size(); j++) {
         // get queued, active, closed evaluations by date
         // check the state of the eval to determine display data
         EvalEvaluation eval = (EvalEvaluation) evals.get(j);
         String evalStatus = evaluationService.updateEvaluationState(eval.getId());

         if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evalStatus) ) {
             partialEvals.add(eval);
         } else if ( EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus) ) {
             inqueueEvals.add(eval);
             takableEvaluationIds.add(eval.getId());
         } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalStatus) ||
                 EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalStatus) ) {
             activeEvals.add(eval);
             takableEvaluationIds.add(eval.getId());
         } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus) ||
                 EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus) ) {
             closedEvals.add(eval);
         }
      }

      // UM specific code for checking unpublished groups
      int countUnpublishedGroups = 0;
      Map<Long, List<EvalAssignGroup>> takableAssignGroups = new HashMap<>();
      if ((Boolean) settings.get(EvalSettings.ENABLE_SITE_GROUP_PUBLISH_CHECK)) {
          // get evalGroups for pending and active evals only. Later we will check if any of them are unpublished
          takableAssignGroups = evaluationService.getAssignGroupsForEvals(takableEvaluationIds.toArray(new Long[takableEvaluationIds.size()]), true, null);
      }

      // create start new eval link
      UIInternalLink.make(tofill, "begin-evaluation-link", UIMessage.make("starteval.page.title"), 
            new EvalViewParameters(EvaluationCreateProducer.VIEW_ID, null) );


      // create partial evaluations header and listing
      if (partialEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "partial-eval-listing:");
         if (EvalConstants.EVALUATION_PARTIAL_CLEANUP_DAYS > 0) {
             UIMessage.make(evalListing, "partial-cleanup-note", "controlevaluations.partial.cleanup", 
                     new Object[] {EvalConstants.EVALUATION_PARTIAL_CLEANUP_DAYS});
         }

         for (int i = 0; i < partialEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) partialEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "partial-eval-row:", evaluation.getId().toString());

            UIOutput.make(evaluationRow, "partial-eval-title", evaluation.getTitle() );

            UIInternalLink.make(evaluationRow, "partial-eval-edit-link", UIMessage.make("controlevaluations.partial.continue"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );
            UIInternalLink.make(evaluationRow, "partial-eval-delete-link", UIMessage.make("general.command.delete"), 
                  new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );

            humanDateRenderer.renderDate(evaluationRow, "partial-eval-created", evaluation.getLastModified());
         }
      }


      // create inqueue evaluations header
      if (inqueueEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "inqueue-eval-listing:");

         for (int i = 0; i < inqueueEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) inqueueEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "inqueue-eval-row:", evaluation.getId().toString());

            UIInternalLink evalTitleLink = UIInternalLink.make(evaluationRow, "inqueue-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId() ) );
            evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.title.tooltip")) );

            if ((Boolean) settings.get(EvalSettings.ENABLE_SITE_GROUP_PUBLISH_CHECK)) {
                // NOTE: UM specific code
                // check if this eval has any site/group that is unpublished
                List<EvalAssignGroup> assignGroups = takableAssignGroups.get(evaluation.getId());
                int countUnpublished = 0;
                for (EvalAssignGroup group : assignGroups){
                    if (! commonLogic.isEvalGroupPublished(group.getEvalGroupId())){
                        countUnpublished ++;
                    }
                }
                if (countUnpublished > 0){
                    evalTitleLink.decorate( new UIStyleDecorator("elementAlertFront") );
                    evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.instructions.site.unpublished")) );
                    countUnpublishedGroups ++;
                }
            }

            UILink directLink = UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  commonLogic.getEntityURL(evaluation));
            directLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.direct.tooltip")) );
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }

            UIInternalLink.make(evaluationRow, "inqueue-eval-edit-link", UIMessage.make("general.command.edit"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            // do the locked check first since it is more efficient
            if ( ! evaluation.getLocked() &&
                  evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "inqueue-eval-delete-link", UIMessage.make("general.command.delete"), 
                     new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

            UIInternalLink.make(evaluationRow, "inqueue-eval-notifications-link", UIMessage.make("controlevaluations.eval.email.link"), 
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "inqueue-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "inqueue-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { groupsCount}), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            humanDateRenderer.renderDate(evaluationRow, "inqueue-eval-startdate", evaluation.getStartDate());

            // TODO add support for evals that do not close - summary.label.nevercloses
            humanDateRenderer.renderDate(evaluationRow, "inqueue-eval-duedate", evaluation.getSafeDueDate());

         }
      } else {
         UIMessage.make(tofill, "no-inqueue-evals", "controlevaluations.inqueue.none");
      }


      // create active evaluations header and link
      if (activeEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "active-eval-listing:");
         for (int i = 0; i < activeEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) activeEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "active-eval-row:", evaluation.getId().toString());

            UIInternalLink evalTitleLink = UIInternalLink.make(evaluationRow, "active-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(),	evaluation.getTemplate().getId() ) );
            evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.title.tooltip")) );

            UILink directLink = UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  commonLogic.getEntityURL(evaluation));
            directLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.direct.tooltip")) );
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }

            UIInternalLink.make(evaluationRow, "active-eval-notifications-link", UIMessage.make("controlevaluations.eval.email.link"),
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            UIInternalLink.make(evaluationRow, "active-eval-edit-link", UIMessage.make("general.command.edit"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            if ( evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "active-eval-delete-link", UIMessage.make("general.command.delete"), 
                     new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

            if (earlyCloseAllowed) {
               UIForm form = UIForm.make(evaluationRow, "evalCloseForm");
               form.addParameter( new UIELBinding(actionBean + "evaluationId", evaluation.getId()) );
               UICommand.make(form, "evalCloseCommand", UIMessage.make("controlevaluations.active.close.now"), 
                     actionBean + "closeEvalAction");
            }

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "active-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "active-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { groupsCount}), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            humanDateRenderer.renderDate(evaluationRow, "active-eval-startdate", evaluation.getStartDate());
            // TODO add support for evals that do not close - summary.label.nevercloses
            humanDateRenderer.renderDate(evaluationRow, "active-eval-duedate", evaluation.getSafeDueDate());

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

            RenderingUtils.renderReponseRateColumn(evaluationRow, evaluation.getId(), responsesNeeded, 
                    responseString, true, true);

            // owner can view the results but only early IF the setting is enabled
            boolean viewResultsEval = viewResultsIgnoreDates;
            // now render the results links depending on what the user is allowed to see
            RenderingUtils.renderResultsColumn(evaluationRow, evaluation, null, evaluation.getSafeViewDate(), df, 
                    responsesNeeded, responsesRequired, viewResultsEval);
         }
      } else {
         UIMessage.make(tofill, "no-active-evals", "controlevaluations.active.none");
      }

      if (countUnpublishedGroups > 0) {
          UIMessage.make(tofill, "eval-instructions-group-notpublished", "controlevaluations.instructions.site.unpublished");
      }


      // create closed evaluations header and link
      if (closedEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "closed-eval-listing:");

         for (int i = 0; i < closedEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) closedEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "closed-eval-row:", evaluation.getId().toString());

            UIInternalLink evalTitleLink = UIInternalLink.make(evaluationRow, "closed-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId() ) );
            evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.title.tooltip")) );
            if (evaluation.getEvalCategory() != null) {
               UIOutput category = UIOutput.make(evaluationRow, "eval-category", shortenText(evaluation.getEvalCategory(), 20) );
               category.decorators = new DecoratorList( new UITooltipDecorator( evaluation.getEvalCategory() ) );
            }

            UIInternalLink.make(evaluationRow, "closed-eval-edit-link", UIMessage.make("general.command.edit"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            if ( evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "closed-eval-delete-link", UIMessage.make("general.command.delete"), 
                     new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

            UIInternalLink.make(evaluationRow, "closed-eval-notifications-link", UIMessage.make("controlevaluations.eval.email.link"),
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            if (reopeningAllowed) {
               // add in link to settings page with reopen option
               UIInternalLink.make(evaluationRow, "closed-eval-reopen-link", UIMessage.make("controlevaluations.closed.reopen.now"),
                     new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId(), true) );
            }

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "closed-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "closed-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { groupsCount}), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            humanDateRenderer.renderDate(evaluationRow, "closed-eval-startdate", evaluation.getStartDate());

            humanDateRenderer.renderDate(evaluationRow, "closed-eval-duedate", evaluation.getSafeDueDate());

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);
            String evalState = EvalUtils.getEvaluationState(evaluation, false);

            RenderingUtils.renderReponseRateColumn(evaluationRow, evaluation.getId(), responsesNeeded, 
                    responseString, true, true);

            // owner can view the results but only early IF the setting is enabled
            boolean viewResultsEval = viewResultsIgnoreDates ? true : EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true);
            // now render the results links depending on what the user is allowed to see
            RenderingUtils.renderResultsColumn(evaluationRow, evaluation, null, evaluation.getSafeViewDate(), df, 
                    responsesNeeded, responsesRequired, viewResultsEval);
         }
         
      } else {
         UIMessage.make(tofill, "no-closed-evals", "controlevaluations.closed.none");
      }
      
      // Links for limiting amount of closed evals
      createClosedEvalDisplayLinks(tofill, maxAgeToDisplay);
   }

   /**
    * Gets the title for the first returned evalGroupId for this evaluation,
    * should only be used when there is only one evalGroupId assigned to an eval
    * 
    * @param evaluationId
    * @return title of first evalGroupId returned
    */
   private String getTitleForFirstEvalGroup(Long evaluationId) {
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
      List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
      EvalAssignGroup eac = (EvalAssignGroup) groups.get(0);
      return commonLogic.getDisplayTitle( eac.getEvalGroupId() );
   }

   /**
    * Shorten a string to be no longer than the length supplied (uses ...)
    * @param text
    * @param length
    * @return shorted text with ... or original string
    */
   private String shortenText(String text, int length) {
      if (text.length() > length) {
         text = text.substring(0, length-3) + "...";
      }
      return text;
   }
   
   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
       return new EvalListParameters();
   }

   /*
    * Arrange the links to show the options that are not currently being shown. So if we're 
    * looking at 12 months of closed evals right now, we don't need a link to look at 12 months.
    * 
    * UIContainer tofill
    * int maxAgeToDisplay: int in months to display the older evals. 0 means don't limit and 
    * 		display all.
    * 
    */
   private void createClosedEvalDisplayLinks(UIContainer tofill, int maxAgeToDisplay) {
	  UIMessage.make("controlevaluations.closed.displaying");
	  String monthText = Integer.toString(maxAgeToDisplay);
	  if (maxAgeToDisplay == 0) monthText = "All";
	  UIOutput.make(tofill, "eval-months-displayed", monthText);
	  UIMessage.make("controlevaluations.closed.months");
	  
	  switch(maxAgeToDisplay) {
	  	case 6: 
		  	  UIInternalLink.make(tofill, "x-month-evals-link", UIMessage.make("controlevaluations.closed.twelve"), 
			          new EvalListParameters(VIEW_ID, 12) );
			  UIInternalLink.make(tofill, "y-month-evals-link", UIMessage.make("controlevaluations.closed.eighteen"), 
			          new EvalListParameters(VIEW_ID, 18) );
			  UIInternalLink.make(tofill, "z-month-evals-link", UIMessage.make("controlevaluations.closed.all"), 
			          new EvalListParameters(VIEW_ID, 0) ); // zero means no limit
		  break;
	  	case 12: 
		  	  UIInternalLink.make(tofill, "x-month-evals-link", UIMessage.make("controlevaluations.closed.six"), 
			          new EvalListParameters(VIEW_ID, 6) );
			  UIInternalLink.make(tofill, "y-month-evals-link", UIMessage.make("controlevaluations.closed.eighteen"), 
			          new EvalListParameters(VIEW_ID, 18) );
			  UIInternalLink.make(tofill, "z-month-evals-link", UIMessage.make("controlevaluations.closed.all"), 
			          new EvalListParameters(VIEW_ID, 0) );
			  break;
	  	case 18: 
		  	  UIInternalLink.make(tofill, "x-month-evals-link", UIMessage.make("controlevaluations.closed.six"), 
			          new EvalListParameters(VIEW_ID, 6) );
			  UIInternalLink.make(tofill, "y-month-evals-link", UIMessage.make("controlevaluations.closed.twelve"), 
			          new EvalListParameters(VIEW_ID, 12) );
			  UIInternalLink.make(tofill, "z-month-evals-link", UIMessage.make("controlevaluations.closed.all"), 
			          new EvalListParameters(VIEW_ID, 0) );
			  break;
	  	default: // all 
		  	  UIInternalLink.make(tofill, "x-month-evals-link", UIMessage.make("controlevaluations.closed.six"), 
			          new EvalListParameters(VIEW_ID, 6) );
			  UIInternalLink.make(tofill, "y-month-evals-link", UIMessage.make("controlevaluations.closed.twelve"), 
			          new EvalListParameters(VIEW_ID, 12) );
			  UIInternalLink.make(tofill, "z-month-evals-link", UIMessage.make("controlevaluations.closed.eighteen"), 
			          new EvalListParameters(VIEW_ID, 18) );
			  break;
	  		
	  } // end switch
   }
   
}
