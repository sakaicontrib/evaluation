/******************************************************************************
 * RemoveQuestionProducer.java - created by fengr@vt.edu on Sep 26, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This page is to remove an Item(all kind of Item type) from DAO
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveQuestionProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "remove_question"; //$NON-NLS-1$
	
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	private ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}

	public Long templateId;
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;
		
        Long templateItemId = templateItemViewParams.templateItemId;
        templateId=templateItemViewParams.templateId;
		
        String templateItemOTPBinding="templateItemBeanLocator."+templateItemId;		
        EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
      
		UIOutput.make(tofill, "remove-question-title", messageLocator.getMessage("removequestion.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));	
		
		UIOutput.make(tofill, "remove-question-confirm-pre-name", messageLocator.getMessage("removequestion.confirm.pre.name")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "remove-question-confirm-post-name", messageLocator.getMessage("removequestion.confirm.post.name")); //$NON-NLS-1$ //$NON-NLS-2$

		//use the renderer evolver
		itemRenderer.renderItem(tofill, "remove-item:", null, templateItem, 0, true);
	
		UIForm form = UIForm.make(tofill, "removeQuestionForm");
		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));
		UICommand rmvBtn=UICommand.make(form, "removeQuestionAction", messageLocator.getMessage("removequestion.remove.button"),  
				"#{itemsBean.removeItemAction}"); //$NON-NLS-1$
		rmvBtn.parameters.add(new UIELBinding("#{itemsBean.templateItem}", new ELReference(templateItemOTPBinding)));
	}
	
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase("removed", new EvalViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId)));
		return i;
	}
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		   return new TemplateItemViewParameters();
	}
	  
}
