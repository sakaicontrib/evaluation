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

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.locators.LineBreakResolver;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
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
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles email template editing, (maybe also adding/removal)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEmailTemplatesProducer extends EvalCommonProducer implements ViewParamsReporter {

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

    private static final String EMAIL_TEMPLATE_LOCATOR = "emailTemplateWBL.";
    private static final String DEFAULTS = "defaults";
    private static final String OTHERS = "others";
    private static final UIDecorator CLASS_DECORATOR = new UIStyleDecorator("inactive");
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        SwitchViewParams svp = (SwitchViewParams) viewparams;

        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        //top links here
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

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
                defaultLink.decorate(CLASS_DECORATOR);
            } else {
                showDefaults = false;
                otherLink.decorate(CLASS_DECORATOR);
            }
        }

        // Get all the email templates for the user
        List<EvalEmailTemplate> templatesList = evaluationService.getEmailTemplatesForUser(currentUserId, null, showDefaults);
        if (templatesList.isEmpty()) {
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
                command.addParameter( new UIDeletionBinding(EMAIL_TEMPLATE_LOCATOR + emailTemplate.getId()) );
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
