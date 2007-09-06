/******************************************************************************
 * ExpertItemsBean.java - created by aaronz on 9 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * The backing bean for expert items adding
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemsBean {

	private static Log log = LogFactory.getLog(ExpertItemsBean.class);

	public Map selectedIds = new HashMap();
	public Long templateId;

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	public void init() {
		log.debug("init");
	}

	public ExpertItemsBean() {
		log.debug("constructor");
	}

	/**
	 * Creates templateItems to add items to a template
	 * @return the status location
	 */
	public String processActionAddItems() {
		log.debug("in process action add items, selectedItems=" + selectedIds.size());

		String currentUserId = external.getCurrentUserId();
		String level = EvalConstants.HIERARCHY_LEVEL_TOP;
		String nodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;

		EvalTemplate template = templatesLogic.getTemplateById(templateId);
		if (EvalConstants.TEMPLATE_TYPE_ADDED.equals( template.getType() )) {
			// TODO change the level and node based on current settings
			level = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
			nodeId = currentUserId;
		}

		for (Iterator iter = selectedIds.keySet().iterator(); iter.hasNext(); ) {
			Long itemId = new Long((String) iter.next());
			EvalItem item = itemsLogic.getItemById(itemId);
			if (item == null) {
				log.error("Invalid item id: " + itemId);
				continue;
			}
			log.debug("Checking to add item:" + itemId);
			if (selectedIds.get(itemId.toString()) == Boolean.TRUE) {
				EvalTemplateItem templateItem = 
					new EvalTemplateItem(new Date(), currentUserId,	
							template, item, null, null, level, nodeId);
				itemsLogic.saveTemplateItem(templateItem, currentUserId);
				log.info("Added new item (" + item.getId() + ") to template (" + template.getId() + ") via templateItem (" + templateItem.getId() + ")");
			}
		}

		return "success";
	}

}
