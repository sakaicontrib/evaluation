/**
 * $Id$
 * $URL$
 * EvalEvaluationSetupService.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles all logic associated with setting up and creating evaluations,
 * this includes all the logic for assigning groups and users to an evaluation
 * and processing the email templates used by an evaluation<br/>
 * Note that the handling and linkage of participants to an evaluation can be confusing.
 * It is all based on assigning users/group/nodes to the evaluation. These assignments are like
 * enrollments in some ways and can be updated before the evaluation begins (and to a limited degree
 * while it is running as well). For clarity this is how it works for a developer:<br/>
 * The evaluation has either users, groups, or hierarchy nodes assigned to it. User are indirectly
 * assigned to the evaluation via groups or hierarchy nodes OR directly assigned to it using the
 * participant methods. <br/>
 * There are methods for lookup and reading of these assignments in {@link EvalEvaluationService}
 * so look there when trying to determine what the current assignments are.<br/>
 * {@link EvalEvaluationService#getAssignGroupById(Long)} <br/>
 * {@link EvalEvaluationService#getAssignGroupsForEvals(Long[], boolean, Boolean)} <br/>
 * {@link EvalEvaluationService#getAssignHierarchyByEval(Long)} <br/>
 * {@link EvalEvaluationService#getAssignHierarchyById(Long)} <br/>
 * {@link EvalEvaluationService#getEvalGroupsForEval(Long[], boolean, Boolean)} <br/>
 * Here are the assignment write methods broken down:<br/>
 * Users - TODO <br/>
 * Groups/Hierarchy - {@link #setEvalAssignments(Long, String[], String[], boolean)} 
 * (this is the method you should use most of the time for assigning groups or hierarchy nodes) <br/>
 * Groups - {@link #saveAssignGroup(EvalAssignGroup, String)}, {@link #deleteAssignGroup(Long, String)} <br/>
 * Hierachy - {@link #deleteAssignHierarchyNodesById(Long[])} <br/>
 * <br/>
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalEvaluationSetupService {

    /**
     * Save or update an evaluation to persistent storage,
     * checks that dates are appropriate and validates settings,
     * use {@link #updateEvaluationState(Long)} to check the state
     * if you want to avoid possible exceptions<br/>
     * Evaluations can be saved with the email templates as null and will use the
     * default templates in this circumstance<br/>
     * <b>Note:</b> Do NOT attempt to save an evaluation with a null template
     * or a template that contains no items or an exception will result<br/>
     * <b>Note:</b> All evaluations must run with copies of templates.
     * The template will automatically be copied the first time the
     * evaluation is saved in a state that is after PARTIAL, you can also copy the
     * template yourself and assign the copied template to the evaluation<br/>
     * 
     * <b>Note about dates</b>:<br/>
     * Start date - eval becomes active on this date, cannot change start date once it passes, 
     * most parts of evaluation cannot change on this date, no assigned contexts can be modified<br/>
     * Due date - eval is reported to be closed after this date passes (interface and email), 
     * cannot change due date once it passes, cannot assign new contexts once this passes<br/>
     * Stop date - eval is actually closed after this date passes, cannot change stop date once it passes,
     * no changes to evaluation after this date EXCEPT adjusting the view dates<br/>
     * View date - eval results visible on this date<br/>
     * (currently times are taken into account, so if you want to close an evaluation at the
     * end of a date, make sure to set the time to midnight)
     * 
     * @param evaluation evaluation object to save
     * @param userId the acting user, normally the current user, internal user id (not username)
     * @param created set this to true when the evaluation has been just created 
     * (i.e. going from partial state to a real state),
     * false in all other cases
     */
    public void saveEvaluation(EvalEvaluation evaluation, String userId, boolean created);

    /**
     * Delete an evaluation from persistent storage,
     * evaluations that are active or completed cannot be deleted,
     * use {@link #canRemoveEvaluation(String, EvalEvaluation)} to check if
     * the evaluation can be removed if you want to avoid possible exceptions,
     * removes all associated course assignments and email templates 
     * (if they are not default or associated with other evaluations)
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param userId the acting user, normally the current user, internal user id (not username)
     */
    public void deleteEvaluation(Long evaluationId, String userId);

    /**
     * Get the evaluations that are currently visible to a user, this should be used
     * to determine evaluations that are visible from an administrative perspective,
     * can limit to recently closed only (closed within 10 days)
     * 
     * @param userId the acting user, normally the current user, internal user id (not username)
     * @param recentOnly if true return recently closed evaluations only 
     * (still returns all active and in queue evaluations), if false return all closed evaluations
     * @param showNotOwned if true for a non-admin user, then return all 
     * evaluations which are both owned and not-owned, else only return the owned evaluations.
     * @param includePartial if true then partial evaluations will be returned as well,
     * otherwise only fully created evaluations are returned
     * @return a List of {@link EvalEvaluation} objects
     */
    public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned, boolean includePartial);

    /**
     * Get all evaluations that can be taken by this user (includes optional evalGroups info),
     * can include only active and only untaken if desired
     * 
     * @param userId the acting user, normally the current user, internal user id (not username)
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param untakenOnly if true, include only the evaluations which have NOT been taken (for at least one group this user has access to), 
     * if false, include only evaluations which have already been taken (for at least one group this user has access to),
     * if null, include all evaluations
     * @param includeAnonymous if true, include both assigned and anonymous evaluations, 
     * if false, only include assigned evals which are not also anonymous,
     * if null include any assigned evaluations (regardless of the anon or not)
     * @return a List of {@link EvalEvaluation} objects (sorted by DueDate)
     */
    public List<EvalEvaluation> getEvaluationsForUser(String userId, Boolean activeOnly, Boolean untakenOnly, Boolean includeAnonymous);

    /**
     * Close an evaluation before the closing date,
     * this will force the evaluation closed and move up the closing date and stop dates to now<br/>
     * Does nothing if the evaluation is already closed
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param userId the acting user, normally the current user, internal user id (not username)
     * @return the evaluation that is being worked with
     */
    public EvalEvaluation closeEvaluation(Long evaluationId, String userId);


    // ASSIGNMENTS

    // ASSIGNMENTS - PARTICIPANTS (USERS)

    /**
     * Save or update user assignments (assign a user to be able to participate in an evaluation),
     * this allows individual user assignments to be directly manipulated,
     * the owner will be set to the current user automatically if it is not set <br/>
     * Most of the time it will be easier to assign users by assigning a group or hierarchy node,
     * <br/> Always run as the current user for permissions checks
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object,
     * this will replace any existing evaluation which is set in the objects
     * @param assignUsers the user assignments to save or update
     */
    public void saveUserAssignments(Long evaluationId, EvalAssignUser... assignUsers);

    /**
     * Directly remove user assignments from an evaluation,
     * if they are linked then the assignments will be marked as removed instead of being actually removed,
     * otherwise the assignments are permanently removed
     * <br/> Always run as the current user for permissions checks
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param userAssignmentIds all the ids of {@link EvalAssignUser} objects to remove
     */
    public void deleteUserAssignments(Long evaluationId, Long... userAssignmentIds);

    /**
     * Synchronizes all the user assignments with the assigned groups for this evaluation <br/>
     * This will fail if it is run after the evaluation closes. If it is run before the evaluation starts then the
     * synchronization is complete, the synchronization cannot remove any users which have already responded 
     * to the evaluation
     * <br/> Always run as an admin for permissions handling
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param evalGroupId (OPTIONAL) the internal group id of an eval group,
     * this will cause the synchronize to only affect the assignments related to this group
     * @return the list of {@link EvalAssignUser} ids changed during the synchronization (created, updated, deleted),
     * NOTE: deleted {@link EvalAssignUser} will not be able to be retrieved
     * @throws IllegalArgumentException if the evaluationId is invalid
     * @throws IllegalStateException if the evaluation state is invalid for synchronizing (closed or later)
     */
    public List<Long> synchronizeUserAssignments(Long evaluationId, String evalGroupId);


    // ASSIGNMENTS - HIERARCHY

    /**
     * Assigns hierarchy nodes and/or eval groups to an evaluation and therefore assigns all eval groups that are located
     * at that hierarchy node, this will not include groups below or above this node so if you want
     * to assign the nodes below you will need to include them in the array,
     * this will also create the user assignments automatically
     * <br/> Always run as the current user for permissions checks
     * 
     * @param evaluationId unique id of an {@link EvalEvaluation}
     * @param nodeIds unique IDs of a set of hierarchy nodes (null if none to assign)
     * @param evalGroupIds the internal unique IDs for a set of evalGroups (null if none to assign)
     * @param appendMode if true then we will add these assignments to the evaluation but not change any existing ones,
     * if false then the set of passed in assignments will be made the only assignments for this evaluation,
     * existing assignments that do not match will be removed
     * @return a list of the persisted hierarchy assignments (nodes and groups together)
     */
    public List<EvalAssignHierarchy> setEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds, boolean appendMode);

    /**
     * Remove all assigned hierarchy nodes with the unique ids specified,
     * also cleanup all the assign groups that are associated underneath these hierarchy nodes,
     * this will remove the associated user assignments automatically
     * <br/> Always run as the current user for permissions checks
     * 
     * @param assignHierarchyIds unique ids of {@link EvalAssignHierarchy} objects
     */
    public void deleteAssignHierarchyNodesById(Long... assignHierarchyIds);


    // ASSIGNMENTS - EVAL GROUPS

    /**
     * Save or update the group assignment, used to make a linkage from
     * an evaluation to an eval group (course, site, group, evalGroupId, etc...),
     * cannot add assignments if the evaluation is closed<br/>
     * <b>Note:</b> cannot change the group or the evaluation once the object is created,
     * you can change any other property at any time<br/>
     * Use {@link #canCreateAssignEval(String, Long)} or 
     * {@link #canControlAssignGroup(String, Long)} to check 
     * if user can do this and avoid possible exceptions <br/>
     * this will create the associated user assignments automatically
     * 
     * @param assignGroup the object to save, represents a link from a single group to an evaluation
     * @param userId (OPTIONAL) the acting user, normally the current user, internal user id (not username)
     */
    public void saveAssignGroup(EvalAssignGroup assignGroup, String userId);

    /**
     * Remove the evalGroupId assignment, used to make a linkage from
     * an evaluation to an eval group (course, site, group, etc...),
     * represents a link from a single group to an evaluation,
     * can only remove assignments if the evaluation is still in queue,
     * also removes the evaluation if there are no assignments remaining<br/>
     * Use {@link #canControlAssignGroup(String, Long)} to check if user can do this
     * and avoid possible exceptions <br/>
     * this will remove the associated user assignments automatically
     * 
     * @param assignGroupId the id of an {@link EvalAssignGroup} object to remove
     * @param userId (OPTIONAL) the acting user, normally the current user, internal user id (not username)
     */
    public void deleteAssignGroup(Long assignGroupId, String userId);


    // EVAL CATEGORIES

    /**
     * Get all current evalaution categories in the system,
     * evaluation categories allow the evaluation owner to categorize their evaluations
     * 
     * @param userId the internal user id (not username), may be null, if not null then only
     * get the categories for evaluations owned by this user (i.e. categories they created)
     * @return an array of categories, sorted in alphabetic order
     */
    public String[] getEvalCategories(String userId);

    /**
     * Get all evaluations which are tagged with a specific category
     * 
     * @param evalCategory a string representing an evaluation category
     * @param userId (OPTIONAL) the acting user, normally the current user, internal user id (not username),
     * may be null, if not null then only get the evaluations in this category which are accessible to this user
     * @return a list of {@link EvalEvaluation} objects
     */
    public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory, String userId);


    // EMAIL TEMPLATES

    /**
     * Save or update an email template, don't forget to associate it
     * with the evaluation and save that separately<br/>
     * Use {@link #canControlEmailTemplate(String, Long, Long)} or
     * {@link #canControlEmailTemplate(String, Long, String)} to check
     * if user can update this template and avoid possible exceptions
     * 
     * @param EmailTemplate emailTemplate object to be saved
     * @param userId the acting user, normally the current user, internal user id (not username)
     */
    public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId);

    /**
     * Remove an email template if the user has the permissions to remove it,
     * will replace the usage of this template with default templates,
     * cannot remove default templates
     * 
     * @param emailTemplateId a unique id for an {@link EvalEmailTemplate}
     * @param userId the acting user, normally the current user, internal user id (not username)
     */
    public void removeEmailTemplate(Long emailTemplateId, String userId);

    /**
     * Assign an email template to an evaluation,
     * setting the emailTemplateId to null (and including an emailTemplateTypeConstant) or 
     * using a default template id will clear the association
     * 
     * @param emailTemplateId the unique id of an {@link EvalEmailTemplate}
     * @param evaluationId the unique id of an {@link EvalEvaluation}
     * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants} to indicate the type
     * @param userId the acting user, normally the current user, internal user id (not username)
     */
    public void assignEmailTemplate(Long emailTemplateId, Long evaluationId, String emailTemplateTypeConstant, String userId);

}
