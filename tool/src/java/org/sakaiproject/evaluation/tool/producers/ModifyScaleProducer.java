/******************************************************************************
 * ModifyScaleProducer.java - created by kahuja@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.ScaleBeanLocator;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInputMany;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles scale addition, removal, and modification.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ModifyScaleProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_scale";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private BoundedDynamicListInputEvolver boundedDynamicListInputEvolver;
    public void setBoundedDynamicListInputEvolver(BoundedDynamicListInputEvolver boundedDynamicListInputEvolver) {
        this.boundedDynamicListInputEvolver = boundedDynamicListInputEvolver;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
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
            if (!((Boolean) evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                UIInternalLink.make(tofill, "control-items-link",
                        UIMessage.make("controlitems.page.title"), 
                        new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            }
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        }

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }


        EvalScaleParameters evalScaleParams = (EvalScaleParameters) viewparams;
        Long scaleId = evalScaleParams.scaleId;

        String scaleOTP = "scaleBeanLocator.";
        if (scaleId == null) {
            // new scale
            scaleOTP += ScaleBeanLocator.NEW_1 + ".";
        } else {
            scaleOTP += scaleId + ".";
        }

        UIForm form = UIForm.make(tofill, "basic-form");

        UIInput.make(form, "scale-title", scaleOTP + "title");

        // use the logic layer method to determine if scales can be controlled
        if (scaleId != null && 
                authoringService.canRemoveScale(currentUserId, scaleId)) {
            UIInternalLink.make(form, "scale-remove-link", 
                    UIMessage.make("modifyscale.remove.scale.link"), 
                    new EvalScaleParameters(RemoveScaleProducer.VIEW_ID, scaleId) );
        }

        boundedDynamicListInputEvolver.setLabels(
                UIMessage.make("modifyscale.remove.scale.option.button"), 
                UIMessage.make("modifyscale.add.scale.option.button"));
        boundedDynamicListInputEvolver.setMinimumLength(2);
        boundedDynamicListInputEvolver.setMaximumLength(40);

        UIInputMany modifypoints = UIInputMany.make(form, 
                "modify-scale-points:", scaleOTP + "options");
        boundedDynamicListInputEvolver.evolve(modifypoints);

        UISelect radios = UISelect.make(form, "scaleIdealRadio", 
                EvalToolConstants.scaleIdealValues, 
                EvalToolConstants.scaleIdealLabels, 
                scaleOTP + "ideal").setMessageKeys();
        radios.selection.mustapply = true; // this is required to ensure that the value gets passed even if it is not changed

        String selectID = radios.getFullID();
        for (int i = 0; i < EvalToolConstants.scaleIdealValues.length; ++i) {
            UIBranchContainer radiobranch = UIBranchContainer.make(form, "scaleIdealOptions:", i+"");
            UISelectLabel label = UISelectLabel.make(radiobranch, "scale-ideal-label", selectID, i);
            UISelectChoice choice = UISelectChoice.make(radiobranch, "scale-ideal-value", selectID, i);
            UILabelTargetDecorator.targetLabel(label, choice);
        }

        if (userAdmin) {
            UIBranchContainer sharingBranch = UIBranchContainer.make(form, "sharing-branch:");
            UISelect.make(sharingBranch, "scale-sharing", 
                    EvalToolConstants.SHARING_VALUES, 
                    EvalToolConstants.SHARING_LABELS_PROPS, 
                    scaleOTP + "sharing").setMessageKeys();
        }

        // command buttons
        UIMessage.make(form, "scale-add-modify-cancel-button", "general.cancel.button");
        UICommand.make(form, "scale-add-modify-save-button", 
                UIMessage.make("modifyscale.save.scale.button"), "templateBBean.saveScaleAction");

    }

    /* 
     * (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List togo = new ArrayList();
        togo.add(new NavigationCase("success", new SimpleViewParameters(ControlScalesProducer.VIEW_ID)));
        return togo;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalScaleParameters();
    }
}
