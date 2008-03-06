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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * The backing bean for expert items adding
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemsBean {

	private static Log log = LogFactory.getLog(ExpertItemsBean.class);

	public Map<String, Boolean> selectedIds = new HashMap<String, Boolean>();
	public Long templateId;

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
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

		EvalTemplate template = authoringService.getTemplateById(templateId);
		if (EvalConstants.TEMPLATE_TYPE_ADDED.equals( template.getType() )) {
			// TODO change the level and node based on current settings
			level = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
			nodeId = currentUserId;
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
				EvalTemplateItem templateItem = 
					new EvalTemplateItem(new Date(), currentUserId,	
							template, item, null, null, level, nodeId);
				authoringService.saveTemplateItem(templateItem, currentUserId);
				log.info("Added new item (" + item.getId() + ") to template (" + template.getId() + ") via templateItem (" + templateItem.getId() + ")");
			}
		}

		return "success";
	}

}
