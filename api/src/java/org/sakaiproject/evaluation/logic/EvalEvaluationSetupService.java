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
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles all logic associated with setting up and creating evaluations,
 * this includes all the logic for assigning groups and users to an evaluation
 * and processing the email templates used by an evaluation<br/>
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
    * or a template that contains no items<br/>
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
    * @param userId the internal user id (not username)
    */
   public void saveEvaluation(EvalEvaluation evaluation, String userId);

   /**
    * Delete an evaluation from persistent storage,
    * evaluations that are active or completed cannot be deleted,
    * use {@link #canRemoveEvaluation(String, EvalEvaluation)} to check if
    * the evaluation can be removed if you want to avoid possible exceptions,
    * removes all associated course assignments and email templates 
    * (if they are not default or associated with other evaluations)
    * 
    * @param evaluationId the id of an {@link EvalEvaluation} object
    * @param userId the internal user id (not username)
    */
   public void deleteEvaluation(Long evaluationId, String userId);

   /**
    * Get the evaluations that are currently visible to a user, this should be used
    * to determine evaluations that are visible from an administrative perspective,
    * can limit to recently closed only (closed within 10 days)
    * 
    * @param userId the internal user id (not username)
    * @param recentOnly if true return recently closed evaluations only 
    * (still returns all active and in queue evaluations), if false return all closed evaluations
    * @param showNotOwned if true for a non-admin user, then return all 
    * evaluations which are both owned and not-owned, else only return the owned evaluations.
    * @return a List of {@link EvalEvaluation} objects
    */
   public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned);

   /**
    * Get all evaluations that can be taken by this user,
    * can include only active and only untaken if desired
    * 
    * @param userId the internal user id (not username)
    * @param activeOnly if true, only include active evaluations, if false, include all evaluations
    * @param untakenOnly if true, include only the evaluations which have NOT been taken, 
    * if false, include all evaluations
    * @return a List of {@link EvalEvaluation} objects (sorted by DueDate)
    */
   public List<EvalEvaluation> getEvaluationsForUser(String userId, boolean activeOnly, boolean untakenOnly);


   // EVAL GROUPS

   /**
    * Save or update the group assignment, used to make a linkage from
    * an evaluation to an eval group (course, site, group, evalGroupId, etc...),
    * cannot add assignments if the evaluation is closed<br/>
    * <b>Note:</b> cannot change the group or the evaluation once the object is created,
    * you can change any other property at any time<br/>
    * Use {@link #canCreateAssignEval(String, Long)} or 
    * {@link #canControlAssignGroup(String, Long)} to check 
    * if user can do this and avoid possible exceptions
    * 
    * @param assignGroup the object to save, represents a link from a single group to an evaluation
    * @param userId the internal user id (not username)
    */
   public void saveAssignGroup(EvalAssignGroup assignGroup, String userId);

   /**
    * Remove the evalGroupId assignment, used to make a linkage from
    * an evaluation to an eval group (course, site, group, etc...),
    * represents a link from a single group to an evaluation,
    * can only remove assignments if the evaluation is still in queue,
    * also removes the evaluation if there are no assignments remaining<br/>
    * Use {@link #canControlAssignGroup(String, Long)} to check if user can do this
    * and avoid possible exceptions
    * 
    * @param assignGroupId the id of an {@link EvalAssignGroup} object to remove
    * @param userId the internal user id (not username)
    */
   public void deleteAssignGroup(Long assignGroupId, String userId);


   // HIERARCHY LOGIC

   /**
    * Assigns hierarchy nodes and/or evalgroups to an evaluation and therefore assigns all evalgroups that are located
    * at that hierarchy node, this will not include groups below or above this node so if you want
    * to assign the nodes below you will need to include them in the array
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @param nodeIds unique IDs of a set of hierarchy nodes (null if none to assign)
    * @param evalGroupIds the internal unique IDs for a set of evalGroups (null if none to assign)
    * @return a list of the persisted hierarchy assignments (nodes and groups together)
    */
   public List<EvalAssignHierarchy> addEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds);

   /**
    * Remove all assigned hierarchy nodes with the unique ids specified,
    * also cleanup all the assign groups that are associated underneath these hierarchy nodes
    * @param assignHierarchyIds unique ids of {@link EvalAssignHierarchy} objects
    */
   public void deleteAssignHierarchyNodesById(Long[] assignHierarchyIds);


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
    * @param userId the internal user id (not username), may be null, if not null then only
    * get the evaluations in this category which are accessible to this user
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
    * @param userId the internal user id (not username)
    */
   public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId);

   /**
    * Remove an email template if the user has the permissions to remove it,
    * will replace the usage of this template with default templates,
    * cannot remove default templates
    * 
    * @param emailTemplateId a unique id for an {@link EvalEmailTemplate}
    */
   public void removeEmailTemplate(Long emailTemplateId, String userId);

}
