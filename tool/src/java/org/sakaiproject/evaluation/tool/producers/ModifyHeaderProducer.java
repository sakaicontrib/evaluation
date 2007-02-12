/******************************************************************************
 * ModifyHeaderProducer.java - created by fengr@vt.edu on Sep 28, 2006
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

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Create, modify,preview, delete a Header type Item
 *
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyHeaderProducer implements ViewComponentProducer,ViewParamsReporter,NavigationCaseReporter,DynamicNavigationCaseReporter{
	public static final String VIEW_ID = "modify_header";


	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
			this.templatesLogic = templatesLogic;
	}
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
			this.external = external;
	}
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
	  this.settings = settings;
	}

	public String getViewID() {
		return VIEW_ID;
	}
	
	// Permissible since is a request-scope producer. Accessed from NavigationCases
	private Long templateId; 
	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
	    TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

	    String templateItemOTP = null;
	    String templateItemOTPBinding = null;
	    templateId = templateItemViewParams.templateId;
	    Long templateItemId = templateItemViewParams.templateItemId;

	    EvalTemplate template = templatesLogic.getTemplateById(templateId);
	    if (templateItemId != null) {
	      templateItemOTPBinding = "templateItemBeanLocator." + templateItemId;
	    }
	    else {
	      templateItemOTPBinding = "templateItemBeanLocator.new1";
	    }
	    templateItemOTP = templateItemOTPBinding + ".";
	    
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIOutput.make(tofill,"modify-header-title", messageLocator.getMessage("modifyheader.page.title"));
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title"));

		UIForm form = UIForm.make(tofill, "headerForm");

		UIOutput.make(form, "item-header", messageLocator.getMessage("modifyitem.item.header"));	
		UIOutput.make(form, "added-by-header", messageLocator.getMessage("modifyitem.added.by"));  //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form,"itemNo",null,"1.");
		UIOutput.make(form,"itemClassification",EvalConstants.ITEM_TYPE_HEADER);
		UIOutput.make(form, "userInfo",external.getUserDisplayName(template.getOwner()));
		
	    if (templateItemViewParams.templateItemId != null) {
	        UIBranchContainer showLink = UIBranchContainer.make(form,
	            "showRemoveLink:");
	        UIInternalLink.make(showLink, "remove_link", messageLocator
	            .getMessage("modifyitem.remove.link"), new SimpleViewParameters(
	            "remove_question")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    }
	    
		UIOutput.make(form, "question-text-header", messageLocator.getMessage("modifyitem.question.text.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,"item_text", templateItemOTP + "item.itemText");

		/*
		 * (non-javadoc)
		 * If the system setting (admin setting) for "EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY"
		 * is set as true then all items default to "Course". If it is set to false, then all items 
		 * default to "Instructor". If it is set to null then user is given the option to choose between
		 * "Course" and "Instructor".
		 */
		Boolean isDefaultCourse = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
		//Means show both options (course and instructor)
		if (isDefaultCourse == null) {
			
			UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:"); //$NON-NLS-1$
			UIOutput.make(showItemCategory, "item-category-header", messageLocator.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showItemCategory, "course-category-header", messageLocator.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showItemCategory, "instructor-category-header", messageLocator.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$

			//Radio Buttons for "Item Category"
			String[] courseCategoryList = 
			{
				messageLocator.getMessage("modifyitem.course.category.header"),
				messageLocator.getMessage("modifyitem.instructor.category.header"),
			};
			UISelect radios = UISelect.make(showItemCategory, "item_category", 
					EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList, 
					templateItemOTP + "itemCategory", null);
			String selectID = radios.getFullID();
			UISelectChoice.make(showItemCategory, "item_category_C", selectID, 0);
			UISelectChoice.make(showItemCategory, "item_category_I", selectID, 1);
		}
		//Default is course
		else if (isDefaultCourse.booleanValue()) {

			//Do not show on the page, just bind it explicitly.
			form.parameters.add(new UIELBinding(templateItemOTP + "itemCategory", EvaluationConstant.ITEM_CATEGORY_VALUES[0])); //$NON-NLS-1$
		}
		//Default is instructor
		else {

			//Do not show on the page, just bind it explicitly.
			form.parameters.add(new UIELBinding(templateItemOTP + "itemCategory", EvaluationConstant.ITEM_CATEGORY_VALUES[1])); //$NON-NLS-1$
		}

		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));
		
        UICommand saveCmd = UICommand.make(form, "saveHeaderAction", messageLocator
            .getMessage("modifyitem.save.button"), "#{itemsBean.saveItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       
        saveCmd.parameters.add(new UIELBinding(templateItemOTP
            + "item.classification", EvalConstants.ITEM_TYPE_HEADER));
        saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateItem}",
            new ELReference(templateItemOTPBinding)));
        saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateId}",
            templateId));

        /**    //TODO-Preview new/modified items
         *UICommand.make(form, "previewHeaderAction", messageLocator
         *    .getMessage("modifyitem.preview.button"),
         *    "#{itemsBean.previewItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		 */
	}

	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
	    i.add(new NavigationCase("success",
	            new EvalViewParameters(TemplateModifyProducer.VIEW_ID, templateId)));
	    i.add(new NavigationCase("cancel", 
	    		new EvalViewParameters(TemplateModifyProducer.VIEW_ID, templateId)));
		return i;
	}

	  public ViewParameters getViewParameters() {
		    return new TemplateItemViewParameters();
		  }
	
}
