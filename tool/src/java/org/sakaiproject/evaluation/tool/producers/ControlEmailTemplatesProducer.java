/**
 * $Id$
 * $URL$
 * ControlEmailTemplatesProducer.java - evaluation - Feb 29, 2008 9:44:19 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.locators.LineBreakResolver;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.SwitchViewParams;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles email template editing, (maybe also adding/removal)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEmailTemplatesProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "control_email_templates";
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

    private String emailTemplateLocator = "emailTemplateWBL.";
    private String DEFAULTS = "defaults";
    private String OTHERS = "others";
    private UIDecorator classDecorator = new UIStyleDecorator("inactive");

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        SwitchViewParams svp = (SwitchViewParams) viewparams;

        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

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

        UIInternalLink.make(tofill, "control-emailtemplates-link",
                UIMessage.make("controlemailtemplates.page.title"),
                new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));

        // ability to control the listing of templates that appear (i.e. enable more than default templates)
        boolean showDefaults = false;
        if (userAdmin) {
            // default to showing the default templates
            if (svp.switcher == null) {
                svp.switcher = DEFAULTS;
            }
            UIBranchContainer switchBranch = UIBranchContainer.make(tofill, "showSwitch:");
            UIInternalLink defaultLink = UIInternalLink.make(switchBranch, "showDefaultsLink",
                    UIMessage.make("controlemailtemplates.show.defaults.link"),
                    new SwitchViewParams(ControlEmailTemplatesProducer.VIEW_ID, DEFAULTS));
            UIInternalLink otherLink = UIInternalLink.make(switchBranch, "showOthersLink", 
                    UIMessage.make("controlemailtemplates.show.others.link"),
                    new SwitchViewParams(ControlEmailTemplatesProducer.VIEW_ID, OTHERS));
            if (svp.switcher.equals(DEFAULTS)) {
                showDefaults = true;
                defaultLink.decorate(classDecorator);
            } else {
                showDefaults = false;
                otherLink.decorate(classDecorator);
            }
        }

        // Get all the default email templates
        List<EvalEmailTemplate> templatesList = evaluationService.getEmailTemplatesForUser(currentUserId, null, showDefaults);
        if (templatesList.size() == 0) {
            UIMessage.make(tofill, "templatesList_none", "controlemailtemplates.no.templates");
        }
        for (int i = 0; i < templatesList.size(); i++) {
            EvalEmailTemplate emailTemplate = templatesList.get(i);

            UIBranchContainer templatesBranch = UIBranchContainer.make(tofill, "templatesList:", i+"");
            UIOutput.make(templatesBranch, "template_number", (i + 1)+"");
            UIOutput.make(templatesBranch, "template_title", emailTemplate.getDefaultType());
            UIMessage.make(templatesBranch, "template_subject", "controlemailtemplates.subject", 
                    new Object[] {emailTemplate.getSubject()});
            UIVerbatim.make(templatesBranch, "template_text", new LineBreakResolver().resolveBean(emailTemplate.getMessage()) );


            boolean canControl = evaluationService.canControlEmailTemplate(currentUserId, null, emailTemplate.getId());
            if ( canControl ) {
                UIInternalLink.make(templatesBranch, "modify_link", 
                        UIMessage.make("general.command.edit"), 
                        new EmailViewParameters(ModifyEmailProducer.VIEW_ID, emailTemplate.getId(), emailTemplate.getType(), null) );
            } else {
                UIMessage.make(templatesBranch, "modify_link_disabled", "general.command.edit");
            }

            if ( canControl && 
                    emailTemplate.getDefaultType() == null ) {
                UIForm form = UIForm.make(templatesBranch, "removeForm");
                UICommand command = UICommand.make(form, "removeCommand");
                command.addParameter( new UIDeletionBinding(emailTemplateLocator + emailTemplate.getId()) );
            } else {
                UIMessage.make(templatesBranch, "remove_link_disabled", "general.command.delete");
            }
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new SwitchViewParams();
    }

}
