/******************************************************************************
 * EvalItemsLogicImpl.java - created by aaronz@vt.edu on Jan 2, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


/**
 * Implementation for EvalItemsLogic
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalItemsLogicImpl implements EvalItemsLogic {

	private static Log log = LogFactory.getLog(EvalItemsLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}

	private EvalExternalLogic external;
	public void setExternalLogic(EvalExternalLogic external) {
		this.external = external;
	}


	// INIT method
	public void init() {
		log.debug("Init");
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getItemById(java.lang.Long)
	 */
	public EvalItem getItemById(Long itemId) {
		log.debug("itemId:" + itemId);
		return (EvalItem) dao.findById(EvalItem.class, itemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#saveItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.String)
	 */
	public void saveItem(EvalItem item, String userId) {
		log.debug("item:" + item.getId() + ", userId:" + userId);

		// set the date modified
		item.setLastModified( new Date() );

		// check on ITEM_TYPE and invalid combinations of item values depending on the type
		if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ) {
			if (item.getScale() == null) {
				throw new IllegalArgumentException("Item scale must be specified for scaled type items");
			} else if (item.getScaleDisplaySetting() == null) {
				throw new IllegalArgumentException("Item scale display setting must be specified for scaled type items");
			} else if (item.getBlockId() != null) {
				throw new IllegalArgumentException("Item blockid cannot be included for scaled type items");
			} else if (item.getDisplayRows() != null) {
				throw new IllegalArgumentException("Item displayRows cannot be included for scaled type items");
			} else if (item.getBlockParent() != null) {
				throw new IllegalArgumentException("Item blockParent cannot be included for scaled type items");
			}
		} else if ( EvalConstants.ITEM_TYPE_TEXT.equals(item.getClassification()) ) {
			if (item.getDisplayRows() == null) {
				throw new IllegalArgumentException("Item display rows must be specified for text type items");
			} else if (item.getBlockId() != null) {
				throw new IllegalArgumentException("Item blockid cannot be included for text type items");
			} else if (item.getScale() != null) {
				throw new IllegalArgumentException("Item scale cannot be included for text type items");
			} else if (item.getScaleDisplaySetting() != null) {
				throw new IllegalArgumentException("Item scale display setting cannot be included for text type items");
			} else if (item.getBlockParent() != null) {
				throw new IllegalArgumentException("Item blockParent cannot be included for text type items");
			}
		} else if ( EvalConstants.ITEM_TYPE_BLOCK.equals(item.getClassification()) ) {
			if (item.getBlockId() == null) {
				throw new IllegalArgumentException("Item blockid must be specified for block type items");
			} else if (item.getScale() == null) {
				throw new IllegalArgumentException("Item scale must be specified for block type items");
			} else if (item.getScaleDisplaySetting() != null) {
				throw new IllegalArgumentException("Item scale display setting cannot be included for block type items");
			} else if (item.getDisplayRows() != null) {
				throw new IllegalArgumentException("Item displayRows cannot be included for block type items");
			}
		} else if ( EvalConstants.ITEM_TYPE_HEADER.equals(item.getClassification()) ) {
			if (item.getBlockId() != null) {
				throw new IllegalArgumentException("Item blockid cannot be included for header type items");
			} else if (item.getScale() != null) {
				throw new IllegalArgumentException("Item scale cannot be included for header type items");
			} else if (item.getScaleDisplaySetting() != null) {
				throw new IllegalArgumentException("Item scale display setting cannot be included for header type items");
			} else if (item.getDisplayRows() != null) {
				throw new IllegalArgumentException("Item displayRows cannot be included for header type items");
			} else if (item.getBlockParent() != null) {
				throw new IllegalArgumentException("Item blockParent cannot be included for header type items");
			}			
		} else {
			throw new IllegalArgumentException("Invalid item classification specified ("+item.getClassification()+"), you must use the ITEM_TYPE constants to indicate classification");
		}

		// check the sharing constants
		if (! EvalUtils.checkSharingConstant(item.getSharing()) ||
				EvalConstants.SHARING_OWNER.equals(item.getSharing()) ) {
			throw new IllegalArgumentException("Invalid sharing constant ("+item.getSharing()+") set for item ("+item.getItemText()+")");
		} else if ( EvalConstants.SHARING_PUBLIC.equals(item.getSharing()) ) {
			// test if non-admin trying to set public sharing
			if (! external.isUserAdmin(userId) ) {
				throw new IllegalArgumentException("Only admins can set item ("+item.getItemText()+") sharing to public");
			}
		}

		if (item.getId() != null) {
			// existing item, don't allow change to locked setting
			EvalItem existingItem = (EvalItem) dao.findById(EvalItem.class, item.getId());
			if (existingItem == null) {
				throw new IllegalArgumentException("Cannot find item with id: " + item.getId());
			}

			if (! existingItem.getLocked().equals(item.getLocked())) {
				throw new IllegalArgumentException("Cannot change locked setting on existing item (" + item.getId() + ")");
			}
		}

		// fill in the locked setting with the default UNLOCKED
		if (item.getLocked() == null) {
			item.setLocked( Boolean.FALSE );
		}

		if (checkUserControlItem(userId, item)) {
			if (item.getLocked().booleanValue() == true) {
				// TODO - add logic to lock associated scales here
				log.error("TODO - Locking associated scales not implemented yet");
			}
			dao.save(item);
			log.info("User ("+userId+") saved item ("+item.getId()+"), title: " + item.getItemText());
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT save item ("+item.getId()+"), title: " + item.getItemText());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#deleteItem(java.lang.Long, java.lang.String)
	 */
	public void deleteItem(Long itemId, String userId) {
		log.debug("itemId:" + itemId + ", userId:" + userId);

		// get the item by id
		EvalItem item = (EvalItem) dao.findById(EvalItem.class, itemId);
		if (item == null) {
			throw new IllegalArgumentException("Cannot find item with id: " + itemId);
		}

		// cannot remove expert items
		if (item.getExpert().booleanValue() == true) {
			throw new IllegalStateException("Cannot remove expert item ("+itemId+")");
		}

		if (checkUserControlItem(userId, item)) {
			if (item.getLocked().booleanValue() == true) {
				// TODO - add logic to unlock associated scales here
				log.error("TODO - Unlocking locked items not implemented yet");
			}
			dao.delete(item);
			return;
		}
		
		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT delete item ("+item.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getItemsForUser(java.lang.String, java.lang.String)
	 */
	public List getItemsForUser(String userId, String sharingConstant) {
		log.debug("sharingConstant:" + sharingConstant + ", userId:" + userId);

		List l = new ArrayList();

		// get admin state
		boolean isAdmin = external.isUserAdmin(userId);

		boolean getPublic = false;
		boolean getPrivate = false;
		// check the sharingConstant param
		if (sharingConstant != null && 
				! EvalUtils.checkSharingConstant(sharingConstant)) {
			throw new IllegalArgumentException("Invalid sharing constant: " + sharingConstant);
		}
		if ( sharingConstant == null || 
				EvalConstants.SHARING_OWNER.equals(sharingConstant) ) {
			// return all items visible to this user
			getPublic = true;
			getPrivate = true;
		} else if ( EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ) {
			// return only private items visible to this user
			getPrivate = true;
		} else if ( EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ) {
			// return all public items
			getPublic = true;
		}

		// handle private sharing items
		if (getPrivate) {
			String[] props;
			Object[] values;
			if (isAdmin) {
				props = new String[] { "sharing" };
				values = new Object[] { EvalConstants.SHARING_PRIVATE };
			} else {
				props = new String[] { "sharing", "owner" };
				values = new Object[] { EvalConstants.SHARING_PRIVATE, userId };				
			}
			l.addAll( dao.findByProperties(EvalItem.class, props, values) );
		}

		// handle public sharing items
		if (getPublic) {
			l.addAll( dao.findByProperties(EvalItem.class, 
					new String[] { "sharing" }, 
					new Object[] { EvalConstants.SHARING_PUBLIC } ) );
		}

		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getItemsForTemplate(java.lang.Long, java.lang.String)
	 */
	public List getItemsForTemplate(Long templateId, String userId) {
		log.debug("templateId:" + templateId + ", userId:" + userId);

		List l = new ArrayList();
		for (Iterator iter = getTemplateItemsForTemplate(templateId, userId).iterator(); iter.hasNext();) {
			EvalTemplateItem eti = (EvalTemplateItem) iter.next();
			l.add(eti.getItem());
		}
		return l;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getTemplateItemById(java.lang.Long)
	 */
	public EvalTemplateItem getTemplateItemById(Long templateItemId) {
		log.debug("templateItemId:" + templateItemId);
		return (EvalTemplateItem) dao.findById(EvalTemplateItem.class, templateItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)
	 */
	public void saveTemplateItem(EvalTemplateItem templateItem, String userId) {
		log.debug("templateItem:" + templateItem.getId() + ", userId:" + userId);
		// TODO Auto-generated method stub

		// TODO Stub to save temporarily
		dao.save(templateItem);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#deleteTemplateItem(java.lang.Long, java.lang.String)
	 */
	public void deleteTemplateItem(Long templateItemId, String userId) {
		log.debug("templateItemId:" + templateItemId + ", userId:" + userId);
		// TODO Auto-generated method stub
		
		// TODO Stub to delete temporarily
		dao.delete(EvalTemplateItem.class, templateItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getTemplateItemsForTemplate(java.lang.Long)
	 */
	public List getTemplateItemsForTemplate(Long templateId, String userId) {
		log.debug("templateId:" + templateId);

		// check if the template is a valid one
		EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
		if (template == null) {
			throw new IllegalArgumentException("Cannot find template with id: " + templateId);
		}

		List l = new ArrayList();
		for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
			EvalTemplateItem eti = (EvalTemplateItem) iter.next();
			// TODO - check if this user can see this item (must be either taking a related eval or must somehow control the template)
			l.add(eti);
		}
		return l;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getNextBlockId()
	 */
	public Integer getNextBlockId() {
		return dao.getNextBlockId();
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#canControlItem(java.lang.String, java.lang.Long)
	 */
	public boolean canControlItem(String userId, Long itemId) {
		log.debug("itemId:" + itemId + ", userId:" + userId);
		// get the item by id
		EvalItem item = (EvalItem) dao.findById(EvalItem.class, itemId);
		if (item == null) {
			throw new IllegalArgumentException("Cannot find item with id: " + itemId);
		}

		// check perms and locked
		try {
			return checkUserControlItem(userId, item);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#canControlTemplateItem(java.lang.String, java.lang.Long)
	 */
	public boolean canControlTemplateItem(String userId, Long templateItemId) {
		log.debug("templateItemId:" + templateItemId + ", userId:" + userId);
		// get the template item by id
		EvalTemplateItem templateItem = (EvalTemplateItem) dao.findById(EvalTemplateItem.class, templateItemId);
		if (templateItem == null) {
			throw new IllegalArgumentException("Cannot find template item with id: " + templateItemId);
		}

		// check perms and locked
		try {
			return checkUserControlTemplateItem(userId, templateItem);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}


	// PRIVATE METHODS

	protected boolean checkUserControlItem(String userId, EvalItem item) {
		log.debug("item: " + item.getId() + ", userId: " + userId);
		// check locked first
		if (item.getId() != null &&
				item.getLocked().booleanValue() == true) {
			throw new IllegalStateException("Cannot control (modify) locked item ("+item.getId()+")");
		}

		// check ownership or super user
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( item.getOwner().equals(userId) ) {
			return true;
		} else {
			throw new SecurityException("User ("+userId+") cannot control item ("+item.getId()+") without permissions");
		}
	}

	protected boolean checkUserControlTemplateItem(String userId, EvalTemplateItem templateItem) {
		log.debug("templateItem: " + templateItem.getId() + ", userId: " + userId);
		// check locked first (expensive check)
		if (templateItem.getId() != null &&
				templateItem.getTemplate().getLocked().booleanValue() == true) {
			throw new IllegalStateException("Cannot control (modify) template item ("+
					templateItem.getId()+") in locked template ("+templateItem.getTemplate().getId()+")");
		}

		// check ownership or super user
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( templateItem.getOwner().equals(userId) ) {
			return true;
		} else {
			throw new SecurityException("User ("+userId+") cannot control template item ("+templateItem.getId()+") without permissions");
		}
	}

}
