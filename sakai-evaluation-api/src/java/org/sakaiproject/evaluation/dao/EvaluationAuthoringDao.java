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

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

public interface EvaluationAuthoringDao {

    public int countEvalScales();

    public EvalScale getScaleByEid(String eid);

    public List<EvalScale> getScalesByIds(Long[] scaleIds);

    public List<EvalScale> getScalesWithNullMode();

    public List<EvalScale> getScalesForUser(String userId, String[] sharingConstants);

    public void saveScales(Set<EvalScale> scales);

    public void deleteScales(Set<EvalScale> scales);

    public int countEvalItems();

    public EvalItem getItemByEid(String eid);

    public List<EvalItem> getItemsByAutoUseTag(String autoUseTag);

    public List<EvalItem> getItemsForUser(String userId, String[] sharingConstants, String filter, boolean includeExpert);

    public List<EvalItem> getItemsByIds(Long[] itemIds);

    public List<EvalItem> getItemsUsingScale(Long scaleId);

    public void saveItems(Set<EvalItem> items);

    public void deleteItems(Set<EvalItem> items);

    public int countEvalItemGroups();

    public EvalItemGroup getItemGroupByTitle(String title);

    public int countTemplateById(Long templateId);

    public EvalTemplate getTemplateByEid(String eid);

    public List<EvalTemplate> getTemplatesByAutoUseTag(String autoUseTag);

    public List<EvalTemplate> getTemplatesForUser(String userId, String[] sharingConstants, boolean includeEmpty);

    public int countTemplatesForUser(String userId, String[] sharingConstants, boolean includeEmpty);

    public List<EvalTemplate> getTemplatesUsingItem(Long itemId);

    public EvalTemplateItem getTemplateItemByEid(String eid);

    public List<EvalTemplateItem> getTemplateItemsByAutoUseTag(String autoUseTag);

    public List<EvalTemplateItem> getTemplateItemsByIds(Long[] templateItemIds);

    public List<EvalTemplateItem> getTemplateItemsByHierarchyNodeId(String nodeId);

    public List<EvalTemplateItem> getOrphanedTemplateItems();

    public int countTopLevelTemplateItems(Long templateId);

    public int countBlockChildTemplateItems(Long templateId, Long blockId);

    public List<EvalTemplateItem> getBlockChildTemplateItems(Long blockParentId);

    public void saveTemplateItemWithLinks(EvalTemplateItem templateItem, EvalItem item, EvalTemplate template);

    public void saveTemplateItems(Set<EvalTemplateItem> templateItems);

    public void deleteTemplateItems(Set<EvalTemplateItem> templateItems);

    public int countEvaluationsByTitle(String titlePattern);

    public List<EvalEvaluation> getEvaluationsByTitle(String titlePattern, String orderProperty, int startResult, int maxResults);

    public void removeTemplateItems(EvalTemplateItem[] templateItems);

    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert);

    public Long getItemGroupIdByItemId(Long itemId, String userId);

    public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds, String[] instructorIds, String[] groupIds);

    public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds);

    public EvalScale getScaleById(Long scaleId);

    public void saveScale(EvalScale scale);

    public void deleteScale(EvalScale scale);

    public EvalItem getItemById(Long itemId);

    public void saveItem(EvalItem item);

    public void deleteItem(EvalItem item);

    public EvalItemGroup getItemGroupById(Long itemGroupId);

    public void saveItemGroup(EvalItemGroup itemGroup);

    public void deleteItemGroup(EvalItemGroup itemGroup);

    public EvalTemplate getTemplateById(Long templateId);

    public void saveTemplate(EvalTemplate template);

    public void deleteTemplate(EvalTemplate template);

    public EvalTemplateItem getTemplateItemById(Long templateItemId);

    public void saveTemplateItem(EvalTemplateItem templateItem);

    public int countTemplates();
}
