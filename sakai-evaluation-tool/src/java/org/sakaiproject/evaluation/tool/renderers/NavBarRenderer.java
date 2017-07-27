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
package org.sakaiproject.evaluation.tool.renderers;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.producers.AdministrateProducer;
import org.sakaiproject.evaluation.tool.producers.AssignPermissionsProducer;
import org.sakaiproject.evaluation.tool.producers.ControlEmailTemplatesProducer;
import org.sakaiproject.evaluation.tool.producers.ControlEvaluationsProducer;
import org.sakaiproject.evaluation.tool.producers.ControlItemsProducer;
import org.sakaiproject.evaluation.tool.producers.ControlScalesProducer;
import org.sakaiproject.evaluation.tool.producers.ControlTemplatesProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalListParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class NavBarRenderer {

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private DeveloperHelperService developerHelperService;
    public void setDeveloperHelperService( DeveloperHelperService developerHelperService )
    {
        this.developerHelperService = developerHelperService;
    }

    private static final String SAK_PROP_ENABLE_EXTRA_STUDENT_LINKS = "evalsys.enable.extra.student.links";
    private static final String SAK_PROP_UI_SERVICE = "ui.service";
    private static final String UI_SERVICE_DEFAULT = "Sakai";
    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService( ServerConfigurationService serverConfigurationService )
    {
        this.serverConfigurationService = serverConfigurationService;
    }

    private String currentViewID;
    
    public static String NAV_ELEMENT = "navIntraTool:";
    
    public void makeNavBar(UIContainer tofill, String divID, String currentViewID) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean isUserAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean canCreateTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean canBeginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        UIJointContainer joint = new UIJointContainer(tofill, divID, "evals-navigation:");
        boolean hideQuestionBank = ((Boolean)settings.get(EvalSettings.DISABLE_ITEM_BANK));
        boolean showMyToplinks = ((Boolean)settings.get(EvalSettings.ENABLE_MY_TOPLINKS));
        boolean adminAllowedToSee = isUserAdmin && showMyToplinks;
        boolean isUserAnon = commonLogic.isUserAnonymous(currentUserId);

        // set a few local variables
        this.currentViewID = currentViewID;

        if (isUserAdmin) {
            renderLink(joint, AdministrateProducer.VIEW_ID, "administrate.page.title");
        }

        // Provide logout and my workspace links
        boolean enableExtraStudentLinks = serverConfigurationService.getBoolean( SAK_PROP_ENABLE_EXTRA_STUDENT_LINKS, true );
        if( !isUserAnon && !isUserAdmin && !adminAllowedToSee && !canCreateTemplate && !canBeginEvaluation && enableExtraStudentLinks )
        {
            UIFreeAttributeDecorator targetDecorator = new UIFreeAttributeDecorator( "target", "_parent" );
            UILink workspaceLink = UILink.make( UIBranchContainer.make( joint, "navigation-cell:" ), "item-link", 
                    UIMessage.make( "summary.myworkspace.link.label" ), 
                    developerHelperService.getUserHomeLocationURL( developerHelperService.getCurrentUserReference() ) );
            workspaceLink.decorate( targetDecorator );

            UILink logoutLink = UILink.make( UIBranchContainer.make( joint, "navigation-cell:" ), "item-link", 
                    UIMessage.make( "summary.logout.link.label", new Object[] { serverConfigurationService.getString( SAK_PROP_UI_SERVICE, UI_SERVICE_DEFAULT ) } ), 
                    developerHelperService.getPortalURL() + "/logout" );
            logoutLink.decorate( targetDecorator );
        }

        renderLink(joint, SummaryProducer.VIEW_ID, "summary.page.title");
        
        if(adminAllowedToSee || showMyToplinks) {

            if (adminAllowedToSee || canBeginEvaluation) {
                renderLinkForMyEvals(joint, ControlEvaluationsProducer.VIEW_ID, "controlevaluations.page.title");
            }

            if (adminAllowedToSee || canCreateTemplate) {
                renderLink(joint, ControlTemplatesProducer.VIEW_ID, "controltemplates.page.title");
                if (adminAllowedToSee || ! hideQuestionBank) {
                    renderLink(joint, ControlItemsProducer.VIEW_ID, "controlitems.page.title");
                }
                renderLink(joint, ControlScalesProducer.VIEW_ID, "controlscales.page.title");
            }

            if (adminAllowedToSee || canBeginEvaluation) {
                renderLink(joint, ControlEmailTemplatesProducer.VIEW_ID, "controlemailtemplates.page.title"); 
            }

         if (adminAllowedToSee) {
            renderLink(joint, AssignPermissionsProducer.VIEW_ID, "assignPermissions.link.display");
         }
        }

        //handle breadcrumb rendering here. TODO: Review
        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (isUserAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                    UIMessage.make("administrate.page.title"),
                    new SimpleViewParameters(AdministrateProducer.VIEW_ID));
        }
        UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"),
                new EvalListParameters(ControlEvaluationsProducer.VIEW_ID,6));
        if(isUserAdmin || canCreateTemplate){
            UIInternalLink.make(tofill, "control-templates-link",
                    UIMessage.make("controltemplates.page.title"), 
                    new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
            if(isUserAdmin || ! hideQuestionBank){
                UIInternalLink.make(tofill, "control-scales-link",
                        UIMessage.make("controlscales.page.title"),
                        new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
            }
        }
    }

    private void renderLink(UIJointContainer joint, String linkViewID, String messageKey) {

        UIBranchContainer cell = UIBranchContainer.make(joint, "navigation-cell:");
        UIInternalLink link = UIInternalLink.make(cell, "item-link", UIMessage.make(messageKey),
                new SimpleViewParameters(linkViewID));

        if (currentViewID != null && currentViewID.equals(linkViewID)) {
            link.decorate( new UIStyleDecorator("inactive"));
        }
    }

    private void renderLinkForMyEvals(UIJointContainer joint, String linkViewID, String messageKey) {

        UIBranchContainer cell = UIBranchContainer.make(joint, "navigation-cell:");
        UIInternalLink link = UIInternalLink.make(cell, "item-link", UIMessage.make(messageKey),
                new EvalListParameters(linkViewID,6));

        if (currentViewID != null && currentViewID.equals(linkViewID)) {
            link.decorate( new UIStyleDecorator("inactive"));
        }
    }
}