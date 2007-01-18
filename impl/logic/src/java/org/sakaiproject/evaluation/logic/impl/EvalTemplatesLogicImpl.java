/******************************************************************************
 * EvalTemplatesLogicImpl.java - created by aaronz@vt.edu on Dec 31, 2006
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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;


/**
 * Implementation for EvalTemplatesLogic
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalTemplatesLogicImpl implements EvalTemplatesLogic {

	private static Log log = LogFactory.getLog(EvalTemplatesLogicImpl.class);

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
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#getTemplateById(java.lang.Long)
	 */
	public EvalTemplate getTemplateById(Long templateId) {
		log.debug("templateId: " + templateId);
		// get the template by id
		return (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#saveTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.String)
	 */
	public void saveTemplate(EvalTemplate template, String userId) {
		log.debug("template: " + template.getTitle() + ", userId: " + userId);

		// set the date modified
		template.setLastModified( new Date() );

		// check the sharing constants
		if (! EvalUtils.checkSharingConstant(template.getSharing()) ||
				EvalConstants.SHARING_OWNER.equals(template.getSharing()) ) {
			throw new IllegalArgumentException("Invalid sharing constant ("+template.getSharing()+") set for template ("+template.getTitle()+")");
		} else if ( EvalConstants.SHARING_PUBLIC.equals(template.getSharing()) ) {
			// test if non-admin trying to set public sharing
			if (! external.isUserAdmin(userId) ) {
				throw new IllegalArgumentException("Only admins can set template ("+template.getTitle()+") sharing to public");
			}
		}

		if (template.getId() == null) {
			// new template
			if (! canCreateTemplate(userId)) {
				throw new SecurityException("User ("+userId+") cannot create templates, invalid permissions");
			}
		} else {
			// existing template, don't allow change to locked setting
			EvalTemplate existingTemplate = (EvalTemplate) dao.findById(EvalTemplate.class, template.getId());
			if (existingTemplate == null) {
				throw new IllegalArgumentException("Cannot find template with id: " + template.getId());
			}

			if (! existingTemplate.getLocked().equals(template.getLocked())) {
				throw new IllegalArgumentException("Cannot change locked setting on existing template (" + template.getId() + ")");
			}
		}

		// TODO - fill in any default values and nulls here
		if (template.getLocked() == null) {
			template.setLocked( Boolean.FALSE );
		}

		if (checkUserControlTemplate(userId, template)) {
			if (template.getLocked().booleanValue() == true) {
				// TODO - add logic to lock associated items here
				log.error("TODO - Locking associated items not implemented yet");
			}
			dao.save(template);
			log.info("User ("+userId+") saved template ("+template.getId()+"), title: " + template.getTitle());
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT save template ("+template.getId()+"), title: " + template.getTitle());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#deleteTemplate(java.lang.Long, java.lang.String)
	 */
	public void deleteTemplate(Long templateId, String userId) {
		log.debug("templateId: " + templateId + ", userId: " + userId);
		// get the template by id
		EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
		if (template == null) {
			throw new IllegalArgumentException("Cannot find template with id: " + templateId);
		}

		// cannot remove expert templates
		if (template.getExpert().booleanValue() == true) {
			throw new IllegalStateException("Cannot remove expert template ("+templateId+")");
		}

		if (checkUserControlTemplate(userId, template)) {
			if (template.getLocked().booleanValue() == true) {
				// TODO - add logic to unlock associated items here
				log.error("TODO - Unlocking locked items not implemented yet");
			}

			// remove all associated templateItems also (should disassociate all items automatically)
			if ( template.getTemplateItems().size() > 0 ) {
				Set[] entitySets = new Set[2];

				entitySets[0] = template.getTemplateItems();

				Set templateSet = new HashSet();
				templateSet.add( template );
				entitySets[1] = templateSet;

				// remove the template and related templateItems in one transaction
				dao.deleteMixedSet(entitySets);
			} else { 
				dao.delete(template);
			}
			return;
		}
		
		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT delete template ("+template.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#getTemplatesForUser(java.lang.String, java.lang.String, boolean)
	 */
	public List getTemplatesForUser(String userId, String sharingConstant, boolean includeEmpty) {
		log.debug("sharingConstant: " + sharingConstant + ", userId: " + userId);
		/**
		 * TODO - Hierarchy
		 * visible and shared sharing methods are meant to work by relating the hierarchy level of 
		 * the owner with the sharing setting in the template, however, that was when 
		 * we assumed there would only be one level per user. That is no longer anything 
		 * we have control over (since we depend on data that comes from another API) 
		 * so we will have to add in a table which will track the hierarchy levels and
		 * link them to the template. This will be a very simple but necessary table.
		 */

		// check sharing constant
		if (sharingConstant != null &&
				! EvalUtils.checkSharingConstant(sharingConstant)) {
			throw new IllegalArgumentException("Invalid sharing constant: " + sharingConstant);
		}

		// admin always gets all of the templates of a type
		if (external.isUserAdmin(userId)) {
			userId = null;
		}

		String[] sharingConstants = new String[] {};
		if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
			// do private templates only
		} else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
			// do public templates only
			sharingConstants = new String[] {EvalConstants.SHARING_PUBLIC};
			userId = "";
		} else if (sharingConstant == null || 
				EvalConstants.SHARING_OWNER.equals(sharingConstant)) {
			// do all templates visible to this user
			sharingConstants = new String[] {EvalConstants.SHARING_PUBLIC};
		}

		return dao.getVisibleTemplates(userId, sharingConstants, includeEmpty);
	}


	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#canControlTemplate(java.lang.String, java.lang.Long)
	 */
	public boolean canControlTemplate(String userId, Long templateId) {
		log.debug("templateId: " + templateId + ", userId: " + userId);
		// get the template by id
		EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
		if (template == null) {
			throw new IllegalArgumentException("Cannot find template with id: " + templateId);
		}

		// check perms and locked
		try {
			return checkUserControlTemplate(userId, template);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalTemplatesLogic#canCreateTemplate(java.lang.String)
	 */
	public boolean canCreateTemplate(String userId) {
		log.debug("userId: " + userId);
		if ( external.isUserAdmin(userId) ) {
			// the system super user can create templates always
			return true;
		}

		// TODO - make this check system wide and not context specific
		if ( external.countContextsForUser(userId, EvalConstants.PERM_WRITE_TEMPLATE) > 0 ) {
			return true;
		}
		return false;
	}


	// INTERNAL METHODS

	/**
	 * Check if user has permissions to control (remove or update) this template
	 * @param userId
	 * @param template
	 * @return true if they do, exception otherwise
	 */
	protected boolean checkUserControlTemplate(String userId, EvalTemplate template) {
		log.debug("template: " + template.getTitle() + ", userId: " + userId);
		// check locked first
		if (template.getId() != null &&
				template.getLocked().booleanValue() == true) {
			throw new IllegalStateException("Cannot control (modify) locked template ("+template.getId()+")");
		}

		// check ownership or super user
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( template.getOwner().equals(userId) ) {
			return true;
		} else {
			throw new SecurityException("User ("+userId+") cannot control template ("+template.getId()+") without permissions");
		}
	}

}
