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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
abstract class EvaluationDaoAuthoringMethods extends EvaluationDaoEmailTemplateMethods {

    public int countEvalScales() {
        Long count = currentSession().createQuery(
                "select count(scale.id) from EvalScale scale",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalScale getScaleByEid(String eid) {
        return findOneByEid(EvalScale.class, eid, "scale");
    }

    public List<EvalScale> getScalesByIds(Long[] scaleIds) {
        if (scaleIds == null) {
            throw new IllegalArgumentException("scaleIds cannot be null");
        }
        if (scaleIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select scale from EvalScale scale where scale.id in (:scaleIds)",
                EvalScale.class)
                .setParameterList("scaleIds", scaleIds)
                .list();
    }

    public List<EvalScale> getScalesWithNullMode() {
        return currentSession().createQuery(
                "select scale from EvalScale scale where scale.mode is null",
                EvalScale.class)
                .list();
    }

    public void saveScales(Set<EvalScale> scales) {
        saveOrUpdateAll(scales);
    }

    public void deleteScales(Set<EvalScale> scales) {
        deleteAll(scales);
    }

    public int countEvalItems() {
        Long count = currentSession().createQuery(
                "select count(item.id) from EvalItem item",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalItem getItemByEid(String eid) {
        return findOneByEid(EvalItem.class, eid, "item");
    }

    public List<EvalItem> getItemsByAutoUseTag(String autoUseTag) {
        if (autoUseTag == null) {
            throw new IllegalArgumentException("autoUseTag cannot be null");
        }
        return currentSession().createQuery(
                "select item from EvalItem item where item.autoUseTag = :autoUseTag order by item.id",
                EvalItem.class)
                .setParameter("autoUseTag", autoUseTag)
                .list();
    }

    public List<EvalItem> getItemsForUser(String userId, String[] sharingConstants, String filter, boolean includeExpert) {
        StringBuilder hql = new StringBuilder(
                "select item from EvalItem item where item.hidden = false "
                + "and item.classification <> :blockParentType ");
        Map<String, Object> params = new HashMap<>();
        params.put("blockParentType", EvalConstants.ITEM_TYPE_BLOCK_PARENT);
        appendSharingPredicate(hql, "item", userId, sharingConstants, params);
        if (!includeExpert) {
            hql.append("and (item.expert is null or item.expert <> :expert) ");
            params.put("expert", Boolean.TRUE);
        }
        if (filter != null && filter.length() > 0) {
            hql.append("and item.itemText like :filter ");
            params.put("filter", "%" + filter + "%");
        }
        hql.append("order by item.id");

        Query<EvalItem> query = currentSession().createQuery(hql.toString(), EvalItem.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.list();
    }

    public List<EvalItem> getItemsByIds(Long[] itemIds) {
        if (itemIds == null) {
            throw new IllegalArgumentException("itemIds cannot be null");
        }
        if (itemIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select item from EvalItem item where item.id in (:itemIds)",
                EvalItem.class)
                .setParameterList("itemIds", itemIds)
                .list();
    }

    public List<EvalItem> getItemsUsingScale(Long scaleId) {
        if (scaleId == null) {
            throw new IllegalArgumentException("scaleId cannot be null");
        }
        return currentSession().createQuery(
                "select item from EvalItem item where item.scale.id = :scaleId",
                EvalItem.class)
                .setParameter("scaleId", scaleId)
                .list();
    }

    public void saveItems(Set<EvalItem> items) {
        saveOrUpdateAll(items);
    }

    public void deleteItems(Set<EvalItem> items) {
        deleteAll(items);
    }

    public int countEvalItemGroups() {
        Long count = currentSession().createQuery(
                "select count(itemGroup.id) from EvalItemGroup itemGroup",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalItemGroup getItemGroupByTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title cannot be null");
        }
        List<EvalItemGroup> itemGroups = currentSession().createQuery(
                "select itemGroup from EvalItemGroup itemGroup where itemGroup.title = :title",
                EvalItemGroup.class)
                .setParameter("title", title)
                .setMaxResults(1)
                .list();
        return itemGroups.isEmpty() ? null : itemGroups.get(0);
    }


    public List<EvalTemplateItem> getTemplateItemsByHierarchyNodeId(String nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId cannot be null");
        }
        return currentSession().createQuery(
                "select templateItem from EvalTemplateItem templateItem where templateItem.hierarchyNodeId = :nodeId",
                EvalTemplateItem.class)
                .setParameter("nodeId", nodeId)
                .list();
    }

    public EvalTemplate getTemplateByEid(String eid) {
        return findOneByEid(EvalTemplate.class, eid, "template");
    }

    public List<EvalScale> getScalesForUser(String userId, String[] sharingConstants) {
        StringBuilder hql = new StringBuilder(
                "select scale from EvalScale scale where scale.mode = :mode and scale.hidden = false ");
        Map<String, Object> params = new HashMap<>();
        params.put("mode", EvalConstants.SCALE_MODE_SCALE);
        appendSharingPredicate(hql, "scale", userId, sharingConstants, params);
        hql.append("order by scale.title");

        Query<EvalScale> query = currentSession().createQuery(hql.toString(), EvalScale.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.list();
    }

    public List<EvalTemplate> getTemplatesByAutoUseTag(String autoUseTag) {
        if (autoUseTag == null) {
            throw new IllegalArgumentException("autoUseTag cannot be null");
        }
        return currentSession().createQuery(
                "select template from EvalTemplate template where template.autoUseTag = :autoUseTag order by template.id",
                EvalTemplate.class)
                .setParameter("autoUseTag", autoUseTag)
                .list();
    }

    public List<EvalTemplate> getTemplatesForUser(String userId, String[] sharingConstants, boolean includeEmpty) {
        StringBuilder hql = new StringBuilder("select template from EvalTemplate template where ");
        Map<String, Object> params = new HashMap<>();
        appendTemplatesForUserFilter(hql, userId, sharingConstants, includeEmpty, params);
        hql.append("order by template.sharing, template.title");

        Query<EvalTemplate> query = currentSession().createQuery(hql.toString(), EvalTemplate.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.list();
    }

    public int countTemplatesForUser(String userId, String[] sharingConstants, boolean includeEmpty) {
        StringBuilder hql = new StringBuilder("select count(template.id) from EvalTemplate template where ");
        Map<String, Object> params = new HashMap<>();
        appendTemplatesForUserFilter(hql, userId, sharingConstants, includeEmpty, params);

        Query<Long> query = currentSession().createQuery(hql.toString(), Long.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private void appendTemplatesForUserFilter(StringBuilder hql, String userId, String[] sharingConstants,
            boolean includeEmpty, Map<String, Object> params) {
        hql.append("template.type = :templateType and template.hidden = false ");
        params.put("templateType", EvalConstants.TEMPLATE_TYPE_STANDARD);
        appendSharingPredicate(hql, "template", userId, sharingConstants, params);
        if (!includeEmpty) {
            hql.append("and template.templateItems.size > 0 ");
        }
    }

    public EvalTemplateItem getTemplateItemByEid(String eid) {
        return findOneByEid(EvalTemplateItem.class, eid, "templateItem");
    }

    public List<EvalTemplateItem> getTemplateItemsByAutoUseTag(String autoUseTag) {
        if (autoUseTag == null) {
            throw new IllegalArgumentException("autoUseTag cannot be null");
        }
        return currentSession().createQuery(
                "select templateItem from EvalTemplateItem templateItem "
                + "where templateItem.autoUseTag = :autoUseTag order by templateItem.displayOrder, templateItem.id",
                EvalTemplateItem.class)
                .setParameter("autoUseTag", autoUseTag)
                .list();
    }

    public List<EvalTemplateItem> getTemplateItemsByIds(Long[] templateItemIds) {
        if (templateItemIds == null) {
            throw new IllegalArgumentException("templateItemIds cannot be null");
        }
        if (templateItemIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select templateItem from EvalTemplateItem templateItem where templateItem.id in (:templateItemIds)",
                EvalTemplateItem.class)
                .setParameterList("templateItemIds", templateItemIds)
                .list();
    }

    public List<EvalTemplate> getTemplatesUsingItem(Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId cannot be null");
        }
        return currentSession().createQuery(
                "select distinct templateItem.template from EvalTemplateItem templateItem "
                + "where templateItem.item.id = :itemId",
                EvalTemplate.class)
                .setParameter("itemId", itemId)
                .list();
    }

    public List<EvalTemplateItem> getOrphanedTemplateItems() {
        return currentSession().createQuery(
                "select templateItem from EvalTemplateItem templateItem "
                + "where templateItem.template is null and templateItem.item is null",
                EvalTemplateItem.class)
                .list();
    }

    public int countTopLevelTemplateItems(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(templateItem.id) from EvalTemplateItem templateItem "
                + "where templateItem.template.id = :templateId and templateItem.blockId is null",
                Long.class)
                .setParameter("templateId", templateId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public int countBlockChildTemplateItems(Long templateId, Long blockId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        if (blockId == null) {
            throw new IllegalArgumentException("blockId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(templateItem.id) from EvalTemplateItem templateItem "
                + "where templateItem.template.id = :templateId and templateItem.blockId = :blockId",
                Long.class)
                .setParameter("templateId", templateId)
                .setParameter("blockId", blockId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalTemplateItem> getBlockChildTemplateItems(Long blockParentId) {
        if (blockParentId == null) {
            throw new IllegalArgumentException("blockParentId cannot be null");
        }
        return currentSession().createQuery(
                "select templateItem from EvalTemplateItem templateItem "
                + "where templateItem.blockId = :blockParentId order by templateItem.displayOrder",
                EvalTemplateItem.class)
                .setParameter("blockParentId", blockParentId)
                .list();
    }

    public void saveTemplateItemWithLinks(EvalTemplateItem templateItem, EvalItem item, EvalTemplate template) {
        if (templateItem == null) {
            throw new IllegalArgumentException("templateItem cannot be null");
        }
        currentSession().saveOrUpdate(templateItem);
        if (item != null) {
            currentSession().saveOrUpdate(item);
        }
        if (template != null) {
            currentSession().saveOrUpdate(template);
        }
    }

    public int countEvaluationsByTitle(String titlePattern) {
        if (titlePattern == null) {
            throw new IllegalArgumentException("titlePattern cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation where evaluation.title like :titlePattern",
                Long.class)
                .setParameter("titlePattern", titlePattern)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalEvaluation> getEvaluationsByTitle(String titlePattern, String orderProperty, int startResult, int maxResults) {
        if (titlePattern == null) {
            throw new IllegalArgumentException("titlePattern cannot be null");
        }
        String orderBy = getEvaluationOrderProperty(orderProperty);
        Query<EvalEvaluation> query = currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.title like :titlePattern "
                + "order by evaluation." + orderBy,
                EvalEvaluation.class)
                .setParameter("titlePattern", titlePattern);
        if (startResult > 0) {
            query.setFirstResult(startResult);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
    }

    private String getEvaluationOrderProperty(String orderProperty) {
        if (orderProperty == null || orderProperty.isEmpty()) {
            return "title";
        }
        switch (orderProperty) {
            case "id":
            case "title":
            case "owner":
            case "state":
            case "startDate":
            case "dueDate":
            case "stopDate":
            case "viewDate":
            case "lastModified":
                return orderProperty;
            default:
                throw new IllegalArgumentException("Unsupported evaluation order property: " + orderProperty);
        }
    }

    public void saveTemplateItems(Set<EvalTemplateItem> templateItems) {
        saveOrUpdateAll(templateItems);
    }

    public void deleteTemplateItems(Set<EvalTemplateItem> templateItems) {
        deleteAll(templateItems);
    }

    public List<EvalGroupNodes> getEvalGroupNodesByNodeIds(String[] nodeIds) {
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot be null");
        }
        if (nodeIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select groupNode from EvalGroupNodes groupNode where groupNode.nodeId in (:nodeIds) order by groupNode.id",
                EvalGroupNodes.class)
                .setParameterList("nodeIds", nodeIds)
                .list();
    }


    public void removeTemplateItems(EvalTemplateItem[] templateItems) {
        log.debug("Removing " + templateItems.length + " template items");
        Set<EvalTemplateItem> deleteTemplateItems = new HashSet<>();

        for( EvalTemplateItem templateItem : templateItems )
        {
            EvalTemplateItem eti = (EvalTemplateItem) getHibernateTemplate().merge( templateItem );
            deleteTemplateItems.add(eti);
            eti.getItem().getTemplateItems().remove(eti);
            eti.getTemplate().getTemplateItems().remove(eti);
            getHibernateTemplate().update(eti);
        }

        // do the actual deletes
        getHibernateTemplate().deleteAll(deleteTemplateItems);
        log.info("Removed " + deleteTemplateItems.size() + " template items");
    }


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
    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty,
            boolean includeExpert) {

        StringBuilder hql = new StringBuilder("select eig from EvalItemGroup eig where eig.expert = :includeExpert");

        if (parentItemGroupId == null) {
            hql.append(" and eig.parent is null");
        } else {
            hql.append(" and eig.parent.id = :parentItemGroupId");
        }

        if (!includeEmpty) {
            // only include categories with items OR groups using them as a parent
            hql.append(" and (size(eig.groupItems) > 0");
            hql.append(" or exists (select child.id from EvalItemGroup child where child.parent = eig))");
        }

        hql.append(" order by eig.title asc");
        Query<EvalItemGroup> query = currentSession().createQuery(hql.toString(), EvalItemGroup.class);
        query.setParameter("includeExpert", includeExpert);
        if (parentItemGroupId != null) {
            query.setParameter("parentItemGroupId", parentItemGroupId);
        }
        return query.list();
    }

    /**
     * Get item groups contained within a specific group<br/>
     * <b>Note:</b> If parent is null then get all the highest level groups
     * 
     * @param itemId
     * @param userId the internal user id (not username)
     * @return a List of {@link EvalItemGroup} objects, ordered by title alphabetically
     */
    @SuppressWarnings("unchecked")
    public Long getItemGroupIdByItemId(Long itemId, String userId) {

        List<Long> results = getHibernateTemplate().execute(session -> session
                .createQuery("select eig.id from EvalItemGroup eig, EvalItem ei where eig.ig_item_id = ei.id and ei.id = :itemid")
                .setParameter("itemid", itemId)
                .list());

        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

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
            String[] instructorIds, String[] groupIds) {
        return getTemplateItemsByTemplates(new Long[] {templateId}, nodeIds, instructorIds, groupIds);
    }


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
    public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds) {
        Long templateId = getTemplateIdForEvaluation(evalId);
        if (templateId == null) {
            throw new IllegalArgumentException("Could not retrieve a template id for this evaluation");
        }
        return getTemplateItemsByTemplates(new Long[] {templateId}, nodeIds, instructorIds, groupIds);
    }

    /**
     * Fetch all the template items based on templates and various params
     * @param templateIds
     * @param nodeIds
     * @param instructorIds
     * @param groupIds
     * @return a list of template items ordered by display order and template
     */
    private List<EvalTemplateItem> getTemplateItemsByTemplates(Long[] templateIds, String[] nodeIds, String[] instructorIds, String[] groupIds) {
        List<EvalTemplateItem> results = new ArrayList<>();
        if (templateIds == null || templateIds.length == 0) {
            throw new IllegalArgumentException("Invalid templateIds, cannot be null or empty");
        } else {
            StringBuilder hql = new StringBuilder();
            hql.append("from EvalTemplateItem ti where ti.template.id in (:templateIds) and (ti.hierarchyLevel = :hierarchyLevel1 ");

            if (nodeIds != null) {
                if (nodeIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelNodes) ");
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelNodes and ti.hierarchyNodeId in (:nodeIds) ) ");
                }
            }

            if (instructorIds != null) {
                if (instructorIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelInst) ");
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelInst and ti.hierarchyNodeId in (:instructorIds) ) ");
                }
            }

            if (groupIds != null) {
                if (groupIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelGroup) ");
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelGroup and ti.hierarchyNodeId in (:groupIds) ) ");
                }
            }

            hql.append(") order by ti.displayOrder, ti.template.id");

            Query<EvalTemplateItem> query = currentSession().createQuery(hql.toString(), EvalTemplateItem.class);
            query.setParameterList("templateIds", templateIds);
            query.setParameter("hierarchyLevel1", EvalConstants.HIERARCHY_LEVEL_TOP);
            if (nodeIds != null) {
                query.setParameter("hierarchyLevelNodes", EvalConstants.HIERARCHY_LEVEL_NODE);
                if (nodeIds.length > 0) {
                    query.setParameterList("nodeIds", nodeIds);
                }
            }
            if (instructorIds != null) {
                query.setParameter("hierarchyLevelInst", EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR);
                if (instructorIds.length > 0) {
                    query.setParameterList("instructorIds", instructorIds);
                }
            }
            if (groupIds != null) {
                query.setParameter("hierarchyLevelGroup", EvalConstants.HIERARCHY_LEVEL_GROUP);
                if (groupIds.length > 0) {
                    query.setParameterList("groupIds", groupIds);
                }
            }
            results.addAll(query.list());
        }
        return results;
    }

    // public Integer getNextBlockId() {
    // String hqlQuery = "select max(item.blockId) from EvalItem item";
    // Integer max = (Integer) getHibernateTemplate().iterate(hqlQuery).next();
    // if (max == null) {
    // return new Integer(0);
    // }
    // return new Integer(max.intValue() + 1);
    // }


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

    protected Long getTemplateIdForEvaluation(Long evaluationId) {
        List<Long> results = (List<Long>) getHibernateTemplate().execute(session -> session
                .createQuery("select eval.template.id from EvalEvaluation eval where eval.id = :evalid")
                .setParameter("evalid", evaluationId)
                .list());

        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }


    /**
     * Get all the users who have responded to an evaluation (completely or partly)
     * and optionally within group(s) assigned to that evaluation
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param evalGroupIds [OPTIONAL] the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all responses for this evaluation
     * @param completed [OPTIONAL] if true then only completed (submitted) responses, 
     *      if false, then only incomplete (saved) responses,
     *      if null, then retrieve all responses (incomplete and complete)
     * @return a set of internal userIds
     */
}
