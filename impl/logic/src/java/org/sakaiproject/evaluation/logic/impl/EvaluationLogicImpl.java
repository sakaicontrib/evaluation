/******************************************************************************
 * EvaluationLogicImpl.java - created by aaronz@vt.edu on Aug 21, 2006
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvaluationLogic;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.site.api.Site;



/**
 * The main business logic class for the app
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated Interface has been split into EvalExternalLogic and the 4 listed below,
 * this will be removed before release, update everything that uses this class
 */
public class EvaluationLogicImpl implements EvaluationLogic {

	private static Log log = LogFactory.getLog(EvaluationLogicImpl.class);

	private EvaluationDao evaluationDao;
	public void setEvaluationDao(EvaluationDao evaluationDao) {
		this.evaluationDao = evaluationDao;
	}

	private EvalExternalLogic externalLogic;
	public void setEvalExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}


	public void init() {
		log.debug("Init");
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getUserDisplayName(java.lang.String)
	 */
	public String getUserDisplayName(String userId) {
		return externalLogic.getUserDisplayName(userId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		return externalLogic.getCurrentUserId();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getCurrentContext()
	 */
	public String getCurrentContext() {
		return externalLogic.getCurrentContext();
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getScales(java.lang.Boolean)
	 */
	/**
	 * @deprecated scales do not work this way anymore
	 */
	public List getScales(Boolean hidden) {
		EvalScale scale = new EvalScale();
		//scale.setHidden(hidden);
		return evaluationDao.findByExample(scale);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getTemplateById(java.lang.Long)
	 */
	public EvalTemplate getTemplateById(Long templateId) {
		return (EvalTemplate) evaluationDao.findById(EvalTemplate.class, templateId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getTemplatesToDisplay(java.lang.String)
	 */
	/** 
	 * @deprecated (see note in interface)
	 */
	public List getTemplatesToDisplay(String userId) {
		if (externalLogic.isUserAdmin(userId)) {
			userId = null;
		}
		return evaluationDao.getVisibleTemplates(userId, 
				new String[] {EvalConstants.SHARING_PUBLIC}, false);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getTemplatesForUser(java.lang.String, boolean)
	 */
	public List getTemplatesForUser(String userId, boolean publicTemplates) {
		/**
		 * TODO - Hierarchy
		 * visible and shared sharing methods are meant to work by relating the hierarchy level of 
		 * the owner with the sharing setting in the template, however, that was when 
		 * we assumed there would only be one level per user. That is no longer anything 
		 * we have control over (since we depend on data that comes from another API) 
		 * so we will have to add in a table which will track the hierarchy levels and
		 * link them to the template. This will be a very simple but necessary table.
		 */
		if (externalLogic.isUserAdmin(userId)) {
			userId = null;
		}
		String [] sharing = new String[] {};
		if (publicTemplates) {
			sharing = new String[] {EvalConstants.SHARING_PUBLIC};
		}
		return evaluationDao.getVisibleTemplates(userId, sharing, false);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveTemplate(org.sakaiproject.evaluation.model.EvalTemplate)
	 */
	public void saveTemplate(EvalTemplate template){
		if (template.getId() != null) {
			EvalTemplate t = (EvalTemplate) evaluationDao.
				findById(EvalTemplate.class, template.getId());
			if (t.getLocked().booleanValue() == true) {
				// cannot change a currently locked template, it has to be unlocked
				// via internal logic controls in other methods
				throw new IllegalStateException("locked templates cannot be modified");
			}
		}
		evaluationDao.save(template);
		// TODO - add logic to lock associated items here
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteTemplate(org.sakaiproject.evaluation.model.EvalTemplate)
	 */
	public void deleteTemplate(EvalTemplate template) {
		if (template.getId() != null) {
			EvalTemplate t = (EvalTemplate) evaluationDao.
				findById(EvalTemplate.class, template.getId());
			if (t.getLocked().booleanValue() == true) {
				// cannot change a currently locked template, it has to be unlocked
				// via internal logic controls in other methods
				throw new IllegalStateException("locked templates cannot be deleted");
			}
		}
		evaluationDao.delete(template);
		// TODO - add logic to unlock associated items here
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteTemplateById(java.lang.Long)
	 */
	public boolean deleteTemplateById(Long templateId) {
		EvalTemplate t = (EvalTemplate) evaluationDao.
			findById(EvalTemplate.class, templateId);
		if (t != null) {
			deleteTemplate(t);
			return true;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#findItem(java.lang.Integer)
	 */
	public List findItem(Integer blockId) {
		EvalItem item = new EvalItem();
		item.setBlockId(blockId);

		return evaluationDao.findByExample(item);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getItemById(java.lang.Long)
	 */
	public EvalItem getItemById(Long itemId) {
		return (EvalItem) evaluationDao.findById(EvalItem.class, itemId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveItem(org.sakaiproject.evaluation.model.EvalItem)
	 */
	public void saveItem(EvalItem item) {
		if (item.getId() != null) {
			EvalItem ei = (EvalItem) evaluationDao.
				findById(EvalItem.class, item.getId());
			if (ei.getLocked().booleanValue() == true) {
				// cannot change a currently locked item, it has to be unlocked
				// via internal logic controls in other methods
				throw new IllegalStateException("locked items cannot be modified");
			}
		}
		evaluationDao.save(item);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteItem(org.sakaiproject.evaluation.model.EvalItem)
	 */
	public void deleteItem(EvalItem item) {
		if (item.getId() != null) {
			EvalItem ei = (EvalItem) evaluationDao.
				findById(EvalItem.class, item.getId());
			if (ei.getLocked().booleanValue() == true) {
				// cannot change a currently locked item, it has to be unlocked
				// via internal logic controls in other methods
				throw new IllegalStateException("locked items cannot be deleted");
			}
		}
		evaluationDao.delete(item);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteItemById(java.lang.Long)
	 */
	public boolean deleteItemById(Long itemId) {
		EvalItem ei = (EvalItem) evaluationDao.
			findById(EvalItem.class, itemId);
		if (ei != null) {
			deleteItem(ei);
			return true;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveEmailTemplate(org.sakaiproject.evaluation.model.EvalEmailTemplate)
	 */
	public void saveEmailTemplate(EvalEmailTemplate emailTemplate) {
		evaluationDao.save(emailTemplate);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getEmailTemplate(boolean)
	 */
	public EvalEmailTemplate getEmailTemplate(boolean availableTemplate) {
		EvalEmailTemplate emailTemplate = new EvalEmailTemplate();

		if (availableTemplate)
			emailTemplate.setDefaultType(EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE);
		else
			emailTemplate.setDefaultType(EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER);

		return (EvalEmailTemplate)evaluationDao.findByExample(emailTemplate).get(0);
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#findEvaluation(java.lang.Long)
	 */
	public EvalEvaluation getEvaluationById(Long evalId) {
		return (EvalEvaluation) evaluationDao.findById(EvalEvaluation.class, evalId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#countEvaluationsByTemplateId(java.lang.Long)
	 */
	public int countEvaluationsByTemplateId(Long templateId) {
		return evaluationDao.countByProperties(EvalEvaluation.class,
				new String[] {"template.id"},  new Object[] {templateId});
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getEvaluationsByTemplateId(java.lang.Long)
	 */
	public List getEvaluationsByTemplateId(Long templateId) {
		return evaluationDao.findByProperties(EvalEvaluation.class,
				new String[] {"template.id"},  new Object[] {templateId});
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void saveEvaluation(EvalEvaluation eval) {
		if ( new Date().after(eval.getDueDate()) ) {
			throw new IllegalStateException("Evaluations may not be modified after the due date");
		}
		evaluationDao.save(eval);
		// TODO - there should be logic here to lock associated templates
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void deleteEvaluation(EvalEvaluation eval) {
		if ( new Date().after(eval.getStartDate()) ) {
			throw new IllegalStateException("Evaluations may not be removed after the start date");
		} else if ( eval.getResponses().size() > 0 ) {
			throw new IllegalStateException("Evaluations may not be removed if there are associated responses");
		}
		evaluationDao.delete(eval);
		// TODO - there should be logic here to unlock associated templates
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveAssignCourse(org.sakaiproject.evaluation.model.EvalAssignCourse)
	 */
	public void saveAssignContext(EvalAssignContext assignCourse) {
		evaluationDao.save(assignCourse);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#deleteAssignCourse(org.sakaiproject.evaluation.model.EvalAssignCourse)
	 */
	public void deleteAssignContext(EvalAssignContext assignCourse) {
		evaluationDao.delete(assignCourse);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getSites()
	 */
	public Map getSites() {
		// TODO - the javadoc for this method is wrong -AZ
		Map idTitleMap = new HashMap();
		List sites = externalLogic.getContextsForUser(getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		for (int i=0; i<sites.size(); i++) {
			Site site = (Site) sites.get(i);
			idTitleMap.put(site.getId(), site.getTitle());
		}
		return idTitleMap;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getEnrollment(java.lang.String[])
	 */
	public int[] getEnrollment(String[] selectedSakaiSiteIds) {
		int totalSites = selectedSakaiSiteIds.length;
		int[] sakaiSiteEnrollments = new int[totalSites];

		for (int count = 0; count < totalSites; count++) {
			Set userIds = externalLogic.getUserIdsForContext(selectedSakaiSiteIds[count], EvalConstants.PERM_TAKE_EVALUATION);
			sakaiSiteEnrollments[count] = userIds.size();
		}
		return sakaiSiteEnrollments;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getVisibleEvaluationsForUser(java.lang.String)
	 */
	public List getVisibleEvaluationsForUser(String userId, boolean includeRecentClosed) {
		if (externalLogic.isUserAdmin(userId)) {
			// TODO - this will probably be too slow -AZ
			return evaluationDao.findAll(EvalEvaluation.class);
		}

		// TODO - handle recently closed flag
		List l = new ArrayList();
		// get all evaluations created (owned) by this user
		l = evaluationDao.findByProperties(EvalEvaluation.class,
				new String[] {"owner"}, new Object[] {userId});
		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationsForUser(java.lang.String, boolean, boolean)
	 */
	public List getEvaluationsForUser(String userId, boolean activeOnly, boolean takenOnly) {
		List sites = externalLogic.getContextsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

		String[] contexts = new String[sites.size()];
		for (int i=0; i<sites.size(); i++) {
			contexts[i] = ((Site)sites.get(i)).getId();
		}
		// TODO - handle the activeOnly and takenOnly

		Set s = evaluationDao.getEvaluationsByContexts( contexts, activeOnly );
		// stuff the set into a list
		return new ArrayList(s);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#countEvaluationContexts(java.lang.Long)
	 */
	public int countEvaluationContexts(Long evaluationId) {
		return evaluationDao.countByProperties(EvalAssignContext.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationId});
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getEvaluationContexts(java.lang.Long)
	 */
	public Map getEvaluationContexts(Long[] evaluationIds) {
		Map evals = new TreeMap();

		// create the inner lists
		for (int i=0; i<evaluationIds.length; i++) {
			List innerList = new ArrayList();
			evals.put(evaluationIds[i], innerList);
		}

		List l = evaluationDao.findByProperties(EvalAssignContext.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationIds});
		for (int i=0; i<l.size(); i++) {
			EvalAssignContext eac = (EvalAssignContext) l.get(i);
			String context = eac.getContext();

			// put stuff in inner list
			Long evalId = eac.getEvaluation().getId();
			List innerList = (List) evals.get(evalId);
			innerList.add( externalLogic.makeContextObject(context) );
		}
		return evals;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getCourseTitle(java.lang.Long)
	 */
	public String getCourseTitle(Long evaluationId) {
		List l = evaluationDao.findByProperties(EvalAssignContext.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationId});

		EvalAssignContext eac = (EvalAssignContext) l.get(0);
		String context = eac.getContext();

		return getDisplayTitle(context);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getCourseTitle(java.lang.String)
	 */
	public String getDisplayTitle(String context) {
		return externalLogic.getDisplayTitle(context);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getEvaluationResponses(java.lang.String, java.lang.Long[])
	 */
	public List getEvaluationResponses(String userId, Long[] evaluationIds) {
		return evaluationDao.findByProperties(EvalResponse.class,
			new String[] {"owner","evaluation.id"},
			new Object[] {userId, evaluationIds},
			new int[] {ByPropsFinder.EQUALS, ByPropsFinder.EQUALS},
			new String[] {"id"});
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getAnswers(java.lang.Long, java.lang.Long)
	 */	
	public List getEvalAnswers(Long itemId, Long evaluationId){
		return evaluationDao.getAnswers(itemId, evaluationId);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getTotalEnrollments(java.lang.Long)
	 */
	public int getTotalEnrollments(Long evaluationId) {
		int totalEnrollments = 0;

		List l = evaluationDao.findByProperties(EvalAssignContext.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationId});
		for (int i=0; i<l.size(); i++) {
			EvalAssignContext eac = (EvalAssignContext) l.get(i);
			String context = eac.getContext();
			Set userIds = externalLogic.getUserIdsForContext(context, EvalConstants.PERM_TAKE_EVALUATION);
			totalEnrollments = totalEnrollments + userIds.size();
		}
		return totalEnrollments;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#saveResponse(org.sakaiproject.evaluation.model.EvalResponse)
	 */
	public void saveResponse(EvalResponse response) {
		// save everything in one transaction
		Set[] entitySets = new Set[2];

		//EvalResponse has to be saved first.
		entitySets[0] = new HashSet();
		entitySets[0].add(response);
		entitySets[1] = response.getAnswers();

		evaluationDao.saveMixedSet(entitySets);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#countResponses(java.lang.Long)
	 */
	public int countResponses(Long evaluationId) {
		return evaluationDao.countByProperties(EvalResponse.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationId});
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#getAssigCourseByEvalId(java.lang.Long)
	 */
	public List getAssignContextsByEvalId(Long evaluationId) {
		return  evaluationDao.findByProperties(EvalAssignContext.class,
				new String[] {"evaluation.id"},  new Object[] {evaluationId});
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationLogic#batchSaveItemTemplate(org.sakaiproject.evaluation.model.EvalTemplate, org.sakaiproject.evaluation.model.EvalItem)
	 */
	public void batchSaveItemTemplate(EvalTemplate template, EvalItem item) {
		// save everything in one transaction
		Set[] entitySets = new Set[2];

		//EvalTemplate has to be saved before Item for ITEM2TEMPLATE table.
		entitySets[0] = item.getTemplates();
		entitySets[1] = template.getItems();

		evaluationDao.saveMixedSet(entitySets);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#getUserIdsForSite(java.lang.String, java.lang.String)
	 */
	public Set getUserIdsForSite(String siteId, String permission) {
		return externalLogic.getUserIdsForContext(siteId, permission);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#getSitesForUser(java.lang.String, java.lang.String)
	 */
	public List getSitesForUser(String userId, String permission) {
		log.info("Sites for user:" + userId + ", permission: " + permission);
		List l = new ArrayList();
		log.error("THIS METHOD IS NO LONGER SUPPORTED, see ExternalLogic.getContextsForUser instead");
		if (l.isEmpty()) log.warn("Empty list of sites for user:" + userId + ", permission: " + permission);
		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvaluationSecurity#countSitesForUser(java.lang.String, java.lang.String)
	 */
	public int countSitesForUser(String userId, String permission) {
		return externalLogic.countContextsForUser(userId, permission);
	}

	public boolean userBeginEvaluation(String userId) {
		log.info("Checking begin eval for: " + userId);
		boolean isAdmin = externalLogic.isUserAdmin(userId);
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
			if ( evaluationDao.countVisibleTemplates(userId, 
					new String[] {EvalConstants.SHARING_PUBLIC}, false) > 0 ) {
				return true;
			}
		}
		return false;
	}

	public boolean canCreateTemplate(String userId) {
		log.info("Checking create template for: " + userId);
		if ( externalLogic.isUserAdmin(userId) ) {
			// the system super user can create templates always
			return true;
		}

		// TODO - make this check system wide and not context specific
		if ( externalLogic.isUserAllowedInContext(userId, 
				EvalConstants.PERM_WRITE_TEMPLATE, externalLogic.getCurrentContext()) ) {
			return true;
		}
		return false;
	}


	/**
	 * WARNING: All methods below are non-functional
	 */


	public void deleteItem(EvalItem item, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canControlItem(String userId, Long itemId) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteItem(Long itemId, String userId) {
		// TODO Auto-generated method stub
	}


	public List getItemsForUser(String userId, String sharingConstant) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveItem(EvalItem item, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canControlTemplate(String userId, Long templateId) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteTemplate(Long templateId, String userId) {
		// TODO Auto-generated method stub
		
	}


	public List getTemplatesForUser(String userId, String sharingConstant) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveTemplate(EvalTemplate template, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canBeginEvaluation(String userId) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canRemoveEvaluation(String userId, Long evaluationId) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canTakeEvaluation(String userId, Long evaluationId, String context) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteEvaluation(Long evaluationId, String userId) {
		// TODO Auto-generated method stub
		
	}


	public String getEvaluationState(Long evaluationId) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveEvaluation(EvalEvaluation evaluation, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canModifyResponse(String userId, Long responseId) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canCreateAssignEval(String userId, Long evaluationId) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canDeleteAssignContext(String userId, Long assignContextId) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteAssignContext(Long assignContextId, String userId) {
		// TODO Auto-generated method stub
		
	}


	public void saveAssignContext(EvalAssignContext assignContext, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canControlEmailTemplate(String userId, Long evaluationId, int emailTemplateTypeConstant) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId) {
		// TODO Auto-generated method stub
		return false;
	}


	public EvalEmailTemplate getDefaultEmailTemplate(int emailTemplateTypeConstant) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canControlScale(String userId, Long scaleId) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteScale(Long scaleId, String userId) {
		// TODO Auto-generated method stub
		
	}


	public EvalScale getScaleById(Long scaleId) {
		// TODO Auto-generated method stub
		return null;
	}


	public List getScalesForUser(String userId, String sharingConstant) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveScale(EvalScale scale, String userId) {
		// TODO Auto-generated method stub
		
	}


	public int countResponses(Long evaluationId, String context) {
		// TODO Auto-generated method stub
		return 0;
	}


	public void saveResponse(EvalResponse response, String userId) {
		// TODO Auto-generated method stub
		
	}


	public boolean canControlTemplateItem(String userId, Long templateItemId) {
		// TODO Auto-generated method stub
		return false;
	}


	public void deleteTemplateItem(Long templateItemId, String userId) {
		// TODO Auto-generated method stub
		
	}


	public List getItemsForTemplate(Long templateId) {
		// TODO Auto-generated method stub
		return null;
	}


	public EvalTemplateItem getTemplateItemById(Long templateItemId) {
		// TODO Auto-generated method stub
		return null;
	}


	public List getTemplateItemsForTemplate(Long templateId) {
		// TODO Auto-generated method stub
		return null;
	}


	public void saveTemplateItem(EvalTemplateItem templateItem, String userId) {
		// TODO Auto-generated method stub
		
	}


	public List getItemsForTemplate(Long templateId, String userId) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getNextBlockId() {
		// TODO Auto-generated method stub
		return null;
	}

}
