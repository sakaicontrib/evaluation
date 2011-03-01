package org.sakaiproject.evaluation.dao;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalAdmin;

/**
 * This is the main class for controlling and retrieving all data for the eval admins.
 * Use this interface to get eval admin info, assign/unassign eval admins, and check if 
 * a user has admin permissions in the evaluation module.
 * 
 * @author Shaun Fleming (spfjr@umd.edu)
 */
public interface EvalAdminSupport {
	
	/**
     * Gets the list of all eval admins.
     * 
     * @return a list of EvalAdmin objects
     */
	public List<EvalAdmin> getEvalAdmins();
	
	/**
     * Gets the EvalAdmin object representing the user with the specified user id.
     * Returns null if no user was found.
     * 
     * @param userId internal user id (not username)
     * @return an EvalAdmin object or null if not found
     */
	public EvalAdmin getEvalAdmin(String userId);
	
	/**
	 * Assigns a user as an eval admin.
	 * 
	 * @param userId internal user id (not username)
	 * @param assignorUserId internal user id (not username) of the assignor (most likely the current user)
	 */
	public void assignEvalAdmin(String userId, String assignorUserId);
	
	/**
	 * Removes a user from the list of eval admins. 
	 * 
	 * @param userId internal user id (not username)
	 */
	public void unassignEvalAdmin(String userId);
	
	/**
	 * Checks if this user has eval admin rights in the evaluation system.
	 * 
	 * @param userId internal user id (not username)
	 * @return true if the user is an eval admin, false otherwise
	 */
	public boolean isUserEvalAdmin(String userId);
	
}
