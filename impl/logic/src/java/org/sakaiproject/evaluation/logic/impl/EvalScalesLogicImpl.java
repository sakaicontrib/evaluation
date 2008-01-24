/******************************************************************************
 * EvalScalesLogicImpl.java - created by aaronz@vt.edu on Dec 29, 2006
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
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


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
		EvalScale scale = (EvalScale) dao.findById(EvalScale.class, scaleId);
      // check for non-null type
      if (scale != null && scale.getMode() == null) {
         // set this to the default then
         scale.setMode(EvalConstants.SCALE_MODE_SCALE);
         saveScale(scale, scale.getOwner());
      }
      return scale;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#getScaleByEid(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
   public EvalScale getScaleByEid(String eid) {
      log.debug("scale eid: " + eid);
      EvalScale evalScale = null;
      if (eid != null) {
         List<EvalScale> evalScales = dao.findByProperties(EvalScale.class,
               new String[] { "eid" }, new Object[] { eid });
         if (evalScales != null && evalScales.size() == 1)
            evalScale = evalScales.get(0);
      }
      return evalScale;
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

		// check for non-null values which can be inferred
      if (scale.getMode() == null) {
         // set this to the default then
         scale.setMode(EvalConstants.SCALE_MODE_SCALE);
      }

      if (scale.getSharing() == null) {
         scale.setSharing(EvalConstants.SHARING_PRIVATE);
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

			// TODO - this does not work -AZ
			EvalScale existingScale = (EvalScale) dao.findById(EvalScale.class, scale.getId());
			if (scale == null) {
				throw new IllegalArgumentException("Cannot find scale with id: " + scale.getId());
			}

			if (! existingScale.getLocked().equals(scale.getLocked())) {
				throw new IllegalArgumentException("Cannot change locked setting on existing scale (" + scale.getId() + ")");
			}
		}

		// fill in any default values and nulls here
		if (scale.getLocked() == null) {
			scale.setLocked( Boolean.FALSE );
		}

		// replace adhoc default title with a unique title
		if (EvalConstants.SCALE_ADHOC_DEFAULT_TITLE.equals(scale.getTitle())) {
		   scale.setTitle("adhoc-" + EvalUtils.makeUniqueIdentifier(100));
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

		// ADMIN CAN REMOVE EXPERT SCALES -AZ
//		// cannot remove expert scales
//		if (scale.getExpert().booleanValue()) {
//			throw new IllegalStateException("Cannot remove expert scale: " + scaleId);
//		}

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
	@SuppressWarnings("unchecked")
   public List<EvalScale> getScalesForUser(String userId, String sharingConstant) {
		log.debug("userId: " + userId + ", sharingConstant: " + sharingConstant );

		List<EvalScale> l = new ArrayList<EvalScale>();

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
			int[] comps;
			if (isAdmin) {
				props = new String[] { "mode", "sharing" };
				values = new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE };
				comps = new int[] { ByPropsFinder.EQUALS , ByPropsFinder.EQUALS };
			} else {
				props = new String[] { "mode", "sharing", "owner" };
				values = new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, userId };				
				comps = new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS, ByPropsFinder.EQUALS };
			}
			l.addAll( dao.findByProperties(EvalScale.class, 
					props, 
					values,
					comps,
					new String[] {"title"}) );
		}

		// handle public sharing items
		if (getPublic) {
			l.addAll( dao.findByProperties(EvalScale.class, 
					new String[] { "mode", "sharing" }, 
					new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PUBLIC },
					new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS },
					new String[] {"title"}) );
		}

		return l;
	}


	// PERMISSIONS


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#canModifyScale(java.lang.String, java.lang.Long)
	 */
	public boolean canModifyScale(String userId, Long scaleId) {
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


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#canRemoveScale(java.lang.String, java.lang.Long)
	 */
	public boolean canRemoveScale(String userId, Long scaleId) {
		log.debug("userId: " + userId + ", scaleId: " + scaleId );
		// get the scale by id
		EvalScale scale = (EvalScale) dao.findById(EvalScale.class, scaleId);
		if (scale == null) {
			throw new IllegalArgumentException("Cannot find scale with id: " + scaleId);
		}

		// cannot remove scales that are in use
		if (dao.isUsedScale(scaleId)) {
			log.debug("Cannot remove scale ("+scaleId+") which is used in at least one item");
			return false;
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
