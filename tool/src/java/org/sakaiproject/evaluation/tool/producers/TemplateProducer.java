/******************************************************************************
 * TemplateProducer.java - created by fengr@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Page for start creating a template
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class TemplateProducer implements ViewComponentProducer, NavigationCaseReporter {
	public static final String VIEW_ID = "template_title_desc"; 
	public String getViewID() {
		return VIEW_ID;
	}
	
	private TemplateBean templateBean;	
	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	

	/*
	 * 1) accessing this page trough "Create Template" link --
	 * 2) accessing through "Modify Template Title/Description" link on ModifyTemplate page
	 * 		2-1) no template been save in DAO
	 * 		2-2) existing template in DAO 
	 *
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {		
		
		UIOutput.make(tofill, "template-title-desc-title", messageLocator.getMessage("modifytemplatetitledesc.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIForm form = UIForm.make(tofill, "basic-form"); //$NON-NLS-1$
		UIOutput.make(form, "title-header", messageLocator.getMessage("modifytemplatetitledesc.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "description-header", messageLocator.getMessage("modifytemplatetitledesc.description.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "description-note", messageLocator.getMessage("modifytemplatetitledesc.description.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
			UIInput.make(form, "title", "#{templateBean.title}");
			UIInput.make(form, "description", "#{templateBean.description}");
	
			//dropdown list		
			UISelect combo = UISelect.make(form, "sharing");
			combo.selection = new UIInput();
			combo.selection.valuebinding = new ELReference("#{templateBean.modifier}");
			//combo.selection.valuebinding = new ELReference("#{templateBean.template.sharing}");
			UIBoundList comboValues = new UIBoundList();
			comboValues.setValue(EvaluationConstant.MODIFIER_VALUES);
			combo.optionlist = comboValues;
			UIBoundList comboNames = new UIBoundList();
			String[] sharingList = 
			{
				messageLocator.getMessage("modifytemplatetitledesc.sharing.private"),
				messageLocator.getMessage("modifytemplatetitledesc.sharing.visible"),
				messageLocator.getMessage("modifytemplatetitledesc.sharing.shared"),
				messageLocator.getMessage("modifytemplatetitledesc.sharing.public")
			};
			comboNames.setValue(sharingList); //TODO: pull strings from properties file
			combo.optionnames = comboNames;
			
			EvalTemplate tpl= templateBean.getCurrTemplate();
			
			UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));
			
			if(tpl!=null && tpl.getId()!=null)
				UICommand.make(form, "addContinue",messageLocator.getMessage("modifytemplatetitledesc.save.button"), "#{templateBean.saveTemplate}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			else 
				UICommand.make(form, "addContinue",messageLocator.getMessage("modifytemplatetitledesc.continue.button"), "#{templateBean.createTemplateAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		
		//TODO-i18n cancel button
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));
		
		return i;
	}


}
