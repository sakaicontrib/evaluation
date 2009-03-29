/**
 * $Id$
 * $URL$
 * TemplateItemDataList.java - evaluation - Mar 28, 2008 8:58:12 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * A special data structure for wrapping template items that allows us to easily get things 
 * like the total number of template items contained in this structure and meta data about them<br/>
 * We can also store extra data in the list (like the total list of template items)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemDataList {

    /**
     * This will create a flat list of all wrapped template items in the proper order,
     * these indicate the total number of rendered items and not the total count of TemplateItems<br/>
     * 
     * @param includeBlockChildren if true then block children will be included in the flat list,
     * otherwise they are only included in the {@link DataTemplateItem#blockChildItems} list
     * @return the complete DISPLAYORDERED list of DataTemplateItems in this data structure
     */
    public List<DataTemplateItem> getFlatListOfDataTemplateItems(boolean includeBlockChildren) {
        return buildFlatDataList(null, null, includeBlockChildren);
    }

    /**
     * Creates a DataTemplateItem from a given template item id (must be a template item in the TIDL) <br/>
     * WARNING: this DTI will be out of context because it is being fetched without being in an item group
     * 
     * @param templateItemId the unique id of a template item in the TIDL
     * @return the DataTemplateItem OR null if the template item id cannot be found
     */
    public DataTemplateItem getDataTemplateItem(Long templateItemId) {
        DataTemplateItem dti = null;
        EvalTemplateItem eti = getTemplateItem(templateItemId);
        if (eti != null) {
            dti = new DataTemplateItem(eti, EvalConstants.ITEM_CATEGORY_COURSE, null, null);
        }
        return dti;
    }

    private List<TemplateItemGroup> templateItemGroups = null;
    /**
     * @return the list of all {@link TemplateItemGroup}s in this structure
     */
    public List<TemplateItemGroup> getTemplateItemGroups() {
        if (templateItemGroups == null) {
            buildDataStructure();
        }
        return templateItemGroups;
    }

    private List<EvalTemplateItem> allTemplateItems = null;
    /**
     * @return the complete DISPLAYORDERED list of all templateItems in this data structure
     */
    public List<EvalTemplateItem> getAllTemplateItems() {
        return allTemplateItems;
    }

    /**
     * Get a template item from the TIDL by its unique id
     * @param templateItemId the unique id of a template item
     * @return the template item OR null if none exists in the TIDL with that id
     */
    public EvalTemplateItem getTemplateItem(Long templateItemId) {
        EvalTemplateItem eti = null;
        for (EvalTemplateItem templateItem : allTemplateItems) {
            if (templateItem.getId().equals(templateItemId)) {
                eti = templateItem;
                break;
            }
        }
        return eti;
    }

    private List<EvalHierarchyNode> hierarchyNodes = null;
    /**
     * @return the list of hierarchy nodes we are working with, empty if there are none
     */
    public List<EvalHierarchyNode> getHierarchyNodes() {
        return hierarchyNodes;
    }

    private Map<String, List<String>> associates = null;
    /**
     * @return the map of associate types -> lists of ids for that type,
     * will always include at least one entry: EvalConstants.ITEM_CATEGORY_COURSE -> List[null]
     */
    public Map<String, List<String>> getAssociates() {
        return associates;
    }

    private List<EvalAnswer> answers = null;
    /**
     * @return the answers stored with and related to these templateItems,
     * will be empty list if there are no answers stored
     */
    public List<EvalAnswer> getAnswers() {
        return answers;
    }

    /**
     * the internal mapping of answers to template items by associated key,
     * key will be the one generated by {@link TemplateItemUtils#makeTemplateItemAnswerKey(Long, String, String)},
     * the list of answers will be all answers for the related key
     */
    private Map<String, List<EvalAnswer>> answersMap = null;
    /**
     * the internal mapping of response ids -> (itemAnswerKey -> answer),
     * key will be the one generated by {@link TemplateItemUtils#makeTemplateItemAnswerKey(Long, String, String)}
     */
    private Map<Long, Map<String, EvalAnswer>> responseAnswersMap = null;

    /**
     * Generate the rendering data structure for working with template items,
     * this is primarily used for sorting and grouping the template items properly<br/>
     * <b>NOTE:</b> There will always be a course category {@link TemplateItemGroup} as long
     * as there were some TemplateItems supplied but it may have nothing in it so you should
     * check to see if the internal lists are empty
     * 
     * @param allTemplateItems the list of all template items to be placed into the structure,
     * this must not be null and must include at least one {@link EvalTemplateItem}
     * @param hierarchyNodes (OPTIONAL) the list of all hierarchy nodes for grouping (list order will be used),
     * this can be null if we are not using hierarchy or there are no hierarchy nodes in use
     * @param associates (OPTIONAL) a map of associate type -> list of ids of that type,
     * this defines the categories of items we are working with and segments them,
     * e.g. {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR} => [aaronz,sgtithens],
     * normally only the instructors,
     * can be null if there are no categories in use (course category is always used)
     * @param answers (OPTIONAL) a set of answers for the set of templateItems, this should be the complete
     * set of answers or it is likely that the data that comes out of this will be confused,
     * can be left null if not needed, if this is null then the all answers data will be blank
     */
    public TemplateItemDataList(List<EvalTemplateItem> allTemplateItems,
            List<EvalHierarchyNode> hierarchyNodes, Map<String, List<String>> associates, List<EvalAnswer> answers) {
        construct(allTemplateItems, hierarchyNodes, associates, answers);
    }

    /**
     * For building a structure which is used for looking at the items in an evaluation <br/>
     * Generate the rendering data structure for working with template items,
     * this is primarily used for sorting and grouping the template items properly<br/>
     * <b>NOTE:</b> There will always be a course category {@link TemplateItemGroup} as long
     * as there were some TemplateItems supplied but it may have nothing in it so you should
     * check to see if the internal lists are empty
     * 
     * @param evaluationId the unique id of the evaluation
     * @param evalGroupId the unique id of an eval group
     * @param evaluationService the evaluation service (used for lookup of participants)
     * @param authoringService the authoring service (used to lookup template items)
     * @param hierarchyLogic (OPTIONAL) the hierarchy service (used to lookup hierarchy levels for this group),
     * if null then no hierarchy levels are resolved and thus some items may be missing from the structure
     * @param deliveryService (OPTIONAL) the delivery service (used to lookup evaluation answers),
     * if null then no answers will be looked up and included in the structure
     */
    public TemplateItemDataList(Long evaluationId, String evalGroupId,
            EvalEvaluationService evaluationService, EvalAuthoringService authoringService, 
            ExternalHierarchyLogic hierarchyLogic, EvalDeliveryService deliveryService) {
        if (evaluationId == null 
                || evalGroupId == null || "".equals(evalGroupId)
                || evaluationService == null
                || authoringService == null) {
            throw new IllegalArgumentException("evaluationId, evalGroupId, evaluationService, and authoringService must be set");
        }

        String[] hierarchyNodeIDs = {};
        List<EvalHierarchyNode> evalHierarchyNodes = null;
        if (hierarchyLogic != null) {
            // Get the Hierarchy Nodes for the current Group and turn it into an array of node ids
            evalHierarchyNodes = hierarchyLogic.getNodesAboveEvalGroup(evalGroupId);
            hierarchyNodeIDs = new String[evalHierarchyNodes.size()];
            for (int i = 0; i < evalHierarchyNodes.size(); i++) {
                hierarchyNodeIDs[i] = evalHierarchyNodes.get(i).id;
            }
        }

        // get the instructors for this evaluation
        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
        Set<String> instructorIds = EvalUtils.getUserIdsFromUserAssignments(userAssignments);
        List<EvalAssignUser> assistantAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {evalGroupId}, EvalAssignUser.TYPE_ASSISTANT, null, null, null);
        Set<String> assistantIds = EvalUtils.getUserIdsFromUserAssignments(assistantAssignments);

        // get all items for this evaluation
        String[] instructorIdsArray = instructorIds.toArray(new String[instructorIds.size()]);
        List<EvalTemplateItem> evalTemplateItems = authoringService.getTemplateItemsForEvaluation(evaluationId, hierarchyNodeIDs, 
                instructorIdsArray, new String[] {evalGroupId});

        Map<String, List<String>> evalAssociates = new HashMap<String, List<String>>();
        evalAssociates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, new ArrayList<String>(instructorIds));
        // add in the assistants list if there are any
        if (assistantIds.size() > 0) {
            evalAssociates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, new ArrayList<String>(assistantIds));
        }

        List<EvalAnswer> evalAnswers = null;
        if (deliveryService != null) {
            evalAnswers = deliveryService.getAnswersForEval(evaluationId, new String[] {evalGroupId}, null);
        }

        construct(evalTemplateItems, evalHierarchyNodes, evalAssociates, evalAnswers);
    }

    /**
     * For building a structure which is used for looking at the results of an evaluation 
     * (report generation and response viewing)<br/>
     * Generate the rendering data structure for working with template items,
     * this is primarily used for sorting and grouping the template items properly<br/>
     * <b>NOTE:</b> There will always be a course category {@link TemplateItemGroup} as long
     * as there were some TemplateItems supplied but it may have nothing in it so you should
     * check to see if the internal lists are empty
     * 
     * @param evaluationId the unique id of the evaluation
     * @param evalGroupIds an array of all eval group ids to get items/answers for
     * @param authoringService the authoring service (used to lookup template items)
     * @param deliveryService the delivery service (used to lookup evaluation answers)
     * @param hierarchyLogic (OPTIONAL) the hierarchy service (used to lookup hierarchy levels for this group),
     * if null then no hierarchy levels are resolved and thus some items may be missing from the structure
     */
    public TemplateItemDataList(Long evaluationId, String[] evalGroupIds, 
            EvalAuthoringService authoringService, EvalDeliveryService deliveryService,
            ExternalHierarchyLogic hierarchyLogic) {
        if (evaluationId == null 
                || evalGroupIds == null || evalGroupIds.length == 0
                || deliveryService == null
                || authoringService == null) {
            throw new IllegalArgumentException("evaluationId, evalGroupId, deliveryService, and authoringService must be set");
        }

        // get all template items for all nodes/instructors, limit by eval and groups only
        List<EvalTemplateItem> evalTemplateItems = authoringService.getTemplateItemsForEvaluation(evaluationId, new String[0], 
                new String[0], evalGroupIds);

        // get all the answers
        List<EvalAnswer> evalAnswers = deliveryService.getAnswersForEval(evaluationId, evalGroupIds, null);

        // get the list of all instructors/assistants (getting it from the answers though so it may not be comprehensive)
        Set<String> instructorIds = TemplateItemDataList.getInstructorsForAnswers(evalAnswers);
        Set<String> assistantIds = TemplateItemDataList.getAssistantsForAnswers(evalAnswers);

        // Get the sorted list of all nodes for this set of template items
        List<EvalHierarchyNode> evalHierarchyNodes = null;
        if (hierarchyLogic != null) {
            evalHierarchyNodes = makeEvalNodesList(evalTemplateItems, hierarchyLogic);
        }

        Map<String, List<String>> evalAssociates = new HashMap<String, List<String>>();
        evalAssociates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, new ArrayList<String>(instructorIds));
        // add in the assistants list if there are any
        if (assistantIds.size() > 0) {
            evalAssociates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, new ArrayList<String>(assistantIds));
        }

        construct(evalTemplateItems, evalHierarchyNodes, evalAssociates, evalAnswers);
    }

    /**
     * Constructs the TIDL
     * @param allTemplateItems (REQUIRED)
     * @param hierarchyNodes (OPTIONAL)
     * @param associates (OPTIONAL)
     * @param answers (OPTIONAL)
     */
    private void construct(List<EvalTemplateItem> allTemplateItems,
            List<EvalHierarchyNode> hierarchyNodes, Map<String, List<String>> associates,
            List<EvalAnswer> answers) {
        if (allTemplateItems == null || allTemplateItems.size() == 0) {
            throw new IllegalArgumentException("You must supply a non-null list of at least one template item to use this structure");
        }

        this.allTemplateItems = TemplateItemUtils.orderTemplateItems(allTemplateItems, false);

        if (hierarchyNodes != null) {
            this.hierarchyNodes = hierarchyNodes;
        } else {
            this.hierarchyNodes = new ArrayList<EvalHierarchyNode>();
        }

        if (associates != null) {
            this.associates = associates;
        } else {
            this.associates = new HashMap<String, List<String>>();
        }

        // ensure there is at least the default course category
        if (! this.associates.containsKey(EvalConstants.ITEM_CATEGORY_COURSE)) {
            List<String> courses = new ArrayList<String>();
            courses.add(null);
            this.associates.put(EvalConstants.ITEM_CATEGORY_COURSE, courses);
        }

        if (answers != null) {
            this.answers = answers;
        } else {
            this.answers = new ArrayList<EvalAnswer>();
        }

        buildDataStructure();
    }

    // PUBLIC data methods

    public int getTemplateItemsCount() {
        return allTemplateItems.size();
    }

    private int nonChildItemsCount = 0;
    /**
     * @return the count of all non-block-child template items
     */
    public int getNonChildItemsCount() {
        return nonChildItemsCount;
    }

    private List<String> associateTypes = new ArrayList<String>();
    /**
     * @return the correctly ordered list of all associate types for these template items
     */
    public List<String> getAssociateTypes() {
        return associateTypes;
    }

    /**
     * @return the count of the number of associate TI groupings,
     * should always be 1 or greater
     */
    public int getTemplateItemGroupsCount() {
        return templateItemGroups.size();
    }

    /**
     * Convenience method to get all the associateIds of a certain type,
     * will return empty list if this type is unused or there are no associate ids
     * 
     * @param associateType the constant associate type string (e.g. {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR})
     * @return a set (empty if no ids for the given type)
     * @see #associates map for more information about the possible returns
     */
    public Set<String> getAssociateIds(String associateType) {
        Set<String> ids = new HashSet<String>();
        if (this.associates.containsKey(associateType)) {
            ids.addAll( this.associates.get(associateType) );
        }
        return ids;
    }

    private Map<String, List<EvalTemplateItem>> autoInsertMap = null;
    /**
     * @return a map of all auto inserted items (autoUseTag -> List[templateItems]),
     * this will be empty if there are none, the size of the map is the number
     * of auto inserted tags
     */
    public Map<String, List<EvalTemplateItem>> getAutoInsertedItems() {
        if (autoInsertMap == null) {
            autoInsertMap = new HashMap<String, List<EvalTemplateItem>>();
            for (EvalTemplateItem templateItem : this.allTemplateItems) {
                String autoUseTag = templateItem.getAutoUseInsertionTag();
                if (autoUseTag != null) {
                    if (autoInsertMap.containsKey(autoUseTag)) {
                        autoInsertMap.get(autoUseTag).add(templateItem);
                    } else {
                        List<EvalTemplateItem> autoUseItems = new ArrayList<EvalTemplateItem>();
                        autoUseItems.add(templateItem);
                        autoInsertMap.put(autoUseTag, autoUseItems);
                    }
                }
            }
        }
        return autoInsertMap;
    }

    /**
     * @return the count of all auto inserted items
     */
    public int countAutoInsertedItems() {
        int count = 0;
        Map<String, List<EvalTemplateItem>> aiis = getAutoInsertedItems();
        for (String key : aiis.keySet()) {
            count += aiis.get(key).size();
        }
        return count;
    }


    /**
     * This turns the data structure into a flattened list of {@link DataTemplateItem}s
     * 
     * @param includeTIG if not null, then only return wrapped items from this {@link TemplateItemGroup}
     * @param includeHNG if not null, then only return wrapped items from this {@link HierarchyNodeGroup}
     * @param includeBlockChildren if true then block children will be included in the flat list,
     * otherwise they are only included in the {@link DataTemplateItem#blockChildItems} list
     */
    protected List<DataTemplateItem> buildFlatDataList(TemplateItemGroup includeTIG, HierarchyNodeGroup includeHNG, boolean includeBlockChildren) {
        if (templateItemGroups == null) {
            buildDataStructure();
        }

        List<DataTemplateItem> dataTemplateItems = new ArrayList<DataTemplateItem>();
        // loop through and build the flattened list
        for (int i = 0; i < templateItemGroups.size(); i++) {
            TemplateItemGroup tig = templateItemGroups.get(i);
            if (includeTIG != null
                    && ! includeTIG.equals(tig)) {
                // skip data from all groups except the matching one
                continue;
            }
            for (int j = 0; j < tig.hierarchyNodeGroups.size(); j++) {
                HierarchyNodeGroup hng = tig.hierarchyNodeGroups.get(j);
                if (includeHNG != null
                        && ! includeHNG.equals(hng)) {
                    // skip data from all groups except the matching one
                    continue;
                }
                for (int k = 0; k < hng.templateItems.size(); k++) {
                    EvalTemplateItem templateItem = hng.templateItems.get(k);
                    DataTemplateItem dti = new DataTemplateItem(templateItem, tig.associateType, tig.associateId, hng.node);
                    if (k == 0) {
                        dti.isFirstInNode = true;
                        if (j == 0) {
                            dti.isFirstInAssociated = true;
                        }
                    }
                    dataTemplateItems.add(dti);
                    // add in the block children to the flat list
                    if (includeBlockChildren 
                            && dti.blockChildItems != null) {
                        for (EvalTemplateItem childItem : dti.blockChildItems) {
                            DataTemplateItem child = new DataTemplateItem(childItem, tig.associateType, tig.associateId, hng.node);
                            child.blockParentId = dti.templateItem.getId();
                            dataTemplateItems.add(child);
                        }
                    }
                }
            }
        }
        return dataTemplateItems;
    }

    // INTERNAL processing methods

    /**
     * This processes the data in the structure and builds up a nested list structure so the TIs are properly grouped
     */
    protected void buildDataStructure() {
        if (allTemplateItems == null || hierarchyNodes == null || associates == null) {
            throw new IllegalArgumentException("null inputs are not allowed, empty lists are ok though");
        }

        buildAnswerMaps();

        templateItemGroups = new ArrayList<TemplateItemGroup>();
        if (allTemplateItems.size() > 0) {
            // filter out the block child items, to get a list of non-child items
            List<EvalTemplateItem> nonChildItemsList = TemplateItemUtils.getNonChildItems(this.allTemplateItems);
            nonChildItemsCount = nonChildItemsList.size();

            // turn the map keys into a properly sorted list of types
            this.associateTypes = new ArrayList<String>();
            for (int i = 0; i < EvalConstants.ITEM_CATEGORY_ORDER.length; i++) {
                if (associates.containsKey(EvalConstants.ITEM_CATEGORY_ORDER[i])) {
                    associateTypes.add(EvalConstants.ITEM_CATEGORY_ORDER[i]);
                }
            }

            // loop through the associates
            for (String associateType : associateTypes) {
                List<String> associateIds = associates.get(associateType);
                // get all the template items for this category
                List<EvalTemplateItem> categoryNonChildItemsList = TemplateItemUtils.getCategoryTemplateItems(associateType, nonChildItemsList);
                if (categoryNonChildItemsList.size() > 0) {
                    // assume the associateIds are in the correct render order
                    for (String associateId : associateIds) {
                        // handle the data creation for this associateId
                        TemplateItemGroup tig = new TemplateItemGroup(associateType, associateId);
                        tig.hierarchyNodeGroups = new ArrayList<HierarchyNodeGroup>();
                        templateItemGroups.add(tig);

                        // now handle the hierarchy levels
                        // top level first
                        List<EvalTemplateItem> templateItems = TemplateItemUtils.getNodeItems(categoryNonChildItemsList, null);
                        if (templateItems.size() > 0) {
                            addNodeTemplateItems(tig, null, templateItems);
                        }

                        // then do the remaining nodes in the order supplied
                        for (EvalHierarchyNode evalNode: hierarchyNodes) {
                            templateItems = TemplateItemUtils.getNodeItems(categoryNonChildItemsList, evalNode.id);
                            if (templateItems.size() > 0) {
                                addNodeTemplateItems(tig, evalNode, templateItems);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add the template items for this node to a newly create HNG and attach it to the TIG
     * 
     * @param tig
     * @param evalNode can be null to indicate the top level
     * @param templateItems
     */
    private void addNodeTemplateItems(TemplateItemGroup tig, EvalHierarchyNode evalNode,
            List<EvalTemplateItem> templateItems) {
        HierarchyNodeGroup hng = new HierarchyNodeGroup(evalNode);
        hng.templateItems = templateItems;
        tig.hierarchyNodeGroups.add(hng);
    }

    /**
     * Builds both answer maps using the answers data (if there is any),
     * the order of the answers inside the lists is effectively random
     */
    protected void buildAnswerMaps() {
        answersMap = new HashMap<String, List<EvalAnswer>>();
        responseAnswersMap = new HashMap<Long, Map<String,EvalAnswer>>();
        for (EvalAnswer answer : answers) {
            // decode the stored answers into the int array
            answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
            // decode the NA value
            EvalUtils.decodeAnswerNA(answer);
            // place the answers into a map which uses the TI, assocType, and assocId as a key
            String key = TemplateItemUtils.makeTemplateItemAnswerKey(answer.getTemplateItem().getId(), 
                    answer.getAssociatedType(), answer.getAssociatedId());
            // place answer in answersMap
            if (answersMap.containsKey(key)) {
                answersMap.get(key).add(answer);
            } else {
                List<EvalAnswer> keyAnswers = new ArrayList<EvalAnswer>();
                keyAnswers.add(answer);
                answersMap.put(key, keyAnswers);
            }
            // place answer in responseAnswersMap
            Long responseId = answer.getResponse().getId();
            if (responseAnswersMap.containsKey(responseId)) {
                responseAnswersMap.get(responseId).put(key, answer);
            } else {
                Map<String,EvalAnswer> keyAnswerMap = new HashMap<String, EvalAnswer>();
                keyAnswerMap.put(key, answer);
                responseAnswersMap.put(responseId, keyAnswerMap);
            }
        }
    }

    /**
     * Gets a list of all the answers for a specified response id
     * 
     * @param responseId a unique id for an {@link EvalResponse}
     * @return the list of answers for this response id or empty list if none found
     */
    public List<EvalAnswer> getAnswersByResponseId(Long responseId) {
        List<EvalAnswer> answersList = new ArrayList<EvalAnswer>();
        if (responseAnswersMap.containsKey(responseId)) {
            answersList.addAll(responseAnswersMap.get(responseId).values());
        }
        return answersList;
    }

    /**
     * @return the list of all responseIds for the set of answers in this data structure
     */
    public List<Long> getResponseIdsForAnswers() {
        List<Long> responseIdsList = new ArrayList<Long>();
        responseIdsList.addAll(responseAnswersMap.keySet());
        Collections.sort(responseIdsList);
        return responseIdsList;      
    }


    // INNER classes

    /**
     * This is a template item with lots of extra meta data so that it can be easily determined where it
     * goes in the processing order and whether it is in the hierarchy or associated with nodes<br/>
     * <b>NOTE:</b> The same template item can belong to many DataTemplateItems<br/>
     * <b>WARNING:</b> You will get a new one of these objects each time you request 
     * one so any changes you make to it will appear to be lost when another one is retrieved from the TIDL
     * that has the same set of attributes
     * 
     * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
     */
    public class DataTemplateItem {

        private String key;
        /**
         * The unique key (templateItemAnswerKey) for this DTI,
         * created using {@link TemplateItemUtils#makeTemplateItemAnswerKey(Long, String, String)}
         */
        public String getKey() {
            if (key == null) {
                key = TemplateItemUtils.makeTemplateItemAnswerKey(templateItem.getId(), associateType, associateId);
            }
            return key;
        }
        /**
         * The template item for this data object
         */
        public EvalTemplateItem templateItem;
        /**
         * The type (category) of this template item,
         * this will usually be like {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR},
         * required and cannot be null
         */
        public String associateType;
        /**
         * The optional ID of the thing that is associated with this type for this template item,
         * e.g. if this is an instructor it would be their internal userId<br/>
         * can be null
         */
        public String associateId;
        /**
         * The hierarchy node associated with this template items,
         * null indicates this is the top level and not a node at all
         */
        public EvalHierarchyNode node;
        /**
         * true if this is the first item for its associated type and id
         */
        public boolean isFirstInAssociated = false;
        /**
         * true if this is the first item for the hierarchy node (or for the top level)
         */
        public boolean isFirstInNode = false;
        /**
         * this will be null if this is not a block parent,
         * if this is a block parent then this will be a list of all block children in displayOrder 
         */
        public List<EvalTemplateItem> blockChildItems;
        /**
         * set to the templateItem id of the blockParent if this is a block child,
         * blockParentId will be null if this is not a block child
         */
        public Long blockParentId = null;

        public DataTemplateItem(EvalTemplateItem templateItem, String associateType,
                String associateId, EvalHierarchyNode node) {
            if (templateItem == null) {
                throw new IllegalArgumentException("templateItem cannot be null");
            }
            this.templateItem = templateItem;
            if (associateType == null) {
                throw new IllegalArgumentException("associateType cannot be null");
            }
            this.associateType = associateType;
            this.associateId = associateId;
            this.getKey(); // initialize the key
            this.node = node;
            if (TemplateItemUtils.isBlockParent(templateItem)) {
                this.blockChildItems = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
                this.templateItem.childTemplateItems = new ArrayList<EvalTemplateItem>(blockChildItems); // for rendering
            } else if (TemplateItemUtils.isBlockChild(templateItem)) {
                this.blockParentId = templateItem.getBlockId();
            }
            // minor fixups to ensure nulls are filled in
            if (templateItem.getUsesComment() == null) {
                templateItem.setUsesComment(false);
            }
            if (templateItem.getUsesNA() == null) {
                templateItem.setUsesNA(false);
            }
        }

        /**
         * @return the type of this template item,
         * @see TemplateItemUtils#getTemplateItemType(EvalTemplateItem)
         */
        public String getTemplateItemType() {
            return TemplateItemUtils.getTemplateItemType(this.templateItem);
        }

        /**
         * @return true if this item is answerable (i.e. not just a text header/block parent/etc.),
         * false otherwise
         * @see TemplateItemUtils#isAnswerable(EvalTemplateItem)
         */
        public boolean isAnswerable() {
            return TemplateItemUtils.isAnswerable(this.templateItem);
        }

        /**
         * @return true if this item is requireable (i.e. must be answered if eval settings stipulate),
         * false otherwise
         * @see TemplateItemUtils#isRequireable(EvalTemplateItem)
         */
        public boolean isRequireable() {
            return TemplateItemUtils.isRequireable(this.templateItem);
        }

        /**
         * @return true if this item is compulsory (i.e. must be answered),
         * false otherwise
         * @see TemplateItemUtils#isCompulsory(EvalTemplateItem)
         */
        public boolean isCompulsory() {
            return TemplateItemUtils.isCompulsory(this.templateItem);
        }

        /**
         * @return the auto insertion tag for this item or null if none found
         */
        public String getAutoInsertionTag() {
            return this.templateItem.getAutoUseInsertionTag();
        }

        /**
         * @return true if the contained item is a block parent
         */
        public boolean isBlockParent() {
            return this.blockChildItems != null;
        }

        /**
         * @return true if the contained item is a block child
         */
        public boolean isBlockChild() {
            return this.blockParentId != null;
        }

        /**
         * Method to generate a list of block children,
         * this is done on demand though so keep that in mind
         * 
         * @return the list of all block children in this DTI as DTIs
         */
        public List<DataTemplateItem> getBlockChildren() {
            ArrayList<DataTemplateItem> children = new ArrayList<DataTemplateItem>();
            if (this.blockChildItems != null) {
                for (EvalTemplateItem eti : this.blockChildItems) {
                    children.add( new DataTemplateItem(eti, this.associateType, this.associateId, this.node) );
                }
            }
            return children;
        }

        /**
         * @return true if this item is set to use comments
         */
        public boolean usesComments() {
            boolean uses = false;
            if (templateItem.getUsesComment() != null && templateItem.getUsesComment()) {
                uses = true;
            }
            return uses;
        }

        /**
         * @return the list of all comments for this item
         */
        public List<String> getComments() {
            List<String> comments = new ArrayList<String>();
            List<EvalAnswer> answers = getAnswers();
            if (answers != null) {
                for (EvalAnswer answer : answers) {
                    if (! EvalUtils.isBlank(answer.getComment())) {
                        comments.add( answer.getComment() );
                    }
                }
            }
            return comments;
        }

        /**
         * @return the list of all answers related to this data template item,
         * this will be null if the item is not answerable
         */
        public List<EvalAnswer> getAnswers() {
            List<EvalAnswer> answers = null;
            if (TemplateItemUtils.isAnswerable(this.templateItem)) {
                String key = getKey();
                if (answersMap.containsKey(key)) {
                    answers = answersMap.get(key);
                } else {
                    answers = new ArrayList<EvalAnswer>();
                }
            }
            return answers;
        }

        /**
         * @param responseId a unique id for an {@link EvalResponse}
         * @return the answer related to this template item and the response or null if not found
         */
        public EvalAnswer getAnswer(Long responseId) {
            EvalAnswer answer = null;
            if (responseAnswersMap.containsKey(responseId)) {
                answer = responseAnswersMap.get(responseId).get(this.key);
            }
            return answer;
        }
    }

    /**
     * This is a high level group of template items related to categories (e.g. Course, Instructor, ...),
     * these categories should receive special treatment in most cases<br/>
     * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}
     * <b>NOTE:</b> There will always be a top level {@link HierarchyNodeGroup} but it may have an empty list of
     * templateItems inside it<br/>
     * Normally you would want to iterate through the {@link #hierarchyNodeGroups} and 
     * 
     * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
     */
    public class TemplateItemGroup {

        /**
         * The type (category) of this TIG,
         * this will usually be like {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR},
         * required and cannot be null
         */
        public String associateType;
        /**
         * The optional ID of the thing that is associated with this type,
         * e.g. if this is an instructor it would be their internal userId<br/>
         * can be null
         */
        public String associateId;
        /**
         * The list of hierarchical node groups within this group,
         * ordered correctly for display and reporting
         */
        public List<HierarchyNodeGroup> hierarchyNodeGroups;

        /**
         * @return the count of all template items contained in this group,
         * does not include block children
         */
        public int getTemplateItemsCount() {
            int total = 0;
            for (HierarchyNodeGroup hng : hierarchyNodeGroups) {
                total += hng.templateItems.size();
            }
            return total;
        }

        /**
         * @return the list of all template items in this group in displayOrder,
         * does not include block children
         */
        public List<EvalTemplateItem> getTemplateItems() {
            List<EvalTemplateItem> tis = new ArrayList<EvalTemplateItem>();
            for (HierarchyNodeGroup hng : hierarchyNodeGroups) {
                tis.addAll(hng.templateItems);
            }
            return tis;
        }

        /**
         * @param includeBlockChildren if true then block children will be included in the flat list,
         * otherwise they are only included in the {@link DataTemplateItem#blockChildItems} list
         * @return the list of all wrapped template items in this group in displayOrder
         */
        public List<DataTemplateItem> getDataTemplateItems(boolean includeBlockChildren) {
            return buildFlatDataList(this, null, includeBlockChildren);
        }

        public TemplateItemGroup(String associateType, String associateId) {
            this.associateType = associateType;
            this.associateId = associateId;
            hierarchyNodeGroups = new ArrayList<HierarchyNodeGroup>();
        }

        public TemplateItemGroup(String associateType, String associateId, List<HierarchyNodeGroup> hierarchyNodeGroups) {
            this.associateType = associateType;
            this.associateId = associateId;
            this.hierarchyNodeGroups = hierarchyNodeGroups;
        }
    }

    /**
     * This is a high level group of hierarchy nodes which contain template items,
     * these nodes should receive special treatment in most cases<br/>
     * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}<br/>
     * Normally you would want to iterate over the {@link #templateItems} and process each one in order,
     * don't forget to handle the special case of the block parents
     * 
     * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
     */
    public class HierarchyNodeGroup {

        /**
         * The hierarchy node associated with this group of template items,
         * null indicates this is the top level and all TIs here are associated with the top level and not a node at all
         */
        public EvalHierarchyNode node;
        /**
         * The list of template items within this group (will not include block-child items),
         * ordered correctly for display and reporting
         */
        public List<EvalTemplateItem> templateItems;
        public List<EvalTemplateItem> getTemplateItems() {
            return templateItems;
        }

        /**
         * @param includeBlockChildren if true then block children will be included in the flat list,
         * otherwise they are only included in the {@link DataTemplateItem#blockChildItems} list
         * @return the list of all wrapped template items in this group in displayOrder
         */
        public List<DataTemplateItem> getDataTemplateItems(boolean includeBlockChildren) {
            return buildFlatDataList(null, this, includeBlockChildren);
        }

        public HierarchyNodeGroup(EvalHierarchyNode node) {
            this.node = node;
            templateItems = new ArrayList<EvalTemplateItem>();
        }

        public HierarchyNodeGroup(EvalHierarchyNode node, List<EvalTemplateItem> templateItems) {
            this.node = node;
            this.templateItems = templateItems;
        }

    }


    /**
     * A helper method which allows us to deal with item categories more easily,
     * simply supply the item category type and an array of values in EvalConstants.ITEM_CATEGORY_ORDER
     * to match against the category
     * 
     * @param categoryType the category to match
     * @param values the values corresponding to the categories
     * @return the appropriate value
     * @throws IllegalStateException if the categoryType does not match a valid one
     */
    //   public static String getValueForCategory(String categoryType, String... values) {
    //      boolean found = false;
    //      if (values == null 
    //            || values.length != EvalConstants.ITEM_CATEGORY_ORDER.length) {
    //         throw new IllegalArgumentException("Must supply the same number of values as the number of valid categories");
    //      }
    //      String value = null;
    //      for (int i = 0; i < EvalConstants.ITEM_CATEGORY_ORDER.length; i++) {
    //         if (categoryType.equals(EvalConstants.ITEM_CATEGORY_ORDER[i])) {
    //            found = true;
    //            value = values[i];
    //         }
    //      }
    //      if (! found) {
    //         throw new IllegalStateException("Don't know how to handle rendering category type: " + categoryType);
    //      }
    //      return value;
    //   }

    /**
     * A helper method to get the list of unique instructor userIds 
     * for the {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR} type item answers from the list of answers<br/>
     * <b>NOTE:</b> Use getEvalUsersByIds(String[]) from commonLogic to turn this into a set of EvalUsers if needed
     * 
     * @param answers a list of {@link EvalAnswer}
     * @return the set of userIds
     */
    public static Set<String> getInstructorsForAnswers(List<EvalAnswer> answers) {
        Set<String> userIds = new HashSet<String>();
        for (EvalAnswer answer: answers) {
            if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(answer.getAssociatedType())) {
                if (! EvalUtils.isBlank(answer.getAssociatedId())) {
                    userIds.add(answer.getAssociatedId());
                }
            }
        }
        return userIds;
    }

    /**
     * A helper method to get the list of unique TA userIds 
     * for the {@link EvalConstants#ITEM_CATEGORY_ASSISTANT} type item answers from the list of answers<br/>
     * <b>NOTE:</b> Use getEvalUsersByIds(String[]) from commonLogic to turn this into a set of EvalUsers if needed
     * 
     * @param answers a list of {@link EvalAnswer}
     * @return the set of userIds
     */
    public static Set<String> getAssistantsForAnswers(List<EvalAnswer> answers) {
        Set<String> userIds = new HashSet<String>();
        for (EvalAnswer answer: answers) {
            if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(answer.getAssociatedType())) {
                if (! EvalUtils.isBlank(answer.getAssociatedId())) {
                    userIds.add(answer.getAssociatedId());
                }
            }
        }
        return userIds;
    }

    /**
     * This helper method deals with producing an array of total number of responses for
     * a list of answers.  It does not deal with any of the logic (such as groups,
     * etc.) it takes to get a list of answers.
     * 
     * The item type should be the expected constant from EvalConstants. For 
     * scaled and multiple choice questions, this will count the answers numeric
     * field for each scale.  For multiple answers, it will aggregate all the responses
     * for each answer to their scale choice.
     * 
     * If the item allows Not Applicable answers, this tally will be included as 
     * the very last item of the array, allowing you to still match up the indexes
     * with the original Scale options.
     * 
     * @param templateItemType the template item type from {@link TemplateItemUtils#getTemplateItemType(EvalTemplateItem)},
     * e.g. {@link EvalConstants#ITEM_TYPE_MULTIPLECHOICE}
     * @param scaleChoices the number of scale choices (normally this is the size of the {@link EvalScale#getOptions()} array. 
     * The returned integer array will be this big (+1 for NA), each index being a count of answers for that scale choice
     * @param answers the List of EvalAnswers to work with
     * @return an integer array which is the same size as the number of choices + 1 (for NA), ignore the last array entry if NA is not used for this item
     * @throws IllegalArgumentException if this is not an itemType that has numeric answers (Scaled/MC/MA/...)
     */
    public static int[] getAnswerChoicesCounts(String templateItemType, int scaleChoices, List<EvalAnswer> itemAnswers) {
        // Make the array one size larger in case we need to add N/A tallies.
        int[] togo = new int[scaleChoices+1];

        if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType) 
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType) 
                || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType) ) {

            for (EvalAnswer answer: itemAnswers) {
                EvalUtils.decodeAnswerNA(answer);
                if (answer.NA) {
                    togo[togo.length-1]++;
                }
                else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
                    // special handling for the multiple answer items
                    if (! EvalConstants.NO_MULTIPLE_ANSWER.equals(answer.getMultiAnswerCode())) {
                        // this multiple answer is not one that should be ignored
                        Integer[] decoded = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
                        for (Integer decodedAnswer: decoded) {
                            incrementArraySafely(decodedAnswer.intValue(), togo);
                        }
                    }
                }
                else {
                    // standard handling for single answer items
                    if (! EvalConstants.NO_NUMERIC_ANSWER.equals(answer.getNumeric())) {
                        // this numeric answer is not one that should be ignored
                        incrementArraySafely(answer.getNumeric().intValue(), togo);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The itemType needs to be one that has numeric answers, this one is invalid: " + templateItemType);
        }

        return togo;
    }

    /**
     * Ensures we will not get AIOOB exceptions
     * @param answerValue
     * @param togo
     */
    private static void incrementArraySafely(int answerValue, int[] togo) {
        if (answerValue >= 0 && answerValue < togo.length) {
            // answer will fit in the array
            togo[answerValue]++;
        } else {
            // put it in the NA slot
            togo[togo.length-1]++;
        }
    }

    /**
     * Create a list of hierarchy nodes used in a set of template items,
     * this will return all hierarchy nodes which have items in this set assigned to them
     * 
     * @param templateItems the list of template items for an evaluation
     * @param hierarchyLogic the hierarchy logic service (used to look up the node objects)
     * @return the list of all hierarchy nodes
     */
    public static List<EvalHierarchyNode> makeEvalNodesList(List<EvalTemplateItem> templateItems, ExternalHierarchyLogic hierarchyLogic) {
        if (templateItems == null || hierarchyLogic == null) {
            throw new IllegalArgumentException("inputs ("+templateItems+","+hierarchyLogic+") must not be null");
        }
        Set<String> nodeIds = new HashSet<String>();
        for (EvalTemplateItem templateItem : templateItems) {
            if (EvalConstants.HIERARCHY_LEVEL_NODE.equals(templateItem.getHierarchyLevel())) {
                nodeIds.add(templateItem.getHierarchyNodeId());
            }
        }
        List<EvalHierarchyNode> hierarchyNodes = new ArrayList<EvalHierarchyNode>();
        if (nodeIds.size() > 0) {
            Set<EvalHierarchyNode> nodes = hierarchyLogic.getNodesByIds(nodeIds.toArray(new String[nodeIds.size()]));
            hierarchyNodes = hierarchyLogic.getSortedNodes(nodes);
        }
        return hierarchyNodes;
    }

}
