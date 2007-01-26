/******************************************************************************
 * EvalScaleLogic.java - created by aaronz@vt.edu on Dec 27, 2006
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

import org.sakaiproject.evaluation.model.EvalScale;


/**
 * Handles all logic associated with the Scales in the system
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalScalesLogic {

	/**
	 * Get a scale by its unique id<br/>
	 * <b>Note:</b> You should get the scale from the item most of the time<br/>
	 * <b>Note:</b> If you need to get a group of scales
	 * then use {@link #getScalesForUser(String, String)}
	 * 
	 * @param scaleId the id of an EvalScale object
	 * @return the item or null if not found
	 */
	public EvalScale getScaleById(Long scaleId);

	/**
	 * Create or update a scale (update only if it is not locked),
	 * cannot change locked setting for existing scales,
	 * 
	 * 
	 * @param scale a scale object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveScale(EvalScale scale, String userId);

	/**
	 * Deletes a stored scale, locked scales cannot be removed,
	 * use {@link #canControlScale(String, Long)} if you want to 
	 * check if a user has permission and avoid possible exceptions
	 * 
	 * @param scaleId the id of an EvalScale object
	 * @param userId the internal user id (not username)
	 */
	public void deleteScale(Long scaleId, String userId);

	/**
	 * Get evaluation scales that are visible to the supplied user
	 * (includes owned scales and public or shared scales), can
	 * optionally get just owned scales, scales are sorted
	 * 
	 * @param userId the internal user id (not username)
	 * @param sharingConstant a SHARING constant from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants},
	 * if null, return all scales visible to the
	 * user, if set to a sharing constant then return just the visible
	 * scales that match that sharing setting (can be used to get all
	 * scales owned by this user for example)
	 * @return a List of EvalScale objects (in alpha order with private scales, then public, then others)
	 */
	public List getScalesForUser(String userId, String sharingConstant);


	// PERMISSIONS

	/**
	 * Check if a user can control (update or delete) a specific scale,
	 * locked scales cannot be modified in any way
	 * 
	 * @param userId the internal user id (not username)
	 * @param scaleId the id of an EvalScale object
	 * @return true if user can control this scale, false otherwise
	 */
	public boolean canControlScale(String userId, Long scaleId);

}
