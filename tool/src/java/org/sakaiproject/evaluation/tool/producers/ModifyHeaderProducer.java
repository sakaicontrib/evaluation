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

import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Page for Create, modify,preview, delete a Header type Item
 *
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyHeaderProducer implements ViewComponentProducer,NavigationCaseReporter{
	public static final String VIEW_ID = "modify_header";
	private TemplateBean templateBean;

	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIOutput.make(tofill,"modify-header-title", messageLocator.getMessage("modifyheader.page.title"));
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title"));

		UIForm form = UIForm.make(tofill, "headerForm");

		UIOutput.make(form, "item-header", messageLocator.getMessage("modifyitem.item.header"));	//TODO: exception: can not get property
		//UIOutput.make(form, "item-header","Item" );
		UIOutput.make(form, "added-by-header", messageLocator.getMessage("modifyitem.added.by"));  //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form,"itemNo",null,"#{templateBean.currItemNo}");
		UIOutput.make(form,"itemClassification",null,"#{templateBean.itemClassification}");
		UIOutput.make(form, "userInfo",null, "#{templateBean.userId}");

		if(templateBean.currentItem != null){
			UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
			UIInternalLink.make(showLink, "remove_link", messageLocator.getMessage("modifyitem.remove.link"), new SimpleViewParameters("remove_question"));
		}
		UIOutput.make(form, "question-text-header", messageLocator.getMessage("modifyitem.question.text.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,"item_text", "#{templateBean.itemText}");


		UIOutput.make(form, "item-category-header", messageLocator.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "course-category-header", messageLocator.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "instructor-category-header", messageLocator.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//Radio Buttons for "Item Category"
		UISelect radios = UISelect.make(form, "item_category", EvaluationConstant.ITEM_CATEGORY_VALUES,
				EvaluationConstant.ITEM_CATEGORY_VALUES_LABELS, "#{templateBean.itemCategory}",null); //Need to pull strings from properties file
		String selectID = radios.getFullID();
		UISelectChoice.make(form, "item_category_C", selectID, 0);
		UISelectChoice.make(form, "item_category_I", selectID, 1);

		UICommand.make(form, "cancelHeaderAction", messageLocator.getMessage("modifyitem.cancel.button"), "#{templateBean.cancelHeaderAction}");

		UICommand.make(form, "saveHeaderAction", messageLocator.getMessage("modifyitem.save.button"), "#{templateBean.saveHeaderAction}");

		UICommand.make(form, "previewHeaderAction", messageLocator.getMessage("modifyitem.preview.button"), "#{templateBean.previewHeaderAction}");

	}

	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));

		return i;
	}

}
