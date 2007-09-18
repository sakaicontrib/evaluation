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
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.utils.HierarchyRenderUtil;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.ELReference;
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
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
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
 * @author Rui Feng (fengr@vt.edu)
 */

public class ModifyHeaderProducer implements ViewComponentProducer,
ViewParamsReporter, NavigationCaseReporter, DynamicNavigationCaseReporter {
    public static final String VIEW_ID = "modify_header";


    private EvalTemplatesLogic templatesLogic;
    public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
        this.templatesLogic = templatesLogic;
    }

    private EvalItemsLogic itemsLogic;
    public void setItemsLogic(EvalItemsLogic itemsLogic) {
        this.itemsLogic = itemsLogic;
    }

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private TextInputEvolver richTextEvolver;
    public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
        this.richTextEvolver = richTextEvolver;
    }

    private HierarchyRenderUtil hierUtil;
    public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
        hierUtil = util;
    }

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {
        TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

        String templateItemOTP = null;
        String templateItemOTPBinding = null;
        Long templateId = templateItemViewParams.templateId;
        Long templateItemId = templateItemViewParams.templateItemId;

        EvalTemplate template = templatesLogic.getTemplateById(templateId);
        if (templateItemId != null) {
            templateItemOTPBinding = "templateItemWBL." + templateItemId;
        } else {
            templateItemOTPBinding = "templateItemWBL.new1";
        }
        templateItemOTP = templateItemOTPBinding + ".";

        UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        UIMessage.make(tofill, "modify-header-title", "modifyheader.page.title");
        UIMessage.make(tofill, "create-eval-title", "starteval.page.title");

        UIForm form = UIForm.make(tofill, "headerForm");

        UIMessage.make(form, "item-header", "modifyitem.item.header");
        UIMessage.make(form,"added-by-header", "modifyitem.added.by"); //$NON-NLS-1$ //$NON-NLS-2$

        if(templateItemId != null){			

            EvalTemplateItem ti = itemsLogic.getTemplateItemById(templateItemId);
            UIOutput.make(form, "itemNo",ti.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$

        }else{
            List<EvalTemplateItem> l = itemsLogic.getTemplateItemsForTemplate(templateId, null, null, null);
            List<EvalTemplateItem> templateItemsList = TemplateItemUtils.getNonChildItems(l);			
            Integer no = new Integer(templateItemsList.size()+1);
            UIOutput.make(form, "itemNo",no.toString());
        }


        UIOutput.make(form, "itemClassification", EvalConstants.ITEM_TYPE_HEADER);
        UIOutput.make(form, "userInfo", external.getUserDisplayName(template
                .getOwner()));

        if (templateItemId != null) {
            UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
            UIInternalLink.make(showLink, "remove_link", UIMessage.make("modifyitem.remove.link"), 
                    new TemplateItemViewParameters(RemoveItemProducer.VIEW_ID,
                            templateId, templateItemId));
        }

        UIMessage.make(form, "question-text-header", "modifyitem.question.text.header"); //$NON-NLS-1$ //$NON-NLS-2$
        UIInput itemText = UIInput.make(form, "item_text:", templateItemOTP + "item.itemText");
        richTextEvolver.evolveTextInput(itemText);

        /*
         * (non-javadoc) If the system setting (admin setting) for
         * "EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY" is set as true then all
         * items default to "Course". If it is set to false, then all items default
         * to "Instructor". If it is set to null then user is given the option to
         * choose between "Course" and "Instructor".
         */
        Boolean isDefaultCourse = (Boolean) settings
        .get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
        // Means show both options (course and instructor)
        if (isDefaultCourse == null) {

            UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:"); //$NON-NLS-1$
            UIMessage.make(showItemCategory, "item-category-header","modifyitem.item.category.header");
            UIMessage.make(showItemCategory, "course-category-header","modifyitem.course.category.header");
            UIMessage.make(showItemCategory, "instructor-category-header",
            "modifyitem.instructor.category.header");

            // Radio Buttons for "Item Category"
            String[] courseCategoryList = {
                    "modifyitem.course.category.header",
            "modifyitem.instructor.category.header" };
            UISelect radios = UISelect.make(showItemCategory, "item_category",
                    EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
                    templateItemOTP + "itemCategory", null);
            String selectID = radios.getFullID();
            UISelectChoice.make(showItemCategory, "item_category_C", selectID, 0);
            UISelectChoice.make(showItemCategory, "item_category_I", selectID, 1);
        }
        else {
            // Course category if default, instructor otherwise
            //  Do not show on the page, just bind it explicitly.
            form.parameters.add(new UIELBinding(templateItemOTP + "itemCategory", 
                    EvaluationConstant.ITEM_CATEGORY_VALUES[isDefaultCourse.booleanValue()? 0: 1])); //$NON-NLS-1$
        }
        
        /* Dropdown option for selecting Hierarchy Node */
        Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        if (showHierarchyOptions.booleanValue() == true) {
            UIBranchContainer hierarchyOptions = UIBranchContainer.make(form, "showItemHierarchyNodeSelection:");
            UIMessage.make(form, "item-hierarchy-assign-header", "modifyitem.hierarchy.assign.header");
            hierUtil.makeHierSelect(form, "hierarchyNodeSelect", templateItemOTP + "hierarchyNodeId");
        }

        UIMessage.make(form, "cancel-button","general.cancel.button");

        UICommand saveCmd = UICommand.make(form, "saveHeaderAction", UIMessage.make("modifyitem.save.button"),
        "#{itemsBean.saveItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        saveCmd.parameters.add(new UIELBinding(templateItemOTP
                + "item.classification", EvalConstants.ITEM_TYPE_HEADER));
        saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateItem}",
                new ELReference(templateItemOTPBinding)));
        saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateId}",
                templateId));

        /**
         * //TODO-Preview new/modified items UICommand.make(form,
         * "previewHeaderAction", messageLocator
         * .getMessage("modifyitem.preview.button"),
         * "#{itemsBean.previewItemAction}"); 
         * 
         */
    }

    public List reportNavigationCases() {
        List i = new ArrayList();

        i.add(new NavigationCase(PreviewItemProducer.VIEW_ID,
                new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
        i.add(new NavigationCase("success", new TemplateViewParameters(
                ModifyTemplateItemsProducer.VIEW_ID, null)));
        i.add(new NavigationCase("cancel", new TemplateViewParameters(
                ModifyTemplateItemsProducer.VIEW_ID, null)));
        return i;
    }

    public ViewParameters getViewParameters() {
        return new TemplateItemViewParameters();
    }

}
