/******************************************************************************
 * ScaleControlProducer.java - created by kahuja@vt.edu
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

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalScale;
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
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles scale addition, removal, and modification.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlScalesProducer implements ViewComponentProducer {

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
    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();

        /*
         * top links here
         */
        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));
        UIInternalLink.make(tofill, "administrate-link", 
                UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));
        UIInternalLink.make(tofill, "control-scales-link",
                UIMessage.make("controlscales.page.title"),
                new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
        UIInternalLink.make(tofill, "control-templates-link",
                UIMessage.make("controltemplates.page.title"), 
                new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
        if (!((Boolean)evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
            UIInternalLink.make(tofill, "control-items-link",
                    UIMessage.make("controlitems.page.title"), 
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
        }
        UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"),
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "controlscales.page.title");

        UIInternalLink.make(tofill, "add-new-scale-link", 
                UIMessage.make("controlscales.add.new.scale.link"), 
                new EvalScaleParameters(ModifyScaleProducer.VIEW_ID, null));

        UIMessage.make(tofill, "scales-control-heading", "controlscales.page.heading");
        UIMessage.make(tofill, "scales-control-instruction", "controlscales.page.instruction");

        UIForm form = UIForm.make(tofill, "copyForm");

        //Get all the scales that are owned by a user
        List<EvalScale> scaleList = authoringService.getScalesForUser(currentUserId, null);
        for (int i = 0; i < scaleList.size(); ++i) {
            EvalScale scale = scaleList.get(i);

            //       NOTE - thise code was here to vet the new scales code, it passed this test -AZ
            //       if (i == 0) {
            //       System.out.println("Changing scale: " + scale.getTitle() + ":" + scale.getOptions().length);
            //       long random = Math.round( Math.random() * 10 );
            //       long random2 = Math.round( Math.random() * 3 );
            //       String[] options;
            //       if (random2 <= 1) {
            //       options = new String[] {"az1"+random, "az2"+random, "az3"+random};
            //       } else if (random2 <= 2) {
            //       options = new String[] {"az1"+random, "az2"+random, "az3"+random, "az4"+random, "az5"+random};
            //       } else {
            //       options = new String[] {"az1"+random, "az2"+random, "az3"+random, "az4"+random, "az5"+random, "az6"+random, "az7"+random};
            //       }
            //       scale.setOptions(options);
            //       scalesLogic.saveScale(scale, currentUserId);
            //       System.out.println("Changed scale: " + scale.getTitle() + ":" + scale.getOptions().length);
            //       }

            UIBranchContainer scaleBranch = UIBranchContainer.make(form, "verticalDisplay:", i+"");
            UIOutput.make(scaleBranch, "scale-no", (i + 1)+"");
            UIOutput.make(scaleBranch, "scale-title", scale.getTitle());

            /*
             * Note that although canControlScale does a locked check,
             * it is more efficient to avoid a cycle by checking the local data first (i.e. getLocked() call)
             */
            if (! scale.getLocked().booleanValue() &&
                    authoringService.canModifyScale(currentUserId, scale.getId()) ) {
                UIInternalLink.make(scaleBranch, "modify-link", 
                        UIMessage.make("general.command.edit"), 
                        new EvalScaleParameters(ModifyScaleProducer.VIEW_ID, scale.getId()));
            } else {
                UIMessage.make(scaleBranch, "modify-dummy", "general.command.edit");
            }

            if (! scale.getLocked().booleanValue() &&
                    authoringService.canRemoveScale(currentUserId, scale.getId()) ) {
                UIInternalLink.make(scaleBranch, "remove-link", 
                        UIMessage.make("general.command.delete"), 
                        new EvalScaleParameters(RemoveScaleProducer.VIEW_ID, scale.getId()));
            } else {
                UIMessage.make(scaleBranch, "remove-dummy", "general.command.delete");
            }

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
