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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.ScaleBeanLocator;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
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
import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * Handles scale addition, removal, and modification.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ModifyScaleProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_scale";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private BoundedDynamicListInputEvolver boundedDynamicListInputEvolver;
    public void setBoundedDynamicListInputEvolver(BoundedDynamicListInputEvolver boundedDynamicListInputEvolver) {
        this.boundedDynamicListInputEvolver = boundedDynamicListInputEvolver;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings=evalSettings;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        EvalScaleParameters evalScaleParams = (EvalScaleParameters) viewparams;
        Long scaleId = evalScaleParams.id;

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
        boundedDynamicListInputEvolver.setMinimumLength((Integer)evalSettings.get(EvalSettings.EVAL_MIN_LIST_LENGTH));
        boundedDynamicListInputEvolver.setMaximumLength((Integer)evalSettings.get(EvalSettings.EVAL_MAX_LIST_LENGTH));

        UIInputMany modifypoints = UIInputMany.make(form, 
                "modify-scale-points:", scaleOTP + "options");
        boundedDynamicListInputEvolver.evolve(modifypoints);

        UISelect radios = UISelect.make(form, "scaleIdealRadio", 
                EvalToolConstants.SCALE_IDEA_VALUES, 
                EvalToolConstants.SCALE_IDEAL_LABELS, 
                scaleOTP + "ideal").setMessageKeys();
        radios.selection.mustapply = true; // this is required to ensure that the value gets passed even if it is not changed

        String selectID = radios.getFullID();
        for (int i = 0; i < EvalToolConstants.SCALE_IDEA_VALUES.length; ++i) {
            UIBranchContainer radiobranch = UIBranchContainer.make(form, "scaleIdealOptions:", i+"");
            UISelectLabel.make(radiobranch, "scale-ideal-label", selectID, i);
            UISelectChoice.make(radiobranch, "scale-ideal-value", selectID, i);
        }

        if (userAdmin) {
            UIBranchContainer sharingBranch = UIBranchContainer.make(form, "sharing-branch:");
            UISelect.make(sharingBranch, "scale-sharing", 
                    EvalToolConstants.SHARING_VALUES, 
                    EvalToolConstants.SHARING_LABELS_PROPS, 
                    scaleOTP + "sharing").setMessageKeys();
        }

        // command buttons
        UICommand.make(form, "scale-add-modify-save-button", 
                UIMessage.make("modifyscale.save.scale.button"), "templateBBean.saveScaleAction");
        UIInternalLink.make(form, "scale-add-modify-preview", 
                UIMessage.make("modifyscale.save.preview.button"), 
                // NOTE: special case which generates the URL without any params, we will fill in the params later via JS
                new EvalScaleParameters(PreviewScaleProducer.VIEW_ID)
        );
        UIMessage.make(form, "scale-add-modify-cancel", "general.cancel.button");

    }

    /* 
     * (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
