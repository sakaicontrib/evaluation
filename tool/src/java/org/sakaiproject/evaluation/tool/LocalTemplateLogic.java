/******************************************************************************
 * LocalTemplateLogic.java - created by antranig on 23 Jan 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Aaron Zeckoski (aaronz@vt.edu)
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Local template local abstraction to allow for default values and central point of access for all things
 * related to creating items and templates
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class LocalTemplateLogic {

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}


	/*
	 * Real methods below
	 */


	// TEMPLATES

	public EvalTemplate fetchTemplate(Long templateId) {
		return templatesLogic.getTemplateById(templateId);
	}

	public void saveTemplate(EvalTemplate tosave) {
		templatesLogic.saveTemplate(tosave, external.getCurrentUserId());
	}

	public EvalTemplate newTemplate() {
		EvalTemplate currTemplate = new EvalTemplate(new Date(), 
				external.getCurrentUserId(), EvalConstants.TEMPLATE_TYPE_STANDARD, 
				null, "private", Boolean.FALSE);
		currTemplate.setDescription(""); // Note- somehow gives DataIntegrityViolation if null
		return currTemplate;
	}


	// TEMPLATE ITEMS

	public EvalTemplateItem fetchTemplateItem(Long itemId) {
		return itemsLogic.getTemplateItemById(itemId);
	}

	public List<EvalTemplateItem> fetchTemplateItems(Long templateId) {
		if (templateId == null) {
			return new ArrayList<EvalTemplateItem>();
		} else {
			return itemsLogic.getTemplateItemsForTemplate(templateId, new String[] {}, null, null);
		}
	}

	public void saveTemplateItem(EvalTemplateItem tosave) {
	    /* This is a temporary hack that is only good while we are only using TOP LEVEL and NODE LEVEL.
	         * Basically, we're putting everything in one combo box and this is a good way to check to see if
	         * it's the top node.  Otherwise the user selected a node id so it must be at the NODE LEVEL since
	         * we don't support the other levels yet.
	         */
	        if (tosave.getHierarchyNodeId() != null && !tosave.getHierarchyNodeId().equals("")
	                && !tosave.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
	            tosave.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_NODE);
	        }
	        else if (tosave.getHierarchyNodeId() != null && !tosave.getHierarchyNodeId().equals("")
	                && tosave.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
	            tosave.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
	        }
		itemsLogic.saveTemplateItem(tosave, external.getCurrentUserId());
	}

	/**
	 * Handles the removal of a templateItem, includes security check and 
	 * takes care of reordering or other items in the template<br/>
	 * Blocks: splits up the block and removes the parent item if a block parent is selected for removal
	 * 
	 * @param templateItemId a unique id of an {@link EvalTemplateItem}
	 */
	public void deleteTemplateItem(Long templateItemId) {
		String currentUserId = external.getCurrentUserId();
		if (! itemsLogic.canControlTemplateItem(currentUserId, templateItemId)) {
			throw new SecurityException("User ("+currentUserId+") cannot control this template item ("+templateItemId+")");
		}

		EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
		// get a list of all template items in this template
		List<EvalTemplateItem> allTemplateItems = itemsLogic.getTemplateItemsForTemplate(templateItem.getTemplate().getId(), null, null, null);
		// get the list of items without child items included
		List<EvalTemplateItem> noChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);

		// now remove the item and correct the display order
		int orderAdjust = 0;
		int removedItemDisplayOrder = 0;
		if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
			// remove the parent item and free up the child items into individual items if the block parent is removed
			removedItemDisplayOrder = templateItem.getDisplayOrder().intValue();
			List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
			orderAdjust = childList.size();

			// delete parent template item and item
			Long itemId = templateItem.getItem().getId();
			itemsLogic.deleteTemplateItem(templateItem.getId(), currentUserId);
			itemsLogic.deleteItem(itemId, currentUserId);

			// modify block children template items
			for (int i = 0; i < childList.size(); i++) {
				EvalTemplateItem child = (EvalTemplateItem) childList.get(i);
				child.setBlockParent(null);
				child.setBlockId(null);
				child.setDisplayOrder(new Integer(removedItemDisplayOrder + i));
				itemsLogic.saveTemplateItem(child, currentUserId);
			}

		} else { // non-block cases
			removedItemDisplayOrder = templateItem.getDisplayOrder().intValue();
			itemsLogic.deleteTemplateItem(templateItem.getId(), currentUserId);
		}

		// shift display-order of items below removed item
		for (int i = removedItemDisplayOrder; i < noChildList.size(); i++) {
			EvalTemplateItem ti = (EvalTemplateItem) noChildList.get(i);
			int order = ti.getDisplayOrder().intValue();
			if (order > removedItemDisplayOrder) {
				ti.setDisplayOrder(new Integer(order + orderAdjust - 1));
				itemsLogic.saveTemplateItem(ti, currentUserId);
			}
		}
	}

	public EvalTemplateItem newTemplateItem() {
		String level = EvalConstants.HIERARCHY_LEVEL_TOP;
		String nodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;

		// TODO - this should respect the current level the user is at

		EvalItem newItem = new EvalItem(new Date(), external.getCurrentUserId(), "", "",
				"", new Boolean(false));
		EvalTemplateItem newTemplateItem = new EvalTemplateItem( new Date(), 
				external.getCurrentUserId(), null, newItem, null, 
				EvaluationConstant.ITEM_CATEGORY_VALUES[0], level, nodeId);
		newTemplateItem.setUsesNA(new Boolean(false));
		return newTemplateItem;
	}


	// ITEMS

	public EvalItem fetchItem(Long itemId) {
		return itemsLogic.getItemById(itemId);
	}

	public void saveItem(EvalItem tosave) {
		// TODO - this should use the defunneler -AZ (so says antranig)
		// this is here to cleanup the fake scale in case it was not needed or load a real one
		if (tosave.getScale() != null) {
			if (tosave.getScale().getId() != null) {
				// this lookup is needed so hibernate can make the connection
				tosave.setScale(scalesLogic.getScaleById(tosave.getScale().getId()));
			} else {
				tosave.setScale(null);
			}
		}
		itemsLogic.saveItem(tosave, external.getCurrentUserId());
	}

	public void deleteItem(Long id) {
		itemsLogic.deleteItem(id, external.getCurrentUserId());
	}

	public EvalItem newItem() {
		EvalItem newItem = new EvalItem(new Date(), external.getCurrentUserId(), "", 
				EvalConstants.SHARING_PRIVATE, "", Boolean.FALSE);
		newItem.setCategory( EvalConstants.ITEM_CATEGORY_COURSE ); // default category
		newItem.setScale( new EvalScale() ); // needed so that EL reference will not fail
		return newItem;
	}


	// SCALES

	public EvalScale fetchScale(Long scaleId) {
		return scalesLogic.getScaleById(scaleId);
	}
  
	public void saveScale(EvalScale tosave) {
		scalesLogic.saveScale(tosave, external.getCurrentUserId());
	}
  
	public void deleteScale(Long id) {
		scalesLogic.deleteScale(id, external.getCurrentUserId());
	}
  
	public EvalScale newScale() {
		EvalScale currScale = new EvalScale(new Date(), 
				external.getCurrentUserId(), null, "private", Boolean.FALSE);
		currScale.setOptions(new String[]{"", ""});
		return currScale;
	}

}
