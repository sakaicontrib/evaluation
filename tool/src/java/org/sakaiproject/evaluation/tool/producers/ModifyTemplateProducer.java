/******************************************************************************
 * TemplateProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * view to start creating a template and for modifying the template settings
 * (title, description, sharing)
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ModifyTemplateProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_template";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
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


    /*
     * 1) accessing this page trough "Create Template" link -- 2) accessing
     * through "Modify Template Title/Description" link on ModifyTemplate page
     * 2-1) no template been save in DAO 2-2) existing template in DAO
     * 
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
        boolean editing = (evalViewParams.templateId == null)? false:true;

        /*
         * top links here
         */
        if (!editing) {
            UIOutput.make(tofill, "js-jquery");

            UIBranchContainer topLinks = UIBranchContainer.make(tofill,"top-links:");
            UIInternalLink.make(topLinks, "summary-link", 
                    UIMessage.make("summary.page.title"), 
                    new SimpleViewParameters(SummaryProducer.VIEW_ID));

            if (userAdmin) {
                UIInternalLink.make(topLinks, "administrate-link", 
                        UIMessage.make("administrate.page.title"),
                        new SimpleViewParameters(AdministrateProducer.VIEW_ID));
                UIInternalLink.make(topLinks, "control-scales-link",
                        UIMessage.make("controlscales.page.title"),
                        new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
            }

            if (createTemplate) {
                UIInternalLink.make(topLinks, "control-templates-link",
                        UIMessage.make("controltemplates.page.title"), 
                        new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
                if (!((Boolean)settings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                    UIInternalLink.make(topLinks, "control-items-link",
                            UIMessage.make("controlitems.page.title"), 
                            new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
                }
            } else {
                throw new SecurityException("User attempted to access " + 
                        VIEW_ID + " when they are not allowed");
            }

            if (beginEvaluation) {
                UIInternalLink.make(topLinks, "control-evaluations-link",
                        UIMessage.make("controlevaluations.page.title"),
                        new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
            }
        }

        UIMessage.make(tofill, "template-title-desc-title", "modifytemplatetitledesc.page.title");

        // setup the OTP binding strings
        String templateOTPBinding = null;
        if (editing) {
            templateOTPBinding = "templateBeanLocator." + evalViewParams.templateId;
        } else {
            templateOTPBinding = "templateBeanLocator." + TemplateBeanLocator.NEW_1;
        }
        String templateOTP = templateOTPBinding + ".";


        UIForm form = UIForm.make(tofill, "basic-form");
        UIMessage.make(form, "title-header", "modifytemplatetitledesc.title.header");
        UIMessage.make(form, "description-header", "modifytemplatetitledesc.description.header");
        UIMessage.make(form, "description-note", "modifytemplatetitledesc.description.note");
        UIMessage.make(form, "sharing-note", "modifytemplatetitledesc.sharing.note");

        UIInput.make(form, "title", templateOTP + "title");
        UIInput.make(form, "description", templateOTP + "description");

        /*
         * If "EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY" is set
         * EvalConstants.SHARING_OWNER, then it means that owner can decide what
         * sharing settings to chose. In other words, it means that show the
         * sharing/visibility dropdown. Else just show the label for what has been
         * set in system setting (admin setting) i.e.
         * "EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY".
         */
        String sharingkey = null;
        String sharingValue = (String) settings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
        if (sharingValue == null) {
            throw new IllegalStateException("SHARING setting cannot be determined because of invalid system settings (sharingValue is null and should not be)");
        }
        if ( EvalConstants.SHARING_OWNER.equals(sharingValue) ) {
            /*
             * Dropdown values are visible only for admins. For instructors
             * (non-admin) we just show the private label
             */
            if (commonLogic.isUserAdmin(commonLogic.getCurrentUserId())) {
                UIBranchContainer showSharingOptions = UIBranchContainer.make(form,	"showSharingOptions:");
                UISelect.make(showSharingOptions, "sharing",
                        EvalToolConstants.SHARING_VALUES, 
                        EvalToolConstants.SHARING_LABELS_PROPS, 
                        templateOTP	+ "sharing", null).setMessageKeys();
            } else {
                sharingkey = "sharing.private";
                form.parameters.add(new UIELBinding(templateOTP + "sharing", EvalConstants.SHARING_PRIVATE));
            }
        } else {
            if ((EvalConstants.SHARING_PRIVATE).equals(sharingValue)) {
                sharingkey = "sharing.private";
            } else if ((EvalConstants.SHARING_PUBLIC).equals(sharingValue)) {
                sharingkey = "sharing.public";
            }

            // Doing the binding of this sharing value so that it can be persisted
            form.parameters.add(new UIELBinding(templateOTP + "sharing", sharingValue));
        }

        if (sharingkey != null) {
            // Displaying the sharing label
            UIMessage.make(form, "sharingValueToDisplay", sharingkey);
        }

        UICommand.make(form, "addContinue", UIMessage.make("modifytemplatetitledesc.save.button"),
        "#{templateBBean.updateTemplateTitleDesc}");
        if (editing) 
            UIMessage.make(form, "cancel-button-lightbox", "general.cancel.button");
        else
            UIMessage.make(form, "cancel-button", "general.cancel.button"); 
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new TemplateViewParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List togo = new ArrayList();
        togo.add(new NavigationCase(new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, null)));
        return togo;
    }

}
