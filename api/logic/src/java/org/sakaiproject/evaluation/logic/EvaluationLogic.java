/******************************************************************************
 * EvaluationLogic.java - created by aaronz@vt.edu on Aug 21, 2006
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

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;



/**
 * <b>THIS WILL BE REMOVED</b> - 
 * The OLD interface for business logic for our app
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated Interface has been split into EvalExternalLogic and the 4 listed below,
 * this will be removed before release, update everything that uses this class
 */
public interface EvaluationLogic 
	extends EvalItemsLogic, EvalTemplatesLogic, EvalEvaluationsLogic, 
		EvalResponsesLogic, EvalAssignsLogic, EvalEmailsLogic, EvalScalesLogic {

	// Place temporary passthrough methods for the external bits
	/**
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId();
	
	/**
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getUserDisplayName(String)
	 */
	public String getUserDisplayName(String userId);

	/**
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getDisplayTitle(String)
	 */
	public String getDisplayTitle(String context);
	
	
	// TODO - Putting deprecated methods here so I remember to remove them all before release

	/**
	 * Get the total number of people enrolled for each site(course) 
	 * associated(assigned) with the evaluationId.
	 *
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return total number of Enrollments corresponding to the evaluation.
	 * @deprecated this should be a count method and a similar one is located in
	 * EvalExternalLogic (though you have to give it the context)
	 */
	public int getTotalEnrollments(Long evaluationId);


	/**
	 * Get the number of users associated with selected sites
	 *
	 * @param selectedSakaiSiteIds - list of sites selected. 
	 * @return Enrollments corresponding to the list of sites.
	 * @deprecated this method is not well written or well used and not any more 
	 * efficient than making multiple calls to {@link #countEnrollment(String)}
	 */
	public int[] getEnrollment(String[] selectedSakaiSiteIds);

	/**
	 * Get the title associated with those contexts
	 * for an evaluation id
	 * 
	 * this function is called when countEvaluationContexts(Long evaluationId) method
	 * returns  less or equal to 1.
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return String (title)
	 * @deprecated this is display logic and should go in the producer or a helper class,
	 * will be removed before release, use {@link #getDisplayTitle(String)}
	 */
	public String getCourseTitle(Long evaluationId);

	/**
	 * This method calls the site service API to get the list of 
	 * site (AKA courses) which a user can update. After fetching the 
	 * sites, Adminstration Workpace is filtered out based on it's
	 * id.
	 * 
	 * @return List of sites a user can update.
	 * @deprecated Duplicate functionality, replaced by {@link getSitesForUser}
	 */
	public Map getSites();


	/**
	 * Find templates for assigning based on who the person is (Instructor or 4 kind of admin).
	 * For instructors following logic should be used. That is the "sharing" column in template 
	 * table can have following options :
	 *      "private" which are owned by the user,
	 *      "visible",
	 *      "public".
	 * 
	 * @param userId - userId of the current user. 
	 * @return the list of template class objects.
	 * @deprecated Poorly named and not enough options, will be removed before release, replaced by {@link #getTemplatesForUser}
	 */
	public List getTemplatesToDisplay(String userId);

	/** 
	 * Do a batch save of template and first item.
	 * 
	 * @param template - EvalTemplate object.  
	 * @param item  - EvalItem Object.
	 * @deprecated This case should be handled in {@link #saveTemplate(EvalTemplate)} or as an overload of it
	 */
	public void batchSaveItemTemplate(EvalTemplate template, EvalItem item);

	/**
	 * Get a list of all users that have a specific permission in a site
	 * @param siteId a Sakai site id
	 * @param permission a Sakai auth permission
	 * @return a List of Strings which represent the user Ids of all users in the site with that permission
	 * @deprecated no longer exposing Sakai objects, use getUserIdsForContext
	 */
	public Set getUserIdsForSite(String siteId, String permission);

	/**
	 * Get a list of all sites that a user has a specific permission in
	 * @param userId a Sakai user id
	 * @param permission a Sakai auth permission
	 * @return a List of Site objects
	 * @deprecated no longer exposing Sakai objects, use getContextsForUser
	 */
	public List getSitesForUser(String userId, String permission);

	/**
	 * Get a count of all sites that a user has a specific permission in
	 * @param userId a Sakai user id
	 * @param permission a Sakai auth permission
	 * @return the count of the Sites that the user has a permission in
	 * @deprecated no longer exposing Sakai objects, use countContextsForUser
	 */
	public int countSitesForUser(String userId, String permission);

	/**
	 * Get the available email and reminder email template.
	 *
	 * @param availableTemplate - true if available template has to be fetched, 
	 * 							  false if reminder email has to be fetched. 
	 * @return email template.
	 * @deprecated Badly formed method, replaced by getEmailTemplate(String) in EvalEvaluations
	 */
	public EvalEmailTemplate getEmailTemplate(boolean availableTemplate);

	/**
	 * Find Child Block Item that has Block_ID as the the argument
	 * @return
	 * @deprecated Not sure what this is meant to do since it returns a List
	 * of... who knows what, but until there are better
	 * docs about it this is deprecated
	 */
	public List findItem(Integer blockId);

	/**
	 * Get evaluation scales
	 * 
	 * @param hidden if true then return hidden scales, 
	 * 	if false return visible scales, 
	 * 	if null return all scales
	 * @return a List of EvalScale objects
	 * @deprecated Needs to get scales for user and hidden is not used anymore, replaced by getScalesForUser(String, String)
	 */
	public List getScales(Boolean hidden);

	/**
	 * Deletes a stored item, locked items cannot be removed
	 * 
	 * @param item an object item to be deleted
	 * @param userId the internal user id (not username)
	 * @deprecated Replaced by deleteItem(Long, String)
	 */
	public void deleteItem(EvalItem item, String userId);

	/**
	 * Delete the template only if it is not locked
	 * 
	 * @param template the object to be removed
	 * @deprecated Use deleteTemplate(Long, String) instead
	 */
	public void deleteTemplate(EvalTemplate template);

	/**
	 * Saves a single response from a single user
	 * 
	 * @param response the response object to save
	 * @deprecated Use saveResponse(EvalResponse, String) instead
	 */
	public void saveResponse(EvalResponse response);
}
