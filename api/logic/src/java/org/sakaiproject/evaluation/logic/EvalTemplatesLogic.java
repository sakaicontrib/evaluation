/******************************************************************************
 * EvalTemplateLogic.java - created by aaronz@vt.edu on Dec 24, 2006
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

import org.sakaiproject.evaluation.model.EvalTemplate;


/**
 * Handles all business logic associated with templates
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalTemplatesLogic {

	/**
	 * Get the template associated with this template id
	 * 
	 * @param templateId the unique id of a template
	 * @return the template object or null if not found
	 */
	public EvalTemplate getTemplateById(Long templateId);
	
	/**
	 * Get the template associated with this external id<br/>
	 * Note: A template eid is null except when the template
	 * was imported from an external system.
	 * 
	 * @param eid the id of a template in an external system
	 * @return the template object or null if not found
	 */
	public EvalTemplate getTemplateByEid(String eid);

	/**
	 * Save or update the template only if it is not locked<br/>
	 * Locks any associated items if this template is locked<br/>
	 * Use {@link #canCreateTemplate(String)} or {@link #canControlTemplate(String, Long)}
	 * to check if user can save template and avoid exceptions
	 * 
	 * @param template the object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveTemplate(EvalTemplate template, String userId);

	/**
	 * Delete the template only if it is not locked and not expert,
	 * also removes all associated templateItems and unlinks associated items<br/>
	 * Unlocks any associated items that are not being used in other locked templates<br/>
	 * Use {@link #canControlTemplate(String, Long)} to check if
	 * the user can control this template and avoid exceptions<br/>
	 * 
	 * @param template the object to be removed
	 * @param userId the internal user id (not username)
	 */
	public void deleteTemplate(Long templateId, String userId);

	/**
	 * Get all accessible templates for a user, this includes all private templates, 
	 * and public templates as desired<br/>
	 * (currently does not include shared or visible templates)
	 * 
	 * @param userId the internal user id (not username)
	 * @param sharingConstant a SHARING constant from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants},
	 * if null, return all templates visible to the
	 * user, if set to a sharing constant then return just the visible
	 * templates that match that sharing setting (can be used to get all
	 * templates owned by this user for example)
	 * @param includeEmpty if true then include templates with no items in them, else
	 * only return templates that have items
	 * @return a list of EvalTemplate objects
	 */
	public List getTemplatesForUser(String userId, String sharingConstant, boolean includeEmpty);


	// PERMISSIONS

	/**
	 * Check if a user can create templates (should check system wide)
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user can create templates, false otherwise
	 */
	public boolean canCreateTemplate(String userId);

	/**
	 * Check if a user can modify a template,
	 * locked templates cannot be modified
	 * 
	 * @param userId the internal user id (not username)
	 * @param templateId the id of an {@link EvalTemplate} object
	 * @return true if the user can control the template, false otherwise
	 */
	public boolean canModifyTemplate(String userId, Long templateId);

	/**
	 * Check if a user can delete a template,
	 * locked templates or those used in an evaluation cannot be removed
	 * 
	 * @param userId the internal user id (not username)
	 * @param templateId the id of an {@link EvalTemplate} object
	 * @return true if the user can remove the template, false otherwise
	 */
	public boolean canRemoveTemplate(String userId, Long templateId);


	// CHECKS

	/**
	 * Check if a title can be used for a new template,
	 * this check should be done before the template save is attempted
	 * 
	 * @param title a possible title for a new template
	 * @param templateId a template id (optional, can be null) to exclude from the title check,
	 * allows a check to be made without triggering that the title is in use by the
	 * template we are currently working with (probably already saved)
	 * @return true if this title is unused, false otherwise
	 */
	public boolean checkTemplateTitleUnused(String title, Long templateId);
}
