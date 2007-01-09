/******************************************************************************
 * EvalScalesLogicImpl.java - created by aaronz@vt.edu on Dec 29, 2006
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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;


/**
 * Implementation for EvalScalesLogic
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalScalesLogicImpl implements EvalScalesLogic {

	private static Log log = LogFactory.getLog(EvalScalesLogicImpl.class);

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
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#getScaleById(java.lang.Long)
	 */
	public EvalScale getScaleById(Long scaleId) {
		log.debug("scaleId: " + scaleId );
		// get the scale by passing in id
		return (EvalScale) dao.findById(EvalScale.class, scaleId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#saveScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.String)
	 */
	public void saveScale(EvalScale scale, String userId) {
		log.debug("userId: " + userId + ", scale: " + scale.getTitle());

		// set the date modified
		scale.setLastModified( new Date() );

		// check for null or length 0 or 1 options
		if (scale.getOptions() == null ||
				scale.getOptions().length <= 1) {
			throw new IllegalArgumentException("Scale options cannot be null and must have at least 2 items");
		}

		// check the sharing constants
		if (! EvalUtils.checkSharingConstant(scale.getSharing()) ||
				EvalConstants.SHARING_OWNER.equals(scale.getSharing()) ) {
			throw new IllegalArgumentException("Invalid sharing constant ("+scale.getSharing()+") set for scale ("+scale.getTitle()+")");
		} else if ( EvalConstants.SHARING_PUBLIC.equals(scale.getSharing()) ) {
			// test if non-admin trying to set public sharing
			if (! external.isUserAdmin(userId) ) {
				throw new IllegalArgumentException("Only admins can set scale ("+scale.getTitle()+") sharing to public");
			}
		}

		// check locking not changed
		if (scale.getId() != null) {
			// existing scale, don't allow change to locked setting
			EvalScale existingScale = (EvalScale) dao.findById(EvalScale.class, scale.getId());
			if (scale == null) {
				throw new IllegalArgumentException("Cannot find scale with id: " + scale.getId());
			}

			if (! existingScale.getLocked().equals(scale.getLocked())) {
				throw new IllegalArgumentException("Cannot change locked setting on existing scale (" + scale.getId() + ")");
			}
		}

		// TODO - fill in any default values and nulls here
		if (scale.getLocked() == null) {
			scale.setLocked( Boolean.FALSE );
		}

		// check perms and save
		if (checkUserControlScale(userId, scale)) {
			dao.save(scale);
			log.info("User ("+userId+") saved scale ("+scale.getId()+"), title: " + scale.getTitle());
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT save scale ("+scale.getId()+"), title: " + scale.getTitle());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#deleteScale(java.lang.Long, java.lang.String)
	 */
	public void deleteScale(Long scaleId, String userId) {
		log.debug("userId: " + userId + ", scaleId: " + scaleId );
		// get the scale by id
		EvalScale scale = (EvalScale) dao.findById(EvalScale.class, scaleId);
		if (scale == null) {
			throw new IllegalArgumentException("Cannot find scale with id: " + scaleId);
		}

		// check perms and remove
		if (checkUserControlScale(userId, scale)) {
			dao.delete(scale);
			log.info("User ("+userId+") deleted scale ("+scale.getId()+"), title: " + scale.getTitle());
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT delete scale ("+scale.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#getScalesForUser(java.lang.String, java.lang.String)
	 */
	public List getScalesForUser(String userId, String sharingConstant) {
		log.debug("userId: " + userId + ", sharingConstant: " + sharingConstant );

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
			// return all scales visible to this user
			getPublic = true;
			getPrivate = true;
		} else if ( EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ) {
			// return only private scales visible to this user
			getPrivate = true;
		} else if ( EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ) {
			// return all public scales
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
			l.addAll( dao.findByProperties(EvalScale.class, props, values) );
		}

		// handle public sharing items
		if (getPublic) {
			l.addAll( dao.findByProperties(EvalScale.class, 
					new String[] { "sharing" }, 
					new Object[] { EvalConstants.SHARING_PUBLIC } ) );
		}

		return l;
	}


	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#canControlScale(java.lang.String, java.lang.Long)
	 */
	public boolean canControlScale(String userId, Long scaleId) {
		log.debug("userId: " + userId + ", scaleId: " + scaleId );
		// get the scale by id
		EvalScale scale = (EvalScale) dao.findById(EvalScale.class, scaleId);
		if (scale == null) {
			throw new IllegalArgumentException("Cannot find scale with id: " + scaleId);
		}

		// check perms and locked
		try {
			return checkUserControlScale(userId, scale);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}


	// INTERNAL METHODS

	/**
	 * Check if user has permissions to control (remove or update) this scale
	 * @param userId
	 * @param scale
	 * @return true if they do, exception otherwise
	 */
	protected boolean checkUserControlScale(String userId, EvalScale scale) {
		// check locked first
		if (scale.getId() != null &&
				scale.getLocked().booleanValue() == true) {
			throw new IllegalStateException("Cannot control locked scale ("+scale.getId()+")");
		}

		// check ownership or super user
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( scale.getOwner().equals(userId) ) {
			return true;
		} else {
			throw new SecurityException("User ("+userId+") cannot control scale ("+scale.getId()+") without permissions");
		}
	}

}
