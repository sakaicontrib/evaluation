/******************************************************************************
 * ControlEvaluationsProducer.java - created by aaronz@vt.edu on Mar 19, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
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
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
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
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This lists evaluations for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEvaluationsProducer implements ViewComponentProducer {

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


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      String actionBean = "setupEvalBean.";
      boolean earlyCloseAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_EARLY_CLOSE);
      boolean reopeningAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);

      // use a date which is related to the current users locale
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

      // page title
      UIMessage.make(tofill, "page-title", "controlevaluations.page.title");

      // local variables used in the render logic
      String currentUserId = commonLogic.getCurrentUserId();

      /*
       * top links here
       */
      navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

      // get all the visible evaluations for the current user
      List<EvalEvaluation> partialEvals = new ArrayList<EvalEvaluation>();
      List<EvalEvaluation> inqueueEvals = new ArrayList<EvalEvaluation>();
      List<EvalEvaluation> activeEvals = new ArrayList<EvalEvaluation>();
      List<EvalEvaluation> closedEvals = new ArrayList<EvalEvaluation>();
      List<Long> takableEvaluationIds = new ArrayList<Long>(); // collect evaluation Ids for evaluations that are pending and active ONLY

      List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(commonLogic.getCurrentUserId(), false, false, true);
      for (int j = 0; j < evals.size(); j++) {
         // get queued, active, closed evaluations by date
         // check the state of the eval to determine display data
         EvalEvaluation eval = (EvalEvaluation) evals.get(j);
         String evalStatus = evaluationService.updateEvaluationState(eval.getId());

         if ( EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus) ) {
            inqueueEvals.add(eval);
            takableEvaluationIds.add(eval.getId());
         } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus) ||
               EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus) ) {
            closedEvals.add(eval);
         } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalStatus) ||
               EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalStatus) ) {
            activeEvals.add(eval);
            takableEvaluationIds.add(eval.getId());
         } else if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evalStatus) ) {
            partialEvals.add(eval);
         }
      }
      
      // get evalGroups for pending and active evals only. Later we will check if any of them are unpublished
      Map<Long, List<EvalAssignGroup>> takableAssignGroups = evaluationService.getAssignGroupsForEvals(takableEvaluationIds.toArray(new Long[0]), true, null);

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
            UIOutput.make(evaluationRow, "partial-eval-created", df.format(evaluation.getLastModified()));

            UIInternalLink.make(evaluationRow, "inqueue-eval-edit-link", UIMessage.make("controlevaluations.partial.continue"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            UIInternalLink.make(evaluationRow, "inqueue-eval-delete-link", UIMessage.make("general.command.delete"), 
                  new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
         }
      }
      
      int countUnpublishedGroups = 0;

      // create inqueue evaluations header
      if (inqueueEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "inqueue-eval-listing:");

         for (int i = 0; i < inqueueEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) inqueueEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "inqueue-eval-row:", evaluation.getId().toString());

            UIInternalLink evalTitleLink = UIInternalLink.make(evaluationRow, "inqueue-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId() ) );
            UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  commonLogic.getEntityURL(evaluation));
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }
            
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

            UIInternalLink.make(evaluationRow, "notifications-link", 
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "inqueue-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "inqueue-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            UIOutput.make(evaluationRow, "inqueue-eval-startdate", df.format(evaluation.getStartDate()));
            // TODO add support for evals that do not close - summary.label.nevercloses
            UIOutput.make(evaluationRow, "inqueue-eval-duedate", df.format(evaluation.getSafeDueDate()));

            UIInternalLink.make(evaluationRow, "inqueue-eval-edit-link", UIMessage.make("general.command.edit"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            // do the locked check first since it is more efficient
            if ( ! evaluation.getLocked().booleanValue() &&
                  evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "inqueue-eval-delete-link", UIMessage.make("general.command.delete"), 
                     new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

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
            UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  commonLogic.getEntityURL(evaluation));
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }
            

            UIInternalLink.make(evaluationRow, "notifications-link", 
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "active-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "active-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

            if (responsesNeeded == 0) {
                UIInternalLink.make(evaluationRow, "responders-link", 
                        UIMessage.make("controlevaluations.eval.responses.inline", new Object[] { responseString }),
                        new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, evaluation.getId() ) );
            } else {
                UIMessage.make(evaluationRow, "active-eval-response-rate", "controlevaluations.eval.responses.inline", 
                        new Object[] { responseString } );
            }

            UIOutput.make(evaluationRow, "active-eval-startdate", df.format(evaluation.getStartDate()));
            // TODO add support for evals that do not close - summary.label.nevercloses
            UIOutput.make(evaluationRow, "active-eval-duedate", df.format(evaluation.getSafeDueDate()));

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
         }
      } else {
         UIMessage.make(tofill, "no-active-evals", "controlevaluations.active.none");
      }
      
      if (countUnpublishedGroups > 0){
      	UIMessage.make(tofill, "eval-instructions-group-notpublished", "controlevaluations.instructions.site.unpublished");
      }

      // create closed evaluations header and link
      if (closedEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "closed-eval-listing:");

         for (int i = 0; i < closedEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) closedEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalListing, "closed-eval-row:", evaluation.getId().toString());

            UIInternalLink.make(evaluationRow, "closed-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId() ) );
            if (evaluation.getEvalCategory() != null) {
               UIOutput category = UIOutput.make(evaluationRow, "eval-category", shortenText(evaluation.getEvalCategory(), 20) );
               category.decorators = new DecoratorList( 
                     new UITooltipDecorator( evaluation.getEvalCategory() ) );
            }

            UIInternalLink.make(evaluationRow, "notifications-link", 
                    new EvalViewParameters( EvaluationNotificationsProducer.VIEW_ID, evaluation.getId() ) );

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId(), false);
            if (groupsCount == 1) {
               UIInternalLink.make(evaluationRow, "closed-eval-assigned-link", getTitleForFirstEvalGroup(evaluation.getId()), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            } else {
               UIInternalLink.make(evaluationRow, "closed-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }), 
                     new EvalViewParameters(EvaluationAssignmentsProducer.VIEW_ID, evaluation.getId()) );
            }

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

            if (responsesNeeded == 0) {
                UIInternalLink.make(evaluationRow, "responders-link", responseString, 
                        new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, evaluation.getId() ) );
            } else {
                UIOutput.make(evaluationRow, "closed-eval-response-rate", responseString );
            }

            UIOutput.make(evaluationRow, "closed-eval-duedate", df.format(evaluation.getDueDate()));

            if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(EvalUtils.getEvaluationState(evaluation, false)) ) {
               if ( responsesNeeded == 0 ) {
                  UIInternalLink.make(evaluationRow, "closed-eval-report-link", 
                        UIMessage.make("controlevaluations.eval.report.link"),
                        new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, evaluation.getId() ));  
               } else {
                  // cannot view yet, more responses needed
                  UIMessage.make(evaluationRow, "closed-eval-message", 
                        "controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded });
               }
            } else {
               UIMessage.make(evaluationRow, "closed-eval-message", 
                     "controlevaluations.eval.report.viewable.on",
                     new String[] { df.format(evaluation.getSafeViewDate()) });
            }

            UIInternalLink.make(evaluationRow, "closed-eval-edit-link", UIMessage.make("general.command.edit"),
                  new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId()) );

            if ( evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "closed-eval-delete-link", UIMessage.make("general.command.delete"), 
                     new EvalViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }
            
            if (reopeningAllowed) {
               // add in link to settings page with reopen option
               UIInternalLink.make(evaluationRow, "closed-eval-reopen-link", UIMessage.make("controlevaluations.closed.reopen.now"),
                     new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluation.getId(), true) );
            }

         }
      } else {
         UIMessage.make(tofill, "no-closed-evals", "controlevaluations.closed.none");
      }

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

}
