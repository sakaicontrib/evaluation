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

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles scale addition, removal, and modification.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlScalesProducer extends EvalCommonProducer {

    public static final String VIEW_ID = "control_scales";
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

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        UIMessage.make(tofill, "page-title", "controlscales.page.title");

        UIInternalLink.make(tofill, "add-new-scale-link", 
                UIMessage.make("controlscales.add.new.scale.link"), 
                new EvalScaleParameters(ModifyScaleProducer.VIEW_ID));

        UIMessage.make(tofill, "scales-control-heading", "controlscales.page.heading");
        UIMessage.make(tofill, "scales-control-instruction", "controlscales.page.instruction");

        UIForm form = UIForm.make(tofill, "copyForm");

        //Get all the scales that are owned by a user
        List<EvalScale> scaleList = authoringService.getScalesForUser(currentUserId, null);
        for (int i = 0; i < scaleList.size(); ++i) {
            EvalScale scale = scaleList.get(i);

            UIBranchContainer scaleBranch = UIBranchContainer.make(form, "verticalDisplay:", i+"");
            UIOutput.make(scaleBranch, "scale-no", (i + 1)+"");
            UIOutput.make(scaleBranch, "scale-title", scale.getTitle());

            /*
             * Note that although canControlScale does a locked check,
             * it is more efficient to avoid a cycle by checking the local data first (i.e. getLocked() call)
             */
            if (! scale.getLocked() &&
                    authoringService.canModifyScale(currentUserId, scale.getId()) ) {
                UIInternalLink.make(scaleBranch, "modify-link", 
                        UIMessage.make("general.command.edit"), 
                        new EvalScaleParameters(ModifyScaleProducer.VIEW_ID, scale.getId()));
            } else {
                UIMessage.make(scaleBranch, "modify-dummy", "general.command.edit");
            }

            if (! scale.getLocked() &&
                    authoringService.canRemoveScale(currentUserId, scale.getId()) ) {
                UIInternalLink.make(scaleBranch, "remove-link", 
                        UIMessage.make("general.command.delete"), 
                        new EvalScaleParameters(RemoveScaleProducer.VIEW_ID, scale.getId()));
            } else {
                UIMessage.make(scaleBranch, "remove-dummy", "general.command.delete");
            }

            UIInternalLink.make(scaleBranch, "preview-link", 
                    UIMessage.make("general.command.preview"), 
                    new EvalScaleParameters(PreviewScaleProducer.VIEW_ID, scale.getId())
            );

            // Display the scale options vertically
            // ASCII value of 'a' = 97 so initial value is 96.
            // This is kinda weird, not sure it is really needed -AZ
            char[] startOptionsNo = { 96 };
            for (int j = 0; j < scale.getOptions().length; ++j) {
                UIBranchContainer scaleOptions = UIBranchContainer.make(scaleBranch, "scaleOptions:", j+"");
                startOptionsNo[0]++;
                UIOutput.make(scaleOptions, "scale-option-no", new String(startOptionsNo));
                UIOutput.make(scaleOptions, "scale-option-label", (scale.getOptions())[j]);
            }

            UIMessage.make(scaleBranch, "ideal-scale-point", "controlscales.ideal.scale.title");

            // Based on the scale ideal value, pick the corresponding i18n message
            if (scale.getIdeal() == null) {
                UIMessage.make(scaleBranch, "ideal-value", "controlscales.ideal.scale.option.label.none");
            } else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_MID)) {
                UIMessage.make(scaleBranch, "ideal-value", "controlscales.ideal.scale.option.label.mid");
            } else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_HIGH)) {
                UIMessage.make(scaleBranch, "ideal-value", "controlscales.ideal.scale.option.label.high");
            } else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_LOW)) {
                UIMessage.make(scaleBranch, "ideal-value", "controlscales.ideal.scale.option.label.low");
            } else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_OUTSIDE)) {
                UIMessage.make(scaleBranch, "ideal-value", "controlscales.ideal.scale.option.label.outside");
            } else {
                UIMessage.make(scaleBranch, "ideal-value", "unknown.caps");
            }

            // create the copy button/link
            UICommand copy = UICommand.make(scaleBranch, "scale-copy-link", 
                    UIMessage.make("general.copy"), "templateBBean.copyScale");
            copy.parameters.add(new UIELBinding("templateBBean.scaleId", scale.getId()));

        }
    }

}
