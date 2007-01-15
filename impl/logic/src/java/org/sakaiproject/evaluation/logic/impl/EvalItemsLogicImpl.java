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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#deleteItem(java.lang.Long, java.lang.String)
	 */
	public void deleteItem(Long itemId, String userId) {
		log.debug("itemId:" + itemId + ", userId:" + userId);
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getItemsForUser(java.lang.String, java.lang.String)
	 */
	public List getItemsForUser(String userId, String sharingConstant) {
		log.debug("sharingConstant:" + sharingConstant + ", userId:" + userId);
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getItemsForTemplate(java.lang.Long, java.lang.String)
	 */
	public List getItemsForTemplate(Long templateId, String userId) {
		log.debug("templateId:" + templateId + ", userId:" + userId);

		// check if the template is a valid one
		EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
		if (template == null) {
			throw new IllegalArgumentException("Cannot find template with id: " + templateId);
		}

		List l = new ArrayList();
		for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
			EvalTemplateItem eti = (EvalTemplateItem) iter.next();
			// TODO - check if this user can see this item (must be either taking a related eval or must somehow control the template)
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
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#deleteTemplateItem(java.lang.Long, java.lang.String)
	 */
	public void deleteTemplateItem(Long templateItemId, String userId) {
		log.debug("templateItemId:" + templateItemId + ", userId:" + userId);
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getTemplateItemsForTemplate(java.lang.Long)
	 */
	public List getTemplateItemsForTemplate(Long templateId) {
		log.debug("templateId:" + templateId);
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalItemsLogic#getNextBlockId()
	 */
	public Integer getNextBlockId() {
		// TODO Auto-generated method stub
		return null;
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
