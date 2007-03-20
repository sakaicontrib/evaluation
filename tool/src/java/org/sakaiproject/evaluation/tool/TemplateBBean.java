/******************************************************************************
 * TemplateBBean.java - created on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng
 * Antranig Basman
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

/**
 * This request-scope bean handles template creation and modification.
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Antranig Basman
 * @author Will Humphries (whumphri@vt.edu)
 */

public class TemplateBBean {

	private static Log log = LogFactory.getLog(TemplateBBean.class);

	private TemplateBeanLocator templateBeanLocator;
	public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
		this.templateBeanLocator = templateBeanLocator;
	}

	private TemplateItemBeanLocator templateItemBeanLocator;
	public void setTemplateItemBeanLocator(TemplateItemBeanLocator templateItemBeanLocator) {
		this.templateItemBeanLocator = templateItemBeanLocator;
	}

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	public Long templateId;

	public Boolean idealColor;

	// public Long scaleId;
	public Integer originalDisplayOrder;

	public String childTemplateItemIds;

	/**
	 * If the template is not saved, button will show text "continue and add
	 * question" method binding to the "continue and add question" button on
	 * template_title_description.html replaces
	 * TemplateBean.createTemplateAction, but template is added to db here.
	 */
	public String createTemplateAction() {
		log.debug("create template");
		templateBeanLocator.saveAll();
		return "success";
	}

	/**
	 * If the template is already stored, button will show text "Save" method
	 * binding to the "Save" button on template_title_description.html replaces
	 * TemplateBean.saveTemplate()
	 */
	public String updateTemplateTitleDesc() {
		log.debug("update template title/desc");
		templateBeanLocator.saveAll();
		return "success";
	}
	
	private void emit(EvalTemplateItem toemit, int outindex) {
		log.debug("EvalTemplateItem toemit: " + toemit.getId() + ", outindex: " + outindex);
		toemit.setDisplayOrder(new Integer(outindex));
		localTemplateLogic.saveTemplateItem(toemit);
	}
	
	/**
	 * NB - this implementation depends on Hibernate reference equality
	 * semantics!! Guarantees output sequence is consecutive without duplicates,
	 * and will prefer honoring user sequence requests so long as they are not
	 * inconsistent.
	 */
	// TODO: This method needs to be invoked via a BeanGuard, trapping any
	// access to templateItemBeanLocator.*.displayOrder
	// Current Jquery implementation is only working as a result of auto-commit
	// bug in DAO wrapper implementation.
	public void saveReorder() { 
		log.info("save items reordering");
		Map delivered = templateItemBeanLocator.getDeliveredBeans();
		List l = itemsLogic.getTemplateItemsForTemplate(templateId, null, null);
		List ordered = TemplateItemUtils.getNonChildItems(l);
		for (int i = 1; i <= ordered.size();) {
			EvalTemplateItem item = (EvalTemplateItem) ordered.get(i - 1);
			int itnum = item.getDisplayOrder().intValue();
			if (i < ordered.size()) {
				EvalTemplateItem next = (EvalTemplateItem) ordered.get(i);
				int nextnum = next.getDisplayOrder().intValue();
				if (itnum == nextnum) {
					if (delivered.containsValue(item) ^ (itnum == i)) {
						emit(next, i++);
						emit(item, i++);
						continue;
					} else {
						emit(item, i++);
						emit(next, i++);
						continue;
					}
				}
			}
			emit(item, i++);
		}
	}

	/**
	 * Action to save a block type
	 * 
	 * @return
	 */
	public String saveBlockItemAction() {
		log.debug("Save Block items");

		String[] strIds = childTemplateItemIds.split(",");
		EvalTemplateItem parent = null;

		Map delivered = templateItemBeanLocator.getDeliveredBeans();
		if (strIds.length > 1) {// creating new Block case
			EvalTemplateItem first = itemsLogic.getTemplateItemById(Long.valueOf(strIds[0]));

			EvalTemplate template = first.getTemplate();
			List allTemplateItems = itemsLogic.getTemplateItemsForTemplate(template.getId(), null, null);

			if (TemplateItemUtils.getTemplateItemType(first).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
				// create new block from multiple existing block
				parent = (EvalTemplateItem) delivered.get(strIds[0]);
				if (parent == null)
					parent = first;
				setIdealColorforBlockParent(parent);
				parent.setDisplayOrder(originalDisplayOrder);
				parent.getItem().setUsesNA(parent.getUsesNA());
				parent.getItem().setCategory(parent.getItemCategory());
				localTemplateLogic.saveItem(parent.getItem());
				localTemplateLogic.saveTemplateItem(parent);

				Long parentId = parent.getId();
				int orderNo = TemplateItemUtils.getChildItems(allTemplateItems, parentId).size();
				// save child, delete other existing block parent
				for (int i = 1; i < strIds.length; i++) {
					EvalTemplateItem eti = itemsLogic.getTemplateItemById(Long.valueOf(strIds[i]));

					if (TemplateItemUtils.getTemplateItemType(eti).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
						List myChilds = TemplateItemUtils.getChildItems(allTemplateItems, eti.getId());
						for (int j = 0; j < myChilds.size(); j++) {
							EvalTemplateItem child = (EvalTemplateItem) myChilds.get(j);
							child.setBlockId(parentId);
							child.setDisplayOrder(new Integer(orderNo + 1));
							localTemplateLogic.saveTemplateItem(child);
							orderNo++;
						}
						localTemplateLogic.deleteTemplateItem(eti.getId()); // delete remaing block parent

					} else { // normal scale type
						if (eti.getBlockParent() != Boolean.FALSE)
							eti.setBlockParent(Boolean.FALSE);
						eti.setDisplayOrder(new Integer(orderNo + 1));
						eti.setBlockId(parentId);
						localTemplateLogic.saveTemplateItem(eti);
						orderNo++;
					}
				}

			} else {
				// create new block from a set of normal scaled type items
				parent = (EvalTemplateItem) delivered.get(TemplateItemBeanLocator.NEW_1);
				parent.setTemplate(template);
				parent.setDisplayOrder(originalDisplayOrder);
				parent.getItem().setScale(first.getItem().getScale());

				parent.setBlockParent(Boolean.TRUE);
				parent.getItem().setClassification( EvalConstants.ITEM_TYPE_BLOCK_PARENT );
				parent.getItem().setSharing(parent.getTemplate().getSharing());
				parent.getItem().setUsesNA(parent.getUsesNA());
				parent.getItem().setCategory(parent.getItemCategory());
				if (idealColor != null && idealColor == Boolean.TRUE) {
					parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
					parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
				} else {
					parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
					parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
				}
				// save Block parent
				localTemplateLogic.saveItem(parent.getItem());
				localTemplateLogic.saveTemplateItem(parent);

				// Save Block Child
				System.out.println("parentId=" + parent.getId());
				Long parentId = parent.getId();
				for (int i = 0; i < strIds.length; i++) {
					EvalTemplateItem child = itemsLogic.getTemplateItemById(Long.valueOf(strIds[i]));
					if (child.getBlockParent() != Boolean.FALSE)
						child.setBlockParent(Boolean.FALSE);
					child.setDisplayOrder(new Integer(i + 1));
					child.setBlockId(parentId);
					localTemplateLogic.saveTemplateItem(child);
				}

			}

			// shifting all the others's order
			allTemplateItems = itemsLogic.getTemplateItemsForTemplate(template.getId(), null, null);
			List noChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);
			for (int i = 0; i < noChildList.size(); i++) {
				EvalTemplateItem eti = (EvalTemplateItem) noChildList.get(i);
				// get parent's PO
				if (eti.getDisplayOrder().intValue() == originalDisplayOrder.intValue()
						&& eti.getId() != parent.getId()) {
					eti.setDisplayOrder(new Integer(originalDisplayOrder.intValue() + 1));
					localTemplateLogic.saveTemplateItem(eti);
				}
			}

			noChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);
			for (int i = 0; i < noChildList.size(); i++) {
				EvalTemplateItem eti = (EvalTemplateItem) noChildList.get(i);
				// System.out.println("item id="+ eti.getId().longValue()+";item
				// ["+i+"].order="+eti.getDisplayOrder().intValue());
				if (eti.getDisplayOrder().intValue() != (i + 1)) {
					eti.setDisplayOrder(new Integer(i + 1));
					localTemplateLogic.saveTemplateItem(eti);
				}
			}

		} else {// modify existing Block
			parent = (EvalTemplateItem) delivered.get(strIds[0]);
			if (parent != null) {
				setIdealColorforBlockParent(parent);
				parent.getItem().setUsesNA(parent.getUsesNA());
				parent.getItem().setCategory(parent.getItemCategory());

			} else {
				parent = itemsLogic.getTemplateItemById(Long.valueOf(strIds[0]));
				setIdealColorforBlockParent(parent);
			}
			localTemplateLogic.saveItem(parent.getItem());
			localTemplateLogic.saveTemplateItem(parent);

		}

		return "success";
	}

	private void setIdealColorforBlockParent(EvalTemplateItem eti) {

		if (idealColor != null) {// only reset when this feild is changed
			if (idealColor == Boolean.TRUE) {
				eti.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
				eti.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
			} else {
				eti.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
				eti.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
			}
		}
	}

}