/**
 * ExpertItemsBean.java - evaluation - 9 Mar 2007 11:35:56 AM - azeckoski
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

/**
 * The backing bean for expert and existing items adding
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemsBean {

    private static Log log = LogFactory.getLog(ExpertItemsBean.class);

    public Map<String, Boolean> selectedIds = new HashMap<String, Boolean>();
    public Long templateId;

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    public ExpertItemsBean() { }

    /**
     * Creates templateItems to add items to a template
     * @return the status location
     */
    public String processActionAddItems() {
        log.debug("in process action add items, selectedItems=" + selectedIds.size());

        String currentUserId = commonLogic.getCurrentUserId();
        String hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_TOP;
        String hierarchyNodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;

        EvalTemplate template = authoringService.getTemplateById(templateId);
        if (EvalConstants.TEMPLATE_TYPE_ADDED.equals( template.getType() )) {
            // TODO change the level and node based on current settings
            hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
            hierarchyNodeId = currentUserId;
        }

        for (Iterator<String> iter = selectedIds.keySet().iterator(); iter.hasNext(); ) {
            Long itemId = new Long(iter.next());
            EvalItem item = authoringService.getItemById(itemId);
            if (item == null) {
                log.error("Invalid item id: " + itemId);
                continue;
            }
            log.debug("Checking to add item:" + itemId);
            if (selectedIds.get(itemId.toString()) == Boolean.TRUE) {
                // make the template item based on the item and default settings
                EvalTemplateItem templateItem = TemplateItemUtils.makeTemplateItem(item);
                templateItem.setOwner(currentUserId);
                templateItem.setTemplate(template);
                templateItem.setHierarchyLevel(hierarchyLevel);
                templateItem.setHierarchyNodeId(hierarchyNodeId);
                // save the template item
                authoringService.saveTemplateItem(templateItem, currentUserId);
                log.info("Added new item (" + item.getId() + ") to template (" + template.getId() + ") via templateItem (" + templateItem.getId() + ")");
            }
        }

        return "success";
    }

}
