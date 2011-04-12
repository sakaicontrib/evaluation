/******************************************************************************
 * ControlTemplatesProducer.java - created by aaronz@vt.edu on Mar 19, 2007
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
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This lists templates for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlTemplatesProducer implements ViewComponentProducer, ViewParamsReporter {

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
     */
    public static String VIEW_ID = "control_templates";
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

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();

        //Process request for one template requested
        TemplateViewParameters evalVPSingle = (TemplateViewParameters) viewparams;
        Long templateId = evalVPSingle.templateId;
        if(templateId != null){
            EvalTemplate template = authoringService.getTemplateById(templateId);
            UIBranchContainer templateListing = UIBranchContainer.make(tofill, "template-listing:");
            UIForm form = UIForm.make(templateListing, "copyForm");

            UIBranchContainer templateBranch = UIBranchContainer.make(form, "template-row:", template.getId().toString());
            templateBranch.decorate(new UIFreeAttributeDecorator("rowId", template.getId().toString()));

            // local locked check is more efficient so do that first
            if ( ! template.getLocked().booleanValue() &&
                    authoringService.canModifyTemplate(currentUserId, template.getId()) ) {
                // template controllable
            } else {
                // template not controllable
            }
            UIOutput.make(templateBranch, "template-title", template.getTitle());

            // local locked check is more efficient so do that first
            if ( ! template.getLocked().booleanValue() &&
                    authoringService.canModifyTemplate(currentUserId, template.getId()) ) {
                UIInternalLink.make(templateBranch, "template-modify-link", UIMessage.make("general.command.edit"), 
                        new TemplateViewParameters( ModifyTemplateItemsProducer.VIEW_ID, template.getId() ));
            } else {
                UIMessage.make(templateBranch, "template-modify-dummy", "general.command.edit")
                .decorate( new UITooltipDecorator( UIMessage.make("controltemplates.template.inuse.note") ) );
            }
            if ( ! template.getLocked().booleanValue() &&
                    authoringService.canRemoveTemplate(currentUserId, template.getId()) ) {
                UIInternalLink.make(templateBranch, "template-delete-link", UIMessage.make("general.command.delete"),
                        new TemplateViewParameters( RemoveTemplateProducer.VIEW_ID, template.getId() ));
            } else {
                UIMessage.make(templateBranch, "template-delete-dummy", "general.command.delete")
                .decorate( new UITooltipDecorator( UIMessage.make("controltemplates.template.inuse.note") ) );
            }
            UIInternalLink.make(templateBranch, "template-preview-link", UIMessage.make("general.command.preview"),
                    new EvalViewParameters( PreviewEvalProducer.VIEW_ID, null, template.getId() ));

            // direct link to the template
            UILink.make(templateBranch, "template-direct-link", UIMessage.make("general.direct.link"), 
                    commonLogic.getEntityURL(template) )
                    .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );

            EvalUser owner = commonLogic.getEvalUserById( template.getOwner() );
            UIOutput.make(templateBranch, "template-owner", owner.displayName );
            UIOutput.make(templateBranch, "template-last-update", df.format( template.getLastModified() ));

            // create the copy button/link
            UICommand copy = UICommand.make(templateBranch, "template-copy-link", 
                    UIMessage.make("general.copy"), "templateBBean.copyTemplate");
            copy.parameters.add(new UIELBinding("templateBBean.templateId", template.getId()));
        }
        else{
            // page title
            UIMessage.make(tofill, "page-title", "controltemplates.page.title");

            /*
             * top links here
             */
            navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

            // create template header and link
            UIMessage.make(tofill, "templates-header", "controltemplates.templates.header"); 
            UIMessage.make(tofill, "templates-description","controltemplates.templates.description"); 
            UIInternalLink.make(tofill, "create-template-link", 
                    UIMessage.make("createtemplate.page.title"), 
                    new TemplateViewParameters(ModifyTemplateProducer.VIEW_ID, null));

            // get templates for the current user
            List<EvalTemplate> templates = authoringService.getTemplatesForUser(currentUserId, null, true);
            if (templates.size() > 0) {
                UIBranchContainer templateListing = UIBranchContainer.make(tofill, "template-listing:");
                UIForm form = UIForm.make(templateListing, "copyForm");

                for (int i = 0; i < templates.size(); i++) {
                    EvalTemplate template = (EvalTemplate) (templates.get(i));

                    UIBranchContainer templateBranch = UIBranchContainer.make(form, "template-row:", template.getId().toString());
                    templateBranch.decorate(new UIFreeAttributeDecorator("rowId", template.getId().toString()));

                    // local locked check is more efficient so do that first
                    if ( ! template.getLocked().booleanValue() &&
                            authoringService.canModifyTemplate(currentUserId, template.getId()) ) {
                        // template controllable
                    } else {
                        // template not controllable
                    }
                    UIOutput.make(templateBranch, "template-title", template.getTitle());

                    // local locked check is more efficient so do that first
                    if ( ! template.getLocked().booleanValue() &&
                            authoringService.canModifyTemplate(currentUserId, template.getId()) ) {
                        UIInternalLink.make(templateBranch, "template-modify-link", UIMessage.make("general.command.edit"), 
                                new TemplateViewParameters( ModifyTemplateItemsProducer.VIEW_ID, template.getId() ));
                    } else {
                        UIMessage.make(templateBranch, "template-modify-dummy", "general.command.edit")
                        .decorate( new UITooltipDecorator( UIMessage.make("controltemplates.template.inuse.note") ) );
                    }
                    if ( ! template.getLocked().booleanValue() &&
                            authoringService.canRemoveTemplate(currentUserId, template.getId()) ) {
                        UIInternalLink.make(templateBranch, "template-delete-link", UIMessage.make("general.command.delete"),
                                new TemplateViewParameters( RemoveTemplateProducer.VIEW_ID, template.getId() ));
                    } else {
                        UIMessage.make(templateBranch, "template-delete-dummy", "general.command.delete")
                        .decorate( new UITooltipDecorator( UIMessage.make("controltemplates.template.inuse.note") ) );
                    }
                    UIInternalLink.make(templateBranch, "template-preview-link", UIMessage.make("general.command.preview"),
                            new EvalViewParameters( PreviewEvalProducer.VIEW_ID, null, template.getId() ));

                    // direct link to the template
                    UILink.make(templateBranch, "template-direct-link", UIMessage.make("general.direct.link"), 
                            commonLogic.getEntityURL(template) )
                            .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );

                    EvalUser owner = commonLogic.getEvalUserById( template.getOwner() );
                    UIOutput.make(templateBranch, "template-owner", owner.displayName );
                    UIOutput.make(templateBranch, "template-last-update", df.format( template.getLastModified() ));

                    // create the copy button/link
                    UICommand copy = UICommand.make(templateBranch, "template-copy-link", 
                            UIMessage.make("general.copy"), "templateBBean.copyTemplate");
                    copy.parameters.add(new UIELBinding("templateBBean.templateId", template.getId()));

                }	
            } else {
                UIMessage.make(tofill, "no-templates", "controltemplates.templates.none");
            }
        }
    }


    public ViewParameters getViewParameters() {
        // TODO Auto-generated method stub
        return new TemplateViewParameters();
    }

}
