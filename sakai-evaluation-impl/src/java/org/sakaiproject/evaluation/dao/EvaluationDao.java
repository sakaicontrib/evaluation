/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Do NOT use this class outside the LOGIC layer
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvaluationDao {

    public static final int COMPARE_EQUALS = 0;
    public static final int COMPARE_GREATER = 1;
    public static final int COMPARE_LESS = 2;
    public static final int COMPARE_LIKE = 3;
    public static final int COMPARE_NULL = 4;
    public static final int COMPARE_NOT_NULL = 5;
    public static final int COMPARE_NOT_EQUALS = 6;

    /**
     * Cause an immediate commit of the current transaction
     */
    public void forceCommit();

    /**
     * Cause an immediate rollback of the current transaction
     */
    public void forceRollback();

    /**
     * This method will check the database for inconsistencies (mostly as a result of upgrades),
     * and will apply any fixes that it can 
     */
    public void fixupDatabase();

    /**
     * Find a persisted entity by id.
     *
     * @param type entity class
     * @param id persistent id
     * @return matching entity, or null when none exists
     */
    public <T> T findById(Class<T> type, Serializable id);

    /**
     * Load all persisted entities of a type.
     *
     * @param type entity class
     * @return matching entities
     */
    public <T> List<T> findAll(Class<T> type);

    /**
     * Count all persisted entities of a type.
     *
     * @param type entity class
     * @return matching entity count
     */
    public <T> int countAll(Class<T> type);

    /**
     * Persist a new entity.
     *
     * @param object entity to persist
     */
    public void create(Object object);

    /**
     * Save a new or changed entity.
     *
     * @param object entity to save
     */
    public void save(Object object);

    /**
     * Update a persistent entity.
     *
     * @param object entity to update
     */
    public void update(Object object);

    /**
     * Delete a persistent entity.
     *
     * @param object entity to delete
     */
    public void delete(Object object);

    /**
     * Delete a persistent entity by type and id.
     *
     * @param entityClass entity class
     * @param id persistent id
     * @return true when an entity was deleted
     */
    public <T> boolean delete(Class<T> entityClass, Serializable id);

    /**
     * Count persisted evaluation system configuration values.
     *
     * @return the number of stored {@link EvalConfig} rows
     */
    public int countEvalConfigs();

    /**
     * Find a single evaluation system configuration value by its unique name.
     *
     * @param name the config name
     * @return the matching config, or null if it does not exist
     */
    public EvalConfig getEvalConfigByName(String name);

    /**
     * Load all persisted evaluation system configuration values.
     *
     * @return all stored configs
     */
    public List<EvalConfig> getAllEvalConfigs();

    /**
     * Count persisted configuration values whose names are in the supplied list.
     *
     * @param names config names
     * @return number of matching config rows
     */
    public int countEvalConfigsByNames(String[] names);

    /**
     * Count persisted evaluation email templates with a non-null default type.
     *
     * @return number of default email-template rows
     */
    public int countDefaultEmailTemplates();

    /**
     * Load persisted evaluation email templates with a non-null default type.
     *
     * @return default email templates
     */
    public List<EvalEmailTemplate> getDefaultEmailTemplates();

    /**
     * Load evaluation email templates using the filters exposed by the service API.
     *
     * @param ownerUserId optional owner user id; null returns templates for all owners
     * @param emailTemplateType optional template type
     * @param includeDefaultsOnly optional default-template filter; true returns defaults,
     * false returns non-defaults, null returns both
     * @return matching email templates
     */
    public List<EvalEmailTemplate> getEmailTemplates(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly);

    /**
     * Find the default evaluation email template for a template type.
     *
     * @param emailTemplateType template type/default type
     * @return the matching default email template, or null if none exists
     */
    public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateType);

    /**
     * Find an evaluation email template by external id.
     *
     * @param eid external id
     * @return the matching template, or null if none exists
     */
    public EvalEmailTemplate getEmailTemplateByEid(String eid);

    /**
     * Load evaluations that reference an email template in one of the evaluation
     * email-template slots.
     *
     * @param emailTemplateId email template id
     * @param emailTemplateType one of the evaluation email template constants
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType);

    /**
     * Count evaluations that reference an email template in one of the evaluation
     * email-template slots.
     *
     * @param emailTemplateId email template id
     * @param emailTemplateType one of the evaluation email template constants
     * @return matching evaluation count
     */
    public int countEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType);

    /**
     * Delete evaluation email templates as a batch.
     *
     * @param emailTemplates email templates to delete
     */
    public void deleteEmailTemplates(Set<EvalEmailTemplate> emailTemplates);

    /**
     * Count persisted evaluation scales.
     *
     * @return scale count
     */
    public int countEvalScales();

    /**
     * Find an evaluation scale by external id.
     *
     * @param eid external id
     * @return the matching scale, or null if none exists
     */
    public EvalScale getScaleByEid(String eid);

    /**
     * Load evaluation scales by persistent ids.
     *
     * @param scaleIds scale ids
     * @return matching scales
     */
    public List<EvalScale> getScalesByIds(Long[] scaleIds);

    /**
     * Load evaluation scales whose mode has not been initialized.
     *
     * @return scales with a null mode
     */
    public List<EvalScale> getScalesWithNullMode();

    /**
     * Save or update evaluation scales as a batch.
     *
     * @param scales scales to save
     */
    public void saveScales(Set<EvalScale> scales);

    /**
     * Delete evaluation scales as a batch.
     *
     * @param scales scales to delete
     */
    public void deleteScales(Set<EvalScale> scales);

    /**
     * Count persisted evaluation items.
     *
     * @return item count
     */
    public int countEvalItems();

    /**
     * Find an evaluation item by external id.
     *
     * @param eid external id
     * @return the matching item, or null if none exists
     */
    public EvalItem getItemByEid(String eid);

    /**
     * Load evaluation items with an auto-use tag, ordered by id.
     *
     * @param autoUseTag auto-use tag
     * @return matching items
     */
    public List<EvalItem> getItemsByAutoUseTag(String autoUseTag);

    /**
     * Load visible evaluation items available to a user for the supplied sharing scope.
     *
     * @param userId owner user id for private items; null includes all private items
     * @param sharingConstants sharing constants to include
     * @param filter optional text filter
     * @param includeExpert true to include expert items
     * @return matching items ordered by id
     */
    public List<EvalItem> getItemsForUser(String userId, String[] sharingConstants, String filter, boolean includeExpert);

    /**
     * Load evaluation items by persistent ids.
     *
     * @param itemIds item ids
     * @return matching items
     */
    public List<EvalItem> getItemsByIds(Long[] itemIds);

    /**
     * Load evaluation items that use a scale.
     *
     * @param scaleId scale id
     * @return matching items
     */
    public List<EvalItem> getItemsUsingScale(Long scaleId);

    /**
     * Save or update evaluation items as a batch.
     *
     * @param items items to save
     */
    public void saveItems(Set<EvalItem> items);

    /**
     * Delete evaluation items as a batch.
     *
     * @param items items to delete
     */
    public void deleteItems(Set<EvalItem> items);

    /**
     * Count persisted evaluation item groups.
     *
     * @return item group count
     */
    public int countEvalItemGroups();

    /**
     * Find an evaluation item group by title.
     *
     * @param title item group title
     * @return the matching item group, or null if none exists
     */
    public EvalItemGroup getItemGroupByTitle(String title);

    /**
     * Count evaluations matching the supplied persistent ids.
     *
     * @param evaluationIds evaluation ids
     * @return number of matching evaluations
     */
    public int countEvaluationsByIds(Long[] evaluationIds);

    /**
     * Count evaluations matching the supplied persistent id.
     *
     * @param evaluationId evaluation id
     * @return 1 if the evaluation exists, otherwise 0
     */
    public int countEvaluationById(Long evaluationId);

    /**
     * Find an evaluation by external id.
     *
     * @param eid external id
     * @return the matching evaluation, or null if none exists
     */
    public EvalEvaluation getEvaluationByEid(String eid);

    /**
     * Count persisted templates by id.
     *
     * @param templateId template id
     * @return 1 if the template exists, otherwise 0
     */
    public int countTemplateById(Long templateId);

    /**
     * Count non-partial, non-deleted evaluations that use a template.
     *
     * @param templateId template id
     * @return matching evaluation count
     */
    public int countEvaluationsByTemplateId(Long templateId);

    /**
     * Load non-partial, non-deleted evaluations that use a template.
     *
     * @param templateId template id
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId);

    /**
     * Load non-partial, non-deleted evaluations for a term.
     *
     * @param termId term id
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsByTermId(String termId);

    /**
     * Load evaluations by state.
     *
     * @param state evaluation state
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsByState(String state);

    /**
     * Load evaluations that are not in terminal viewable/deleted states.
     *
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsNotViewableOrDeleted();

    /**
     * Load evaluations for a category ordered by start date.
     *
     * @param evalCategory evaluation category
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory);

    /**
     * Find an assigned user by external id.
     *
     * @param eid external id
     * @return the matching assignment, or null if none exists
     */
    public EvalAssignUser getAssignUserByEid(String eid);

    /**
     * Count groups assigned to an evaluation, optionally requiring instructor approval.
     *
     * @param evaluationId evaluation id
     * @param includeUnApproved true to include unapproved groups
     * @return matching group count
     */
    public int countEvaluationGroups(Long evaluationId, boolean includeUnApproved);

    /**
     * Find an assigned group by external id.
     *
     * @param eid external id
     * @return the matching assignment, or null if none exists
     */
    public EvalAssignGroup getAssignGroupByEid(String eid);

    /**
     * Count evaluator assignments for an evaluation, optionally constrained to eval groups,
     * excluding removed assignments.
     *
     * @param evaluationId evaluation id
     * @param evalGroupIds optional eval group ids
     * @return matching evaluator assignment count
     */
    public int countParticipantsForEval(Long evaluationId, String[] evalGroupIds);

    /**
     * Load approved group assignments for an evaluation, optionally constrained to one eval group id.
     *
     * @param evaluationId evaluation id
     * @param evalGroupId optional eval group id
     * @return matching approved group assignments
     */
    public List<EvalAssignGroup> getApprovedAssignGroupsForEvaluation(Long evaluationId, String evalGroupId);

    /**
     * Count approved group assignments for an evaluation constrained to eval group ids.
     *
     * @param evaluationId evaluation id
     * @param evalGroupIds eval group ids
     * @return matching approved group assignment count
     */
    public int countApprovedAssignGroupsForEvaluation(Long evaluationId, String[] evalGroupIds);

    /**
     * Find a group assignment by evaluation and eval group id.
     *
     * @param evaluationId evaluation id
     * @param evalGroupId eval group id
     * @return matching group assignment, or null if none exists
     */
    public EvalAssignGroup getAssignGroupByEvalAndGroupId(Long evaluationId, String evalGroupId);

    /**
     * Load non-empty hierarchy assignments for an evaluation ordered by id.
     *
     * @param evaluationId evaluation id
     * @return matching hierarchy assignments
     */
    public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId);

    /**
     * Load group assignments for evaluations, optionally constrained by approval and hierarchy origin.
     *
     * @param evaluationIds evaluation ids
     * @param includeUnApproved true to include unapproved groups
     * @param includeHierarchyGroups null for all, true for node-created groups, false for direct groups
     * @return matching group assignments ordered by eval group id
     */
    public List<EvalAssignGroup> getAssignGroupsForEvals(Long[] evaluationIds, boolean includeUnApproved, Boolean includeHierarchyGroups);

    /**
     * Count group assignments matching an evaluation and eval group id.
     *
     * @param evaluationId evaluation id
     * @param evalGroupId eval group id
     * @return matching group assignment count
     */
    public int countAssignGroupsByEvalAndGroupId(Long evaluationId, String evalGroupId);

    /**
     * Delete all user, group, and hierarchy assignments associated with an evaluation.
     *
     * @param evaluationId evaluation id
     */
    public void deleteAssignmentsForEvaluation(Long evaluationId);

    /**
     * Save hierarchy node assignments and their expanded group assignments.
     *
     * @param assignHierarchies hierarchy assignments to save
     * @param assignGroups group assignments to save
     */
    public void saveAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups);

    /**
     * Load hierarchy assignments by persistent ids.
     *
     * @param assignHierarchyIds hierarchy assignment ids
     * @return matching hierarchy assignments
     */
    public List<EvalAssignHierarchy> getAssignHierarchiesByIds(Long[] assignHierarchyIds);

    /**
     * Load node-created group assignments for an evaluation and node ids.
     *
     * @param evaluationId evaluation id
     * @param nodeIds hierarchy node ids
     * @return matching group assignments
     */
    public List<EvalAssignGroup> getAssignGroupsByEvalAndNodeIds(Long evaluationId, Set<String> nodeIds);

    /**
     * Delete hierarchy node assignments and their expanded group assignments.
     *
     * @param assignHierarchies hierarchy assignments to delete
     * @param assignGroups group assignments to delete
     */
    public void deleteAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups);

    /**
     * Save or update user assignment rows.
     *
     * @param assignUsers user assignments to save
     */
    public void saveAssignUsers(Set<EvalAssignUser> assignUsers);

    /**
     * Delete user assignment rows by persistent ids.
     *
     * @param assignUserIds user assignment ids
     */
    public void deleteAssignUsersByIds(Long[] assignUserIds);

    /**
     * Delete user assignments tied to an assigned group, excluding a status that should be preserved.
     *
     * @param assignGroupId assigned group id
     * @param excludedStatus status to preserve
     * @return number of rows removed
     */
    public int deleteAssignUsersByAssignGroupIdExcludingStatus(Long assignGroupId, String excludedStatus);

    /**
     * Load a response for an evaluation/user/group unique key.
     *
     * @param evaluationId evaluation id
     * @param userId internal user id
     * @param evalGroupId eval group id
     * @return matching responses
     */
    public List<EvalResponse> getEvaluationResponsesForUserAndGroup(Long evaluationId, String userId, String evalGroupId);

    /**
     * Load responses for evaluation ids, optionally constrained to one owner, ordered by id.
     *
     * @param evaluationIds evaluation ids
     * @param ownerUserId optional owner user id; null returns all owners
     * @param completed optional completion filter; null returns complete and incomplete
     * @return matching responses
     */
    public List<EvalResponse> getEvaluationResponsesForUser(Long[] evaluationIds, String ownerUserId, Boolean completed);

    /**
     * Count responses for an evaluation, optionally constrained to one eval group and completion state.
     *
     * @param evaluationId evaluation id
     * @param evalGroupId optional eval group id
     * @param completed optional completion filter
     * @return response count
     */
    public int countResponses(Long evaluationId, String evalGroupId, Boolean completed);

    /**
     * Load responses for an evaluation, optionally constrained to eval groups and completion state, ordered by id.
     *
     * @param evaluationId evaluation id
     * @param evalGroupIds optional eval group ids
     * @param completed optional completion filter
     * @return matching responses
     */
    public List<EvalResponse> getEvaluationResponses(Long evaluationId, String[] evalGroupIds, Boolean completed);

    /**
     * Load responses for evaluation ids, optionally constrained to one owner, eval groups,
     * and completion state.
     *
     * @param evaluationIds evaluation ids
     * @param ownerUserId optional owner user id; null returns all owners
     * @param evalGroupIds optional eval group ids
     * @param completed optional completion filter
     * @return matching responses
     */
    public List<EvalResponse> getEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed);

    /**
     * Count responses for evaluation ids, optionally constrained to one owner, eval groups,
     * and completion state.
     *
     * @param evaluationIds evaluation ids
     * @param ownerUserId optional owner user id; null returns all owners
     * @param evalGroupIds optional eval group ids
     * @param completed optional completion filter
     * @return matching response count
     */
    public int countEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed);

    /**
     * Save a response and its answers in the order required by the answer foreign key.
     *
     * @param response response to save
     * @param answers answers to save after the response
     */
    public void saveResponseAndAnswers(EvalResponse response, Set<EvalAnswer> answers);

    /**
     * Load template items associated with a hierarchy node.
     *
     * @param nodeId hierarchy node id
     * @return matching template items
     */
    public List<EvalTemplateItem> getTemplateItemsByHierarchyNodeId(String nodeId);

    /**
     * Find an evaluation template by external id.
     *
     * @param eid external id
     * @return the matching template, or null if none exists
     */
    public EvalTemplate getTemplateByEid(String eid);

    /**
     * Load visible scale-mode scales available to a user for the supplied sharing scope.
     *
     * @param userId owner user id for private scales; null includes all private scales
     * @param sharingConstants sharing constants to include
     * @return matching scales ordered by title
     */
    public List<EvalScale> getScalesForUser(String userId, String[] sharingConstants);

    /**
     * Load templates with an auto-use tag, ordered by id.
     *
     * @param autoUseTag auto-use tag
     * @return matching templates
     */
    public List<EvalTemplate> getTemplatesByAutoUseTag(String autoUseTag);

    /**
     * Load visible standard templates available to a user for the supplied sharing scope.
     *
     * @param userId owner user id for private templates; null includes all private templates
     * @param sharingConstants sharing constants to include
     * @param includeEmpty true to include templates with no template items
     * @return matching templates ordered by sharing and title
     */
    public List<EvalTemplate> getTemplatesForUser(String userId, String[] sharingConstants, boolean includeEmpty);

    /**
     * Find a template item by external id.
     *
     * @param eid external id
     * @return the matching template item, or null if none exists
     */
    public EvalTemplateItem getTemplateItemByEid(String eid);

    /**
     * Load template items with an auto-use tag, ordered by display order and id.
     *
     * @param autoUseTag auto-use tag
     * @return matching template items
     */
    public List<EvalTemplateItem> getTemplateItemsByAutoUseTag(String autoUseTag);

    /**
     * Load template items by persistent ids.
     *
     * @param templateItemIds template item ids
     * @return matching template items
     */
    public List<EvalTemplateItem> getTemplateItemsByIds(Long[] templateItemIds);

    /**
     * Load distinct templates that contain a template item using an item.
     *
     * @param itemId item id
     * @return matching templates
     */
    public List<EvalTemplate> getTemplatesUsingItem(Long itemId);

    /**
     * Load template items with missing template and item links.
     *
     * @return orphaned template items
     */
    public List<EvalTemplateItem> getOrphanedTemplateItems();

    /**
     * Count top-level template items in a template.
     *
     * @param templateId template id
     * @return matching template item count
     */
    public int countTopLevelTemplateItems(Long templateId);

    /**
     * Count child template items in a template block.
     *
     * @param templateId template id
     * @param blockId block parent template item id
     * @return matching template item count
     */
    public int countBlockChildTemplateItems(Long templateId, Long blockId);

    /**
     * Load child template items for a block parent ordered by display order.
     *
     * @param blockParentId block parent template item id
     * @return child template items
     */
    public List<EvalTemplateItem> getBlockChildTemplateItems(Long blockParentId);

    /**
     * Save a template item and update its item/template link owners.
     *
     * @param templateItem template item to save
     * @param item linked item
     * @param template linked template
     */
    public void saveTemplateItemWithLinks(EvalTemplateItem templateItem, EvalItem item, EvalTemplate template);

    /**
     * Count evaluations whose title matches an HQL LIKE pattern.
     *
     * @param titlePattern title LIKE pattern
     * @return matching evaluation count
     */
    public int countEvaluationsByTitle(String titlePattern);

    /**
     * Load evaluations whose title matches an HQL LIKE pattern.
     *
     * @param titlePattern title LIKE pattern
     * @param orderProperty supported evaluation property to order by
     * @param startResult zero-based start row
     * @param maxResults maximum rows
     * @return matching evaluations
     */
    public List<EvalEvaluation> getEvaluationsByTitle(String titlePattern, String orderProperty, int startResult, int maxResults);

    /**
     * Save template items as a batch.
     *
     * @param templateItems template items to save
     */
    public void saveTemplateItems(Set<EvalTemplateItem> templateItems);

    /**
     * Load eval-group node mappings by node id, ordered by persistent id.
     *
     * @param nodeIds hierarchy node ids
     * @return matching eval-group node mappings
     */
    public List<EvalGroupNodes> getEvalGroupNodesByNodeIds(String[] nodeIds);

    /**
     * Load all users assigned as evaluation administrators.
     *
     * @return all evaluation admin records
     */
    public List<EvalAdmin> getAllEvalAdmins();

    /**
     * Find an evaluation administrator assignment by user id.
     *
     * @param userId the internal user id
     * @return the matching admin record, or null if the user is not an eval admin
     */
    public EvalAdmin getEvalAdminByUserId(String userId);

    /**
     * Load all hierarchy rules.
     *
     * @return all hierarchy rules
     */
    public List<EvalHierarchyRule> getAllHierarchyRules();

    /**
     * Find a hierarchy rule by id.
     *
     * @param ruleId the rule id
     * @return the matching hierarchy rule, or null if it does not exist
     */
    public EvalHierarchyRule getHierarchyRuleById(Long ruleId);

    /**
     * Load all hierarchy rules assigned to a node.
     *
     * @param nodeId the hierarchy node id
     * @return hierarchy rules for the node
     */
    public List<EvalHierarchyRule> getHierarchyRulesByNodeId(Long nodeId);

    /**
     * Delete hierarchy rules as a batch.
     *
     * @param rules hierarchy rules to delete
     */
    public void deleteHierarchyRules(Set<EvalHierarchyRule> rules);

    /**
     * Find an adhoc user by username.
     *
     * @param username the unique login name
     * @return the matching adhoc user, or null if none exists
     */
    public EvalAdhocUser getAdhocUserByUsername(String username);

    /**
     * Find an adhoc user by email address.
     *
     * @param email the unique email address
     * @return the matching adhoc user, or null if none exists
     */
    public EvalAdhocUser getAdhocUserByEmail(String email);

    /**
     * Load all adhoc users.
     *
     * @return all stored adhoc users
     */
    public List<EvalAdhocUser> getAllAdhocUsers();

    /**
     * Load adhoc users by their persistent ids.
     *
     * @param ids persistent adhoc user ids
     * @return matching adhoc users
     */
    public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids);

    /**
     * Load adhoc groups owned by a user, ordered by title.
     *
     * @param userId the internal user id of the owner
     * @return matching adhoc groups
     */
    public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId);

    /**
     * Method to find all evals which have no user assignments
     * @return the list of all evaluations without any user assignments
     */
    public List<EvalEvaluation> getEvalsWithoutUserAssignments();

    /**
     * Get the list of all participants for an evaluation,
     * can limit it to a single group which is assigned to the evaluation and
     * can filter the results to only include some of the participants,
     * this should be used in all cases where  <br/>
     * Will not include any assignments with {@link EvalAssignUser#STATUS_REMOVED}
     * <br/>
     * You must include at least one of the following (non-null):
     * evaluationId OR userId
     * <br/> Uses the current user for permissions checks
     * 
     * @param evaluationId (OPTIONAL) the unique id of an {@link EvalEvaluation} object,
     * if this is null then assignments from any evaluation are returned
     * @param userId (OPTIONAL) limit the returned assignments to those for this user,
     * will return assignments for any user if this is null
     * @param evalGroupIds (OPTIONAL) an array of unique IDs for eval groups, 
     * if this is null or empty then results include participants from the entire evaluation,
     * NOTE: these ids are not validated
     * @param assignTypeConstant (OPTIONAL) a constant to indicate which types of assignment participants to include,
     * use the TYPE_* constants from {@link EvalAssignUser}, default (null) is to include all types of assignments
     * @param assignStatusConstant (OPTIONAL) a constant to indicate which status of assignment participants to include,
     * use the STATUS_* constants from {@link EvalAssignUser}, to include users with any status use {@link #STATUS_ANY}, 
     * default (null) is to include {@link EvalAssignUser#STATUS_LINKED} and {@link EvalAssignUser#STATUS_UNLINKED},
     * @param includeConstant (OPTIONAL) a constant to indicate what users should be retrieved, 
     * EVAL_INCLUDE_* from {@link EvalConstants}, default (null) is {@link EvalConstants#EVAL_INCLUDE_ALL},
     * <b>NOTE</b>: if this is non-null it will filter users to type {@link EvalAssignUser#TYPE_EVALUATOR} automatically
     * regardless of what the assignTypeConstant is set to
     * @param evalStateConstant (OPTIONAL) this is the state of the evals to limit the results to,
     * this should be one of the EVALUATION_STATE_* constants (e.g. {@link EvalConstants#EVALUATION_STATE_ACTIVE}),
     * if null then evaluations with any state are included
     * @return the list of user assignments ({@link EvalAssignUser} objects)
     * @throws IllegalArgumentException if all inputs are null or the inputs are invalid
     */
    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId,
            String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, 
            String includeConstant, String evalStateConstant);

    /**
     * Returns all evaluations which the given user can take,
     * can also include anonymous evaluations and filter on active/approved
     * 
     * @param userId the internal user id for the user who we are checking evals they can take
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, only include evaluations authcontrol = anon, 
     * if false, include any evaluations with authcontrol != anon,
     * if null, include all evaluations regardless of authcontrol
     * @param startResult
     * @param maxResults
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults);

    /**
     * A general method for fetching entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param order a string array of property names to order by
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @param start the returned entity to start with (for paging), 0 means start with the first one
     * @param limit the total number of entities to return, 0 means return all
     * @return a list of entities
     */
    public <T> List<T> getSharedEntitiesForUser(Class<T> entityClass, String userId, String[] sharingConstants,
            String[] props, Object[] values, int[] comparisons, String[] order, String[] options, int start, int limit);

    /**
     * A general method for counting entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @return a count of the matching entities
     * @see #getSharedEntitiesForUser(Class, String, String[], String[], Object[], int[], String[], String[])
     */
    public <T> int countSharedEntitiesForUser(Class<T> entityClass, String userId, String[] sharingConstants,
            String[] props, Object[] values, int[] comparisons, String[] options);

    /**
     * Returns all evaluation objects associated with the input groups,
     * can also include anonymous evaluations
     * 
     * @param evalGroupIds an array of eval group IDs to get associated evals for, 
     * can be empty or null but only anonymous evals will be returned
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, include assigned and anonymous evaluations (only anonymous evals if evalGroupIds is null), 
     * if false, only include assigned evals which are not also anonymous,
     * if null include only assigned evaluations
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds,
            Boolean activeOnly, Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults);

    /**
     * Get a set of evaluations based on the owner and their groups
     * 
     * @param userId internal user id, owner of the evaluations, if null then do not filter on the owner id
     * @param evalGroupIds an array of eval group IDs to get associated evals for, can be empty or null to get all evals
     * @param recentClosedDate only return evaluations which closed after this date
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @param includePartial if true then partial evals are included, otherwise only fully created evals
     * @return a List of EvalEvaluation objects sorted by stop date, title, and id
     */
    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
            String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults, boolean includePartial);

    /**
     * Returns all answers to the given item associated with 
     * responses which are associated with the given evaluation,
     * only returns the answers for completed responses
     * 
     * @param evalId the id of the evaluation you want answers from
     * @param evalGroupIds an array of eval group IDs to return answers for,
     * if null then just return answers for all groups
     * @param templateItemIds the ids of the template items you want answers for,
     * if null then return answers for all template items
     * @return a list of EvalAnswer objects or empty list if none found
     */
    public List<EvalAnswer> getAnswers(Long evalId, String[] evalGroupIds, Long[] templateItemIds);

    /**
     * Removes a group of templateItems and updates all related items 
     * and templates at the same time (inside one transaction)
     * 
     * @param templateItems the array of {@link EvalTemplateItem} to remove 
     */
    public void removeTemplateItems(EvalTemplateItem[] templateItems);

    /**
     * Get item groups contained within a specific group<br/>
     * <b>Note:</b> If parent is null then get all the highest level groups
     * 
     * @param parentItemGroupId the unique id of an {@link EvalItemGroup}, if null then get all the highest level groups
     * @param userId the internal user id (not username)
     * @param includeEmpty if true then include all groups (even those with nothing in them), else return only groups
     * which contain other groups or other items
     * @param includeExpert if true then include expert groups only, else include non-expert groups only
     * @return a List of {@link EvalItemGroup} objects, ordered by title alphabetically
     */
    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId,
            boolean includeEmpty, boolean includeExpert);
    /**
     * Returns the eval itemgroup id for an item.  If an item is not part of an itemgroup, null.
     * 
     * @param itemId the unique id of the EvalItem object
     * @param userId the internal user Id (of the owner)
     * @return
     */
    public Long getItemGroupIdByItemId(Long itemId, String userId);
    
    /**
     * Get all the templateItems for this template limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes
     * 
     * @param templateId the unique id of an EvalTemplate object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder
     */
    public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds,
            String[] instructorIds, String[] groupIds);

    /**
     * Get all the templateItems for this evaluation limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes, should be ordered in the list by the proper display order
     * 
     * @param evalId the unique id of an {@link EvalEvaluation} object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder and template
     */
    public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds,
            String[] instructorIds, String[] groupIds);

    /**
     * Returns list of response ids for a given evaluation
     * and for specific groups and for specific users if desired,
     * can limit to only completed responses
     *
     * @param evalId the id of the evaluation you want the response ids for
     * @param evalGroupIds an array of eval group IDs to return response ids for,
     * if null or empty then return responses ids for all evalGroups associated with this eval
     * @param userIds an array of internal userIds to return responses for,
     * if null or empty then return responses ids for all users
     * @param completed if true only return the completed responses, 
     * if false only return the incomplete responses,
     * if null then return all responses
     * @return a list of response ids (Long) for {@link EvalResponse} objects
     */
    public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds, Boolean completed);

    /**
     * Removes an array of responses and all their associated answers at
     * the same time (in a single transaction)<br/>
     * Use {@link #getResponseIds(Long, String[], String[], Boolean)} to get the set of responseIds to remove<br/>
     * <b>WARNING:</b> This does not check permissions for removal of responses so you should
     * be sure to check that responses can be removed (system setting) and that they can be removed for this evaluation and user
     * 
     * @param responseIds the array of ids for {@link EvalResponse} objects to remove
     */
    public void removeResponses(Long[] responseIds);

    /**
     * Get a list of evaluation categories
     * 
     * @param userId the internal user id (not username), if null then return all categories
     * @return a list of {@link String}
     */
    public List<String> getEvalCategories(String userId);

    /**
     * Get the node which contains this evalgroup,
     * Note: this will always only return a single node so if an evalgroup is assigned to multiple
     * nodes then only the first one will be returned
     * @param evalGroupId a unique id for an eval group
     * @return a unique id for the containing node or null if none found
     */
    public String getNodeIdForEvalGroup(String evalGroupId);

    /**
     * Get all the users who have completely responded to an evaluation 
     * and optionally within group(s) assigned to that evaluation
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param evalGroupIds the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all responses for this evaluation
     * @param completed if true only return the completed responses, 
     * if false only return the incomplete responses,
     * if null then return all responses
     * @return a set of internal userIds
     */
    public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds, Boolean completed);

    /**
     * Get all the evalGroupIds for an evaluation which are viewable by
     * the input permission,
     * can limit the eval groups to check by inputing an array of evalGroupIds<br/>
     * <b>NOTE:</b> If you input evalGroupIds then the returned set will always be
     * a subset (the same size or smaller) of the input
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param permissionConstant a permission constant which is 
     * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
     * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
     * other permissions will return no results
     * @param evalGroupIds the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all ids for this evaluation
     * @return a set of eval group ids which allow viewing by the specified permission
     */
    public Set<String> getViewableEvalGroupIds(Long evaluationId, String permissionConstant,
            String[] evalGroupIds);

    /**
     * Get adhoc groups for a user and permission, 
     * this is a way to check the perms for a user
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission constant which is 
     * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
     * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
     * other permissions will return no results
     * @return a list of adhoc groups for which this user has this permission
     */
    public List<EvalAdhocGroup> getEvalAdhocGroupsByUserAndPerm(String userId,
            String permissionConstant);

    /**
     * Check if a user has a specified permission/role within an adhoc group
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission string PERM constant (from this API),
     * <b>Note</b>: only take evaluation and be evaluated are supported
     * @param evalGroupId the unique id of an eval group
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant,
            String evalGroupId);

    /**
     * Set lock state if scale is not already at that lock state
     * 
     * @param scale
     * @param lockState if true then lock this scale, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockScale(EvalScale scale, Boolean lockState);

    /**
     * Set lock state if item is not already at that lock state,
     * lock associated scale if it does not match OR
     * unlock associated scale if not locked by other item(s) 
     * 
     * @param item
     * @param lockState if true then lock this item, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockItem(EvalItem item, Boolean lockState);

    /**
     * Set lock state if template is not already at that lock state,
     * lock associated item(s) if they do not match OR
     * unlock associated item(s) if not locked by other template(s) 
     * 
     * @param template
     * @param lockState if true then lock this template, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockTemplate(EvalTemplate template, Boolean lockState);

    /**
     * Lock evaluation if not already locked,
     * lock associated template(s) if not locked OR
     * unlock associated template(s) if not locked by other evaluations
     * 
     * @param evaluation
     * @param lockState if true then lock this evaluations, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockEvaluation(EvalEvaluation evaluation, Boolean lockState);

    /**
     * @param scaleId
     * @return true if this scale is used in any items
     */
    public boolean isUsedScale(Long scaleId);

    /**
     * @param itemId
     * @return true if this item is used in any template (via a template item)
     */
    public boolean isUsedItem(Long itemId);

    /**
     * @param templateId
     * @return true if this template is used in any evalautions
     */
    public boolean isUsedTemplate(Long templateId);

    /**
     * Allows a lock to be obtained that is system wide,
     * this is primarily for ensuring something runs on a single server only in a cluster<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param executerId a unique id for the executer of this lock (normally a server id)
     * @param timePeriod the length of time (in milliseconds) that the lock should be valid for,
     * set this very low for non-repeating processes (the length of time the process should take to run)
     * and the length of the repeat period plus the time to run the process for repeating jobs
     * @return true if a lock was obtained, false if not, null if failure
     */
    public Boolean obtainLock(String lockId, String executerId, long timePeriod);

    /**
     * Releases a lock that was being held,
     * this is useful if you know a server is shutting down and you want to release your locks early<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param executerId a unique id for the executer of this lock (normally a server id)
     * @return true if a lock was released, false if not, null if failure
     */
    public Boolean releaseLock(String lockId, String executerId);

    /**
     * Access one page of summary info needed to render consolidated email templates. 
     * The summary info consists of a user-id, a user-eid, a template-id (EmailTemplate.ID) and the earliest 
     * due date of Active evals which use the email template and which the referenced user can take.
     * @param sendingAvailableEmails Should be true if the results will be used to send notifications that new 
     * 		evaluations are opening, and false if they are to be used for reminders. 
     * @param pageSize The maximum number of mappings to return. A mapping consists of a user-id, an email template
     * 		id and a date.
     * @param page The zero-based starting page. In other words, return a page of items beginning at index 
     * 		(pageSize * page).
     * @return A mapping from user-id to data about the evals that user can take. The data for each users is 
     * 		a mapping from string values (EvalConstants.KEY_USER_ID, EvalConstants.KEY_USER_EID, 
     * 		EvalConstants.KEY_EMAIL_TEMPLATE_ID and EvalConstants.KEY_EARLIEST_DUE_DATE) to a String 
     * 		object for EvalConstants.KEY_USER_ID, a String object for EvalConstants.KEY_USER_EID, a Long 
     * 		object for EvalConstants.KEY_EMAIL_TEMPLATE_ID and a Date object forEvalConstants.KEY_EARLIEST_DUE_DATE).  
     */
    public List<Map<String,Object>> getConsolidatedEmailMapping(boolean sendingAvailableEmails, int pageSize, int page);

    /**
     * Build the email processing queue by adding one record for each evalAssignUser record 
     * matching the search criteria.  Search criteria are determined based on the values of 
     * EvalAssignUser.availableEmailSent, EvalAssignUser.reminderEmailSent and 
     * EvalEmailTemplate.emailTemplateType.   
     * @param useAvailableEmailSent Should be true if the availableEmailSent date should be used in selecting records.
     * @param availableEmailSent The date to use if querying by availableEmailSent.
     * @param useReminderEmailSent Should be true if the reminderEmailSent date should be used in selecting records.
     * @param reminderEmailSent The date to use if querying by reminderEmailSent.
     * @param emailTemplateType The type of template (ConsolidatedAvailable or ConsolidateReminder) to find.
     * @return
     */
    public int selectConsolidatedEmailRecipients(boolean useAvailableEmailSent,
            Date availableEmailSent, boolean useReminderEmailSent, Date reminderEmailSent, String emailTemplateType);

    /**
     * Remove all records from the the email processing queue and report the number of items removed.
     * @return 
     */
    public int resetConsolidatedEmailRecipients();

    /**
     * Returns a list of evaluation responses that have been saved but not 
     * submitted (completed)
     * @param activeEvaluationsOnly If true, only responses assigned to an 
     * evaluations that is currently in an Active state will be returned.  If 
     * false, only responses assigned to an evaluations not in an Active state 
     * will be returned.
     * @return a List of EvalResponse objects
     */
    public List<EvalResponse> getResponsesSavedInProgress(boolean activeEvaluationsOnly);

    /**
     * Reports the number of distinct eval groups for which mappings are currently in the email processing queue. 
     * @return
     */
    public int countDistinctGroupsInConsolidatedEmailMapping();

    /**
     * Get a list of site IDs that have a section attached that matches the section title provided
     * 
     * @param sectionTitleWithWildcards - the section title to match on
     * @return a list of site IDs which all have a section attached with the given section title
     */
    public Set<String> getAllSiteIDsMatchingSectionTitle( String sectionTitleWithWildcards );

    /**
     * Get a list of site IDs, where the title of the sites matches the given title (with wildcards in place)
     * 
     * @param siteTitleWithWildcards - the title to match on, with wildcards in place
     * @return a list of site IDs that match the criteria
     */
    public Set<String> getAllSiteIDsMatchingSiteTitle( String siteTitleWithWildcards );
}
