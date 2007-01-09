/******************************************************************************
 * EvaluationSecurityImpl.java - created by aaronz@vt.edu
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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;


/**
 * OLD Implementation for security
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationSecurityOld {

	private static Log log = LogFactory.getLog(EvaluationSecurityOld.class);

	private EvaluationDao evaluationDao;
	public void setEvaluationDao(EvaluationDao evaluationDao) {
		this.evaluationDao = evaluationDao;
	}

	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private FunctionManager functionManager;
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}


	public void init() {
		log.debug("init, register security perms");
		
		// register Sakai permissions for this tool
//		functionManager.registerFunction(WRITE_TEMPLATE);
//		functionManager.registerFunction(ASSIGN_EVALUATION);
//		functionManager.registerFunction(BE_EVALUATED);
//		functionManager.registerFunction(TAKE_EVALUATION);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#isUserAdmin(java.lang.String)
	 */
	public boolean userAdmin(String userId) {
		log.info("Checking is eval admin for: " + userId);
		return securityService.isSuperUser(userId);
	}

	public List userHierarchyAdmin(String userId) {
		// TODO This method cannot be written until the hierarchy code is available, always return empty list for now
		return new ArrayList();
	}

	public boolean userHierarchyAdmin(String userId, String hierarchyId) {
		// TODO This method cannot be written until the hierarchy code is available, always return false for now
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#userCreateTemplate(java.lang.String)
	 */
	public boolean userCreateTemplate(String userId) {
		log.info("Checking create template for: " + userId);
		if ( securityService.isSuperUser(userId) ) {
			// the system super user can create templates always
			return true;
		}
		String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
		if ( securityService.unlock(userId, EvalConstants.PERM_WRITE_TEMPLATE, siteRef) ) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#userBeginEvaluation(java.lang.String)
	 */
	public boolean userBeginEvaluation(String userId) {
		log.info("Checking begin eval for: " + userId);
		boolean isAdmin = userAdmin(userId);
		if ( isAdmin && (evaluationDao.countAll(EvalTemplate.class) > 0) ) {
			// admin can access all templates and create an evaluation if 
			// there is at least one template
			return true;
		}
		if ( countSitesForUser(userId, EvalConstants.PERM_ASSIGN_EVALUATION) > 0 ) {
			log.debug("User has permission to assign evaluation in at least one site");
			// TODO - this check needs to be more robust at some point
			// currently we are ignoring shared and visible templates
			/**
			 * TODO - Hierarchy
			 * visible and shared sharing methods are meant to work by relating the hierarchy level of 
			 * the owner with the sharing setting in the template, however, that was when 
			 * we assumed there would only be one level per user. That is no longer anything 
			 * we have control over (since we depend on data that comes from another API) 
			 * so we will have to add in a table which will track the hierarchy levels and
			 * link them to the template. This will be a very simple but necessary table.
			 */
			if ( evaluationDao.countVisibleTemplates(userId, true) > 0 ) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#userTakeEvaluation(java.lang.String)
	 */
	public boolean userTakeEvaluation(String userId) {
		// convenience method
		return userTakeEvaluation(userId, toolManager.getCurrentPlacement().getContext());
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#userTakeEvaluation(java.lang.String, java.lang.String)
	 */
	public boolean userTakeEvaluation(String userId, String context) {
		log.info("Checking take eval for: " + userId);
		if ( securityService.isSuperUser(userId) ) {
			// the system super user can take all evaluations
			return true;
		}
		String siteRef = siteService.siteReference(context);
		if ( securityService.unlock(userId, EvalConstants.PERM_TAKE_EVALUATION, siteRef) ) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#getUserIdsForSite(java.lang.String, java.lang.String)
	 */
	public Set getUserIdsForSite(String siteId, String permission) {
		String siteRef = siteService.siteReference(siteId);
		List azGroups = new ArrayList();
		azGroups.add(siteRef);
		return authzGroupService.getUsersIsAllowed(permission, azGroups);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#getSitesForUser(java.lang.String, java.lang.String)
	 */
	public List getSitesForUser(String userId, String permission) {
		log.info("Sites for user:" + userId + ", permission: " + permission);
		List l = new ArrayList();
		Set authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
		Iterator it = authzGroupIds.iterator();
		while (it.hasNext()) {
			String authzGroupId = (String) it.next();
			Reference r = entityManager.newReference(authzGroupId);
			if(r.isKnownType()) {
				// check if this is a Sakai Site
				if(r.getType().equals(SiteService.APPLICATION_ID)) {
					String siteId = r.getId();
					try {
						Site site = siteService.getSite(siteId);
						l.add(site);
					} catch (IdUnusedException e) {
						// invalid site Id returned
						throw new RuntimeException("Could not get site from siteId:" + siteId);
					}
				}
			}
		}
		if (l.isEmpty()) log.warn("Empty list of sites for user:" + userId + ", permission: " + permission);
		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#countSitesForUser(java.lang.String, java.lang.String)
	 */
	public int countSitesForUser(String userId, String permission) {
		int count = 0;
		Set authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
		Iterator it = authzGroupIds.iterator();
		while (it.hasNext()) {
			String authzGroupId = (String) it.next();
			Reference r = entityManager.newReference(authzGroupId);
			if(r.isKnownType()) {
				// check if this is a Sakai Site
				if(r.getType().equals(SiteService.APPLICATION_ID)) {
					count++;
				}
			}
		}
		return count;
	}

}