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
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
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
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This lists evaluations for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEvaluationsProducer implements ViewComponentProducer, NavigationCaseReporter {

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

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
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


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // use a date which is related to the current users locale
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

      // page title
      UIMessage.make(tofill, "page-title", "controlevaluations.page.title");

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
      boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
      boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      if (userAdmin) {
         UIInternalLink.make(tofill, "administrate-link", 
               UIMessage.make("administrate.page.title"),
               new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-scales-link",
               UIMessage.make("controlscales.page.title"),
               new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
      }

      if (createTemplate) {
         UIInternalLink.make(tofill, "control-templates-link",
               UIMessage.make("controltemplates.page.title"), 
               new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controlitems.page.title"), 
               new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
      }

      if (beginEvaluation) {
         UIInternalLink.make(tofill, "control-evaluations-link",
               UIMessage.make("controlevaluations.page.title"),
            new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
      } else {
         throw new SecurityException("User attempted to access " + 
               VIEW_ID + " when they are not allowed");
      }

      // get all the visible evaluations for the current user
      List<EvalEvaluation> inqueueEvals = new ArrayList<EvalEvaluation>();
      List<EvalEvaluation> activeEvals = new ArrayList<EvalEvaluation>();
      List<EvalEvaluation> closedEvals = new ArrayList<EvalEvaluation>();

      List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(externalLogic.getCurrentUserId(), false, false);
      for (int j = 0; j < evals.size(); j++) {
         // get queued, active, closed evaluations by date
         // check the state of the eval to determine display data
         EvalEvaluation eval = (EvalEvaluation) evals.get(j);
         String evalStatus = evaluationService.updateEvaluationState(eval.getId());

         if ( EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus) ) {
            inqueueEvals.add(eval);
         } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus) ||
               EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus) ) {
            closedEvals.add(eval);
         } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalStatus) ||
               EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalStatus) ) {
            activeEvals.add(eval);
         }
      }

      // create inqueue evaluations header and link
      UIForm startEvalForm = UIForm.make(tofill, "begin-evaluation-form");
      UICommand.make(startEvalForm, "begin-evaluation-link", UIMessage.make("starteval.page.title"), "#{evaluationBean.startEvaluation}");

      if (inqueueEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "inqueue-eval-listing:");
         UIForm evalForm = UIForm.make(evalListing, "inqueue-eval-form");

         for (int i = 0; i < inqueueEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) inqueueEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "inqueue-eval-row:", evaluation.getId().toString());

            UIMessage.make(evalForm, "eval-preview-title", "controlevaluations.eval.preview.title");
            UIMessage.make(evalForm, "eval-link-title", "controlevaluations.eval.link.title");

            UIInternalLink.make(evaluationRow, "inqueue-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(),	evaluation.getTemplate().getId() ) );
            UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  externalLogic.getEntityURL(evaluation));
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     externalLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId());
            if (groupsCount == 1) {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "inqueue-eval-assigned-link", 
                     getTitleForFirstEvalGroup(evaluation.getId()),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            } else {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "inqueue-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            }

            UIOutput.make(evaluationRow, "inqueue-eval-startdate", df.format(evaluation.getStartDate()));
            UIOutput.make(evaluationRow, "inqueue-eval-duedate", df.format(evaluation.getDueDate()));

            UICommand evalEdit = UICommand.make(evaluationRow, "inqueue-eval-edit-link", 
                  UIMessage.make("general.command.edit"), "#{evaluationBean.editEvalSettingAction}");
            evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

            // do the locked check first since it is more efficient
            if ( ! evaluation.getLocked().booleanValue() &&
                  evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "inqueue-eval-delete-link", 
                     UIMessage.make("general.command.delete"), 
                     new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

         }
      } else {
         UIMessage.make(tofill, "no-inqueue-evals", "controlevaluations.inqueue.none");
      }


      // create active evaluations header and link
      if (activeEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "active-eval-listing:");
         UIForm evalForm = UIForm.make(evalListing, "active-eval-form");

         for (int i = 0; i < activeEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) activeEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "active-eval-row:", evaluation.getId().toString());

            UIMessage.make(evalForm, "eval-preview-title", "controlevaluations.eval.preview.title");
            UIMessage.make(evalForm, "eval-link-title", "controlevaluations.eval.link.title");

            UIInternalLink.make(evaluationRow, "active-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(),	evaluation.getTemplate().getId() ) );
            UILink.make(evaluationRow, "eval-direct-link", UIMessage.make("controlevaluations.eval.direct.link"), 
                  externalLogic.getEntityURL(evaluation));
            if (evaluation.getEvalCategory() != null) {
               UILink catLink = UILink.make(evaluationRow, "eval-category-direct-link", shortenText(evaluation.getEvalCategory(), 20), 
                     externalLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) );
               catLink.decorators = new DecoratorList( 
                     new UITooltipDecorator( UIMessage.make("general.category.link.tip", new Object[]{evaluation.getEvalCategory()}) ) );
            }

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId());
            if (groupsCount == 1) {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "active-eval-assigned-link", 
                     getTitleForFirstEvalGroup(evaluation.getId()),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            } else {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "active-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            }

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId());
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);
            UIMessage.make(evaluationRow, "active-eval-response-rate", "controlevaluations.eval.responses.inline", 
                  new Object[] { responseString } );

            UIOutput.make(evaluationRow, "active-eval-startdate", df.format(evaluation.getStartDate()));
            UIOutput.make(evaluationRow, "active-eval-duedate", df.format(evaluation.getDueDate()));

            UICommand evalEdit = UICommand.make(evaluationRow, 
                  "active-eval-edit-link", 
                  UIMessage.make("general.command.edit"),
            "#{evaluationBean.editEvalSettingAction}");
            evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

            if ( evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "active-eval-delete-link", 
                     UIMessage.make("general.command.delete"), 
                     new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

         }
      } else {
         UIMessage.make(tofill, "no-active-evals", "controlevaluations.active.none");
      }

      // create closed evaluations header and link
      if (closedEvals.size() > 0) {
         UIBranchContainer evalListing = UIBranchContainer.make(tofill, "closed-eval-listing:");
         UIForm evalForm = UIForm.make(evalListing, "closed-eval-form");

         for (int i = 0; i < closedEvals.size(); i++) {
            EvalEvaluation evaluation = (EvalEvaluation) closedEvals.get(i);

            UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "closed-eval-row:", evaluation.getId().toString());

            UIMessage.make(evalForm, "eval-preview-title", "controlevaluations.eval.preview.title");
            UIMessage.make(evalForm, "eval-link-title", "controlevaluations.eval.link.title");

            UIInternalLink.make(evaluationRow, "closed-eval-link", evaluation.getTitle(), 
                  new EvalViewParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId() ) );
            if (evaluation.getEvalCategory() != null) {
               UIOutput category = UIOutput.make(evaluationRow, "eval-category", shortenText(evaluation.getEvalCategory(), 20) );
               category.decorators = new DecoratorList( 
                     new UITooltipDecorator( evaluation.getEvalCategory() ) );
            }

            // vary the display depending on the number of groups assigned
            int groupsCount = evaluationService.countEvaluationGroups(evaluation.getId());
            if (groupsCount == 1) {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "closed-eval-assigned-link", 
                     getTitleForFirstEvalGroup(evaluation.getId()),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            } else {
               UICommand evalAssigned = UICommand.make(evaluationRow, 
                     "closed-eval-assigned-link", 
                     UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
               "#{evaluationBean.evalAssigned}");
               evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
            }

            // calculate the response rate
            int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId());
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

            UIOutput.make(evaluationRow, "closed-eval-response-rate", responseString );

            UIOutput.make(evaluationRow, "closed-eval-duedate", df.format(evaluation.getDueDate()));

            UICommand evalEdit = UICommand.make(evaluationRow, "closed-eval-edit-link", 
                  UIMessage.make("general.command.edit"), "#{evaluationBean.editEvalSettingAction}");
            evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

            if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(EvalUtils.getEvaluationState(evaluation, false)) ) {
               int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
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
                     new String[] { df.format(evaluation.getViewDate()) });
            }

            if ( evaluationService.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
               // evaluation removable
               UIInternalLink.make(evaluationRow, "closed-eval-delete-link", 
                     UIMessage.make("general.command.delete"), 
                     new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
            }

         }
      } else {
         UIMessage.make(tofill, "no-closed-evals", "controlevaluations.closed.none");
      }

   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();

      i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(
            EvaluationSettingsProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new SimpleViewParameters(
            EvaluationStartProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(
            EvaluationAssignConfirmProducer.VIEW_ID)));

      return i;
   }	

   /**
    * Gets the title for the first returned evalGroupId for this evaluation,
    * should only be used when there is only one evalGroupId assigned to an eval
    * 
    * @param evaluationId
    * @return title of first evalGroupId returned
    */
   private String getTitleForFirstEvalGroup(Long evaluationId) {
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
      List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
      EvalAssignGroup eac = (EvalAssignGroup) groups.get(0);
      return externalLogic.getDisplayTitle( eac.getEvalGroupId() );
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
