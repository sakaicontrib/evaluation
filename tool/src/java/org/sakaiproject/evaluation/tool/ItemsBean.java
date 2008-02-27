/**
 * ItemsBean.java - evaluation - Jan 16, 2007 11:35:56 AM - whumphri
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * This request-scope bean handles item creation and modification.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @deprecated This needs to be completely removed -AZ
 */
public class ItemsBean {

    /*
     * VARIABLE DECLARATIONS
     */
    private static Log log = LogFactory.getLog(ItemsBean.class);

    /**
     * Point to the value of dropdown list (Scale/Survey,Question
     * Block,TextHeader,etc) on template_modify.html and also point to the top
     * label field on tempalte_item.html
     */

    public EvalTemplateItem templateItem;

    // The following fields below belong to template_item.html
    public Long scaleId;
    private List scaleValues; // "Scale Type" drop down list
    private List scaleLabels; // "Scale Type" drop down list

    // The following fields below belong to modify_block.html
    public Boolean idealColor;
    public List queList;
    public String currQueNo;
    public String currRowNo;
    public String classification;
    public Long templateId;

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
       this.authoringService = authoringService;
    }



    public ItemsBean() {
        templateItem = new EvalTemplateItem();
        templateItem.setItem(new EvalItem());
        templateItem.setTemplate(new EvalTemplate());
    }


    /*
     * INITIALIZATION
     */
    public void init() {
        log.debug("INIT");
    }

    // GETTERS and SETTERS
    /**
     * @deprecated
     */
    public List getScaleValues() {
        // get scale values and labels from DAO
        List list = null;
        String scaleOptionsStr = "";
        this.scaleValues = new ArrayList();
        this.scaleLabels = new ArrayList();

        list = authoringService.getScalesForUser(external.getCurrentUserId(), null);// logic.getScales(Boolean.FALSE);
        for (int count = 0; count < list.size(); count++) {
            scaleOptionsStr = "";
            String[] scaleOptionsArr = ((EvalScale) list.get(count)).getOptions();
            for (int innerCount = 0; innerCount < scaleOptionsArr.length; innerCount++) {

                if (scaleOptionsStr == "")
                    scaleOptionsStr = scaleOptionsArr[innerCount];
                else
                    scaleOptionsStr = scaleOptionsStr + ", " + scaleOptionsArr[innerCount];

            } // end of inner for

            EvalScale sl = (EvalScale) list.get(count);
            this.scaleValues.add((sl.getId()).toString());
            this.scaleLabels.add(scaleOptionsArr.length + " pt - " + sl.getTitle() + " (" + scaleOptionsStr + ")");

        } // end of outer loop
        return scaleValues;
    }

    /**
     * @deprecated
     */
    public void setScaleValues(List scaleValues) {
        this.scaleValues = scaleValues;
    }

    /**
     * @deprecated
     */
    public List getScaleLabels() {
        // make sure if getScaleValue() was called first, getScaleLabels() will not be called again
        if (scaleLabels == null)
            getScaleValues();

        return scaleLabels;
    }

    /**
     * @deprecated
     */
    public void setScaleLabels(List scaleLabels) {
        this.scaleLabels = scaleLabels;
    }


    // ACTION METHODS

    public String cancelItemAction() {
        return "cancel";
    }

    public String previewItemAction() {
        return null;
    }

    public String cancelRemoveAction() {
        return null;
    }

    /**
     * @deprecated
     */
    public String saveItemAction() {
        templateItem.getItem().setScaleDisplaySetting(templateItem.getScaleDisplaySetting());
        templateItem.getItem().setUsesNA(templateItem.getUsesNA());
        templateItem.setTemplate(authoringService.getTemplateById(templateId));
        templateItem.getItem().setSharing(templateItem.getTemplate().getSharing());
        templateItem.getItem().setCategory(templateItem.getCategory());
        if (scaleId != null)
            templateItem.getItem().setScale(authoringService.getScaleById(scaleId));
        /* This is a temporary hack that is only good while we are only using TOP LEVEL and NODE LEVEL.
         * Basically, we're putting everything in one combo box and this is a good way to check to see if
         * it's the top node.  Otherwise the user selected a node id so it must be at the NODE LEVEL since
         * we don't support the other levels yet.
         */
        if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
                && !templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_NODE);
        }
        else if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
                && templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
        }
        authoringService.saveItem(templateItem.getItem(), external.getCurrentUserId());
        authoringService.saveTemplateItem(templateItem, external.getCurrentUserId());
        return "success";
    }

    /**
     * @deprecated
     */
    public void newItemInit(Long templateId, String classification) {
        templateItem.getItem().setClassification(classification);
        this.templateId = templateId;
    }

    /**
     * @deprecated
     */
    public void fetchTemplateItem(Long templateItemId) {
        templateItem = authoringService.getTemplateItemById(templateItemId);
    }

}