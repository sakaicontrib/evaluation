package org.sakaiproject.evaluation.tool.renderers;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.producers.AdministrateProducer;
import org.sakaiproject.evaluation.tool.producers.ControlEmailTemplatesProducer;
import org.sakaiproject.evaluation.tool.producers.ControlEvaluationsProducer;
import org.sakaiproject.evaluation.tool.producers.ControlItemsProducer;
import org.sakaiproject.evaluation.tool.producers.ControlScalesProducer;
import org.sakaiproject.evaluation.tool.producers.ControlTemplatesProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
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

    
    private String currentViewID;
    
    public static String NAV_ELEMENT = "navIntraTool:";
    
	public void makeNavBar(UIContainer tofill, String divID, String currentViewID) {
		
        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean isUserAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean canCreateTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean canBeginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        UIJointContainer joint = new UIJointContainer(tofill, divID, "evals-navigation:");
        boolean hideQuestionBank = ((Boolean)settings.get(EvalSettings.DISABLE_ITEM_BANK)).booleanValue();
        boolean showMyToplinks = ((Boolean)settings.get(EvalSettings.ENABLE_MY_TOPLINKS)).booleanValue();
        
        // set a few local variables
        this.currentViewID = currentViewID;
        
        if (isUserAdmin) {
            renderLink(joint, AdministrateProducer.VIEW_ID, "administrate.page.title");
        }
        
        renderLink(joint, SummaryProducer.VIEW_ID, "summary.page.title");


        
        if(isUserAdmin || showMyToplinks) {
        	
        	if (isUserAdmin || canBeginEvaluation) {
        		renderLink(joint, ControlEvaluationsProducer.VIEW_ID, "controlevaluations.page.title");
        	}
        	
        	if (isUserAdmin || canCreateTemplate) {
        		renderLink(joint, ControlTemplatesProducer.VIEW_ID, "controltemplates.page.title");
        		if (isUserAdmin || ! hideQuestionBank) {
        			renderLink(joint, ControlItemsProducer.VIEW_ID, "controlitems.page.title");
        			renderLink(joint, ControlScalesProducer.VIEW_ID, "controlscales.page.title");
        		}
        	}

        	if (isUserAdmin || canBeginEvaluation) {
        		renderLink(joint, ControlEmailTemplatesProducer.VIEW_ID, "controlemailtemplates.page.title"); 
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
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
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
}