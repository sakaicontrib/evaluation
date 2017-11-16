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
package org.sakaiproject.evaluation.logic.externals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.providers.EvalHierarchyProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.utils.HierarchyUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;

/**
 * Allows Evaluation to interface with an external hierarchy system,
 * also plugs into the provider
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImpl implements ExternalHierarchyLogic {

    private static final Log LOG = LogFactory.getLog(ExternalHierarchyLogicImpl.class);

    private EvaluationDao dao;
    public void setDao(EvaluationDao evaluationDao) {
        this.dao = evaluationDao;
    }

    private HierarchyService hierarchyService;
    public void setHierarchyService(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalHierarchyProvider evalHierarchyProvider;
    public void setEvalHierarchyProvider(EvalHierarchyProvider evalHierarchyProvider) {
        this.evalHierarchyProvider = evalHierarchyProvider;
    }

    private CourseManagementService courseManagementService;
    public void setCourseManagementService( CourseManagementService courseManagementService ) {
        this.courseManagementService = courseManagementService;
    }
    
    private SiteService siteService;
    public void setSiteService( SiteService siteService ) {
        this.siteService = siteService;
    }
    
    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService( AuthzGroupService authzGroupService ) {
        this.authzGroupService = authzGroupService;
    }

    public static final String HIERARCHY_ID = "evaluationHierarchyId";
    public static final String HIERARCHY_ROOT_TITLE = "Root";

    /**
     * Place any code that should run when this class is initialized by spring here
     */
    public void init() {
        // create the hierarchy if it is not there already
        if (hierarchyService.getRootNode(HIERARCHY_ID) == null) {
            HierarchyNode root = hierarchyService.createHierarchy(HIERARCHY_ID);
            hierarchyService.saveNodeMetaData(root.id, HIERARCHY_ROOT_TITLE, null, null);
            LOG.info("Created the root node for the eval hierarchy: " + HIERARCHY_ID);
        }
        // get the provider if there is one
        // setup provider
        if (evalHierarchyProvider == null) {
            evalHierarchyProvider = (EvalHierarchyProvider) externalLogic.getBean(EvalHierarchyProvider.class);
            if (evalHierarchyProvider != null)
            {
                LOG.info("EvalHierarchyProvider found...");
            }
        } else {
            LOG.debug("No EvalHierarchyProvider found...");
        }
    }

    /*
      * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#getSectionsUnderEvalGroup(java.lang.String)
     */
    public List<Section> getSectionsUnderEvalGroup( String evalGroupID )
    {
        if( evalGroupID == null )
        {
            throw new IllegalArgumentException( "evalGroupId cannot be null" );
        }

        if( !evalGroupID.startsWith( EvalConstants.GROUP_ID_SITE_PREFIX ) )
        {
            throw new IllegalArgumentException( "cannot determine sections of groupId='" + evalGroupID + "' (must be a site)" );
        }

        // Determine if the group ID is pointing to a single section
        List<Section> sections = new ArrayList<>();
        boolean isSingleSection = false;
        if( evalGroupID.contains( EvalConstants.GROUP_ID_SECTION_PREFIX ) )
        {
            isSingleSection = true;
        }

        try
        {
            // Get the site ID
            String siteID = evalGroupID.replace( EvalConstants.GROUP_ID_SITE_PREFIX, "" );

            // If the evalGroup is pointing to a single section, get the single session, add it to the list and return
            if( isSingleSection )
            {
                sections.add( courseManagementService.getSection( evalGroupID.substring( evalGroupID.indexOf( EvalConstants.GROUP_ID_SECTION_PREFIX ) 
                                                                                         + EvalConstants.GROUP_ID_SECTION_PREFIX.length() ) ) );
            }

            // Otherwise, the evalGroup is pointing at a site...
            else
            {
                // Add all the sections from this site and return
                String realmID = siteService.siteReference( siteID );
                Set<String> sectionIDs = authzGroupService.getProviderIds( realmID );
                for( String secID : sectionIDs )
                {
                    sections.add( courseManagementService.getSection( secID ) );
                }
            }
        }
        catch( Exception ex ) {}

        return sections;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#determineQualifierFromRuleText(java.lang.String)
     */
    public String determineQualifierFromRuleText( String ruleText )
    {
        if( ruleText.startsWith( "%" ) && ruleText.endsWith( "%" ) )
        {
            return EvalConstants.HIERARCHY_QUALIFIER_CONTAINS;
        }
        else if( ruleText.startsWith( "%" ) )
        {
            return EvalConstants.HIERARCHY_QUALIFIER_ENDS_WITH;
        }
        else if( ruleText.endsWith( "%" ) )
        {
            return EvalConstants.HIERARCHY_QUALIFIER_STARTS_WITH;
        }
        else
        {
            return EvalConstants.HIERARCHY_QUALIFIER_IS;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#removeQualifierFromRuleText(java.lang.String)
     */
    public String removeQualifierFromRuleText( String ruleText )
    {
        String rule = ruleText;
        if( rule.startsWith( "%" ) )
        {
            rule = rule.substring( 1 );
        }
        if( rule.endsWith( "%" ) )
        {
            rule = rule.substring( 0, rule.length() - 1 );
        }

        return rule;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#isRuleAlreadyAssignedToNode(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public boolean isRuleAlreadyAssignedToNode( String ruleText, String qualifierSelection, String optionSelection, Long nodeID )
    {
        // Check that the node exists first
        checkNodeExists( nodeID );

        return externalLogic.isRuleAlreadyAssignedToNode( ruleText, qualifierSelection, optionSelection, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#assignNodeRule(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void assignNodeRule( String ruleText, String qualifier, String option, Long nodeID )
    {
        // Check that the node exists first
        checkNodeExists( nodeID );

        externalLogic.assignNodeRule( ruleText, qualifier, option, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeNodeRule(java.lang.Long)
     */
    public void removeNodeRule( Long ruleID )
    {
        externalLogic.removeNodeRule( ruleID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeAllRulesForNode(java.lang.Long)
     */
    public void removeAllRulesForNode( Long nodeID )
    {
        // Check that the node exists first
        checkNodeExists( nodeID );

        externalLogic.removeAllRulesForNode( nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#updateNodeRule(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void updateNodeRule( Long ruleID, String ruleText, String qualifier, String option, Long nodeID )
    {
        // Check that the node exists first
        checkNodeExists( nodeID );

        externalLogic.updateNodeRule( ruleID, ruleText, qualifier, option, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRulesByNodeID(java.lang.Long)
     */
    public List<HierarchyNodeRule> getRulesByNodeID( Long nodeID )
    {
        // Check that the node exists first
        checkNodeExists( nodeID );

        return externalLogic.getRulesByNodeID( nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRuleByID(java.lang.Long)
     */
    public HierarchyNodeRule getRuleByID( Long ruleID )
    {
        return externalLogic.getRuleByID( ruleID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getAllRules()
     */
    public List<HierarchyNodeRule> getAllRules()
    {
        return externalLogic.getAllRules();
    }

    /**
     * Utility method to check if a hierarchy node exists.
     * If it doesn't exist, it will throw an IllegalArgumentException
     * @param nodeID the ID of the node to check for existence
     * @
     */
    private void checkNodeExists( Long nodeID )
    {
        if( nodeID == null )
        {
            throw new IllegalArgumentException( "Node ID provided is invalid (null)" );
        }

        EvalHierarchyNode node = getNodeById( nodeID.toString() );
        if( node == null )
        {
            throw new IllegalArgumentException( "Node ID (" + nodeID + ") provided is invalid, node does not exist" );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getRootLevelNode()
     */
    public EvalHierarchyNode getRootLevelNode() {
        EvalHierarchyNode node = null;
        if (evalHierarchyProvider != null) {
            node = evalHierarchyProvider.getRootLevelNode();
        }
        if (node == null) {
            HierarchyNode hNode = hierarchyService.getRootNode(HIERARCHY_ID);
            node = makeEvalNode(hNode);
        }
        return node;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodeById(java.lang.String)
     */
    public EvalHierarchyNode getNodeById(String nodeId) {
        EvalHierarchyNode node = null;
        if (evalHierarchyProvider != null) {
            node = evalHierarchyProvider.getNodeById(nodeId);
        }
        if (node == null) {
            HierarchyNode hNode = hierarchyService.getNodeById(nodeId);
            node = makeEvalNode(hNode);
        }
        return node;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#addNode(java.lang.String)
     */
    public EvalHierarchyNode addNode(String parentNodeId) {
        HierarchyNode node = hierarchyService.addNode(HIERARCHY_ID, parentNodeId);
        return makeEvalNode(node);
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#removeNode(java.lang.String)
     */
    public EvalHierarchyNode removeNode(String nodeId) {
        // fail to remove nodes when there are associated evalGroups
        Map<String, Integer> egCount = countEvalGroupsForNodes( new String[] {nodeId} );
        if (egCount.get(nodeId) > 0) {
            throw new IllegalArgumentException("Cannot remove this node because there are associated eval groups, " +
            "you must remove the associated evalgroups from this node before you can remove the node");
        }

        HierarchyNode node = hierarchyService.removeNode(nodeId);

        // Remove all hierarchy rules associated with this node
        externalLogic.removeAllRulesForNode( Long.parseLong( nodeId ) );

        // cleanup related data
        List<EvalTemplateItem> l = dao.findBySearch(EvalTemplateItem.class, new Search("hierarchyNodeId", nodeId) );
        for (EvalTemplateItem templateItem : l) {
            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
            templateItem.setHierarchyNodeId(EvalConstants.HIERARCHY_NODE_ID_NONE);
        }
        dao.saveSet( new HashSet<EvalTemplateItem>(l) );
        // return the parent node
        return makeEvalNode(node);
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#updateNodeData(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public EvalHierarchyNode updateNodeData(String nodeId, String title, String description) {
        HierarchyNode node = hierarchyService.saveNodeMetaData(nodeId, title, description, null);
        return makeEvalNode(node);
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getChildNodes(java.lang.String,
     *      boolean)
     */
    public Set<EvalHierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
        Set<EvalHierarchyNode> eNodes = new HashSet<>();
        if (evalHierarchyProvider != null) {
            eNodes = evalHierarchyProvider.getChildNodes(nodeId, directOnly);
        } else {
            Set<HierarchyNode> nodes = hierarchyService.getChildNodes(nodeId, directOnly);
            for (HierarchyNode node : nodes) {
                EvalHierarchyNode eNode = makeEvalNode(node);
                if (eNode != null) {
                    eNodes.add( eNode );
                }
            }
        }
        return eNodes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#getAllChildrenNodes(java.util.Collection, boolean)
     */
    public Set<String> getAllChildrenNodes(Collection<EvalHierarchyNode> nodes, boolean includeSuppliedNodeIds) {
        Set<String> s = new HashSet<>();
        for (EvalHierarchyNode node : nodes) {
            if (includeSuppliedNodeIds) {
                s.add(node.id);
            }
            s.addAll(node.childNodeIds);
        }
        return s;
    }

    public Set<EvalHierarchyNode> getNodesByIds(String[] nodeIds) {
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot br null");
        }
        Set<EvalHierarchyNode> s = new HashSet<>();
        if (evalHierarchyProvider != null) {
            s = evalHierarchyProvider.getNodesByIds(nodeIds);
        } else {
            Map<String, HierarchyNode> nodes = hierarchyService.getNodesByIds(nodeIds);
            for (HierarchyNode node : nodes.values()) {
                EvalHierarchyNode eNode = makeEvalNode(node);
                if (eNode != null) {
                    s.add( eNode );
                }
            }
        }
        return s;
    }

    public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds) {
        if (hierarchyService.getNodeById(nodeId) == null) {
            throw new IllegalArgumentException("Invalid node id, this node does not exist: " + nodeId);
        }
        EvalGroupNodes egn = getEvalGroupNodeByNodeId(nodeId);
        if (evalGroupIds == null || evalGroupIds.isEmpty()) {
            if (egn != null) {
                // clean up the object if we are removing all the attached eval groups
                dao.delete(egn);
            }
        } else {
            if (egn == null) {
                egn = new EvalGroupNodes(new Date(), nodeId);
            }
            String[] evalGroups = evalGroupIds.toArray(new String[] {});
            egn.setEvalGroups(evalGroups);
            dao.save(egn);         
        }
    }

    public Set<String> getEvalGroupsForNode(String nodeId) {
        if (nodeId == null || nodeId.equals("")) {
            throw new IllegalArgumentException("nodeId cannot be null or blank");
        }
        Set<String> s = new HashSet<>();
        if (evalHierarchyProvider != null) {
            s = evalHierarchyProvider.getEvalGroupsForNode(nodeId);
        }

        // Support for resolving eval groups based on hierarchy node rules.
        // Hierarchy rules should be obeyed regardless of if an external provider is present or not (supplemental)
        s.addAll( getEvalGroupsForNodeSectionAware( nodeId ) );

        return s;
    }

    public Map<String, Set<String>> getEvalGroupsForNodes(String[] nodeIds) {
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot be null");
        }
        Map<String, Set<String>> m = new HashMap<>();
        if (nodeIds.length > 0) {
            if (evalHierarchyProvider != null) {
                m = evalHierarchyProvider.getEvalGroupsForNodes(nodeIds);
            }

            // Support for resolving eval groups based on hierarchy node rules.
            // Hierarchy rules should be obeyed regardless of if an external provider is present or not (supplemental)
            for( String nodeID : nodeIds )
            {
                Set<String> evalGroupsForNodeSectionAware = getEvalGroupsForNodeSectionAware(nodeID);
                if (!evalGroupsForNodeSectionAware.isEmpty()) {
                    m.put(nodeID, evalGroupsForNodeSectionAware);
                }
            }
        }
        return m;
    }

    /**
     * Utility method to extract common (section aware) behaviour.
     * 
     * @param nodeID
     * @return 
     */
    private Set<String> getEvalGroupsForNodeSectionAware( String nodeID )
    {
        Set<String> groups = new HashSet<>();
        try
        {
            EvalGroupNodes egn = getEvalGroupNodeByNodeId(nodeID);
            if (egn != null) {
                String[] evalGroups = egn.getEvalGroups();
                for (int i = 0; i < evalGroups.length; i++) {
                    groups.add(evalGroups[i]);
                }
            }

            List<HierarchyNodeRule> rules = getRulesByNodeID( Long.parseLong( nodeID ) );
            for( HierarchyNodeRule rule : rules )
            {
                if( EvalConstants.HIERARCHY_RULE_SECTION.equals( rule.getOption() ) )
                {
                    groups.addAll( dao.getAllSiteIDsMatchingSectionTitle( rule.getRule() ) );
                }
                else
                {
                    groups.addAll( dao.getAllSiteIDsMatchingSiteTitle( rule.getRule() ) );
                }
            }
        }
        catch( NumberFormatException ex ) { LOG.warn( ex ); }

        return groups;
    }

    public Map<String, Integer> countEvalGroupsForNodes(String[] nodeIds) {
        Map<String, Integer> nodeGroupSizeMap;
        if (evalHierarchyProvider != null) {
            nodeGroupSizeMap = evalHierarchyProvider.countEvalGroupsForNodes(nodeIds);
        }
        else
        {
            nodeGroupSizeMap = new HashMap<>();
        }

        for( String nodeId : nodeIds )
        {
            if( !nodeGroupSizeMap.containsKey( nodeId ) )
            {
                nodeGroupSizeMap.put( nodeId, 0 );
            }
        }

        // Support for resolving eval groups based on hierarchy node rules
        // Hierarchy rules should be obeyed regardless of if an external provider is present or not (supplemental)
        Map<String, Set<String>> nodeGroupsMap = getEvalGroupsForNodes( nodeIds );
        for( String nodeID : nodeGroupsMap.keySet() )
        {
            nodeGroupSizeMap.put( nodeID, nodeGroupsMap.get( nodeID ).size() );
        }

        return nodeGroupSizeMap;
    }

    public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId) {
        List<EvalHierarchyNode> hierarchyNodes;
        if (evalHierarchyProvider != null) {
            hierarchyNodes = evalHierarchyProvider.getNodesAboveEvalGroup(evalGroupId);
        }
        else
        {
            hierarchyNodes = new ArrayList<>();
        }

        // Support for resolving eval groups based on hierarchy node rules
        // Hierarchy rules should be obeyed regardless of if an external provider is present or not (supplemental)
        String nodeID = "";
        String siteID = evalGroupId.replace( EvalConstants.GROUP_ID_SITE_PREFIX, "" );
        boolean isGroupIDSectionBased = false;
        if( evalGroupId.contains( EvalConstants.GROUP_ID_SECTION_PREFIX ) )
        {
            siteID = siteID.substring( 0, siteID.indexOf( EvalConstants.GROUP_ID_SECTION_PREFIX ) );
            isGroupIDSectionBased = true;
        }

        try
        {
            Site site = siteService.getSite( siteID );
            List<HierarchyNodeRule> rules = externalLogic.getAllRules();
            Map<String, Set<String>> realmSectionIDs = new HashMap<>();
            for( HierarchyNodeRule rule : rules )
            {
                String rawRuleText = removeQualifierFromRuleText( rule.getRule() );
                String qualifier = determineQualifierFromRuleText( rule.getRule() );

                // If the rule is section based and so is the group ID...
                if( EvalConstants.HIERARCHY_RULE_SECTION.equals( rule.getOption() ) && isGroupIDSectionBased )
                {
                    // Get the sections of the site, if necessary
                    String realmID = siteService.siteReference( site.getId() );
                    if( !realmSectionIDs.containsKey( realmID ) )
                    {
                        realmSectionIDs.put( realmID, authzGroupService.getProviderIds( realmID ) );
                    }

                    for( String sectionID : realmSectionIDs.get( realmID ) )
                    {
                        Section section = courseManagementService.getSection( sectionID );
                        if( siteOrSectionTitleSatisfiesRule( section.getTitle(), qualifier, rawRuleText ) )
                        {
                            nodeID = rule.getNodeID().toString();
                        }
                    }
                }

                // Otherwise it's a site based rule...
                else
                {
                    if( siteOrSectionTitleSatisfiesRule( site.getTitle(), qualifier, rawRuleText ) )
                    {
                        nodeID = rule.getNodeID().toString();
                    }
                }
            }
        }
        catch ( IdUnusedException ex ) { LOG.debug("IdUnusedException looking up site ID", ex); }
        catch ( IdNotFoundException ex ) { LOG.warn( "Could not find site or section by ID", ex ); }

        if (StringUtils.isNotBlank(nodeID)) {
            HierarchyNode currentNode = hierarchyService.getNodeById(nodeID);
            Set<HierarchyNode> parents = hierarchyService.getParentNodes(nodeID, false);
            parents.add(currentNode);
            List<HierarchyNode> sorted = HierarchyUtils.getSortedNodes(parents);
            // now convert the nodes to eval nodes
            for (HierarchyNode node : sorted) {
                hierarchyNodes.add( makeEvalNode(node) );
            }
        }

        return hierarchyNodes;
    }

    /**
     * Utility method to determine if a site or section title satisfies a given rule's
     * qualifier and rule text.
     * 
     * @param siteOrSectionTitle the site or section title to compare
     * @param ruleQualifier the qualifier of the rule (is, starts with, ends with or contains)
     * @param ruleText the actual text of the rule to be obeyed
     * @return true if the site or section title satisfies the rule; false otherwise
     */
    private boolean siteOrSectionTitleSatisfiesRule( String siteOrSectionTitle, String ruleQualifier, String ruleText )
    {
        // If the qualifier is 'contains' and the title contains the raw rule text, OR
        // the qualifier is 'ends with' and the title ends with the raw rule text, OR
        // the qualifier is 'starts with' and the title starts with the raw rule text, OR
        // the qualifier is 'is' and the title equals the raw rule text, grab the node ID of the rule
        return (EvalConstants.HIERARCHY_QUALIFIER_CONTAINS.equals( ruleQualifier ) && siteOrSectionTitle.contains( ruleText ))
                            || (EvalConstants.HIERARCHY_QUALIFIER_ENDS_WITH.equals( ruleQualifier ) && siteOrSectionTitle.endsWith( ruleText ))
                            || (EvalConstants.HIERARCHY_QUALIFIER_STARTS_WITH.equals( ruleQualifier) && siteOrSectionTitle.startsWith( ruleText ))
                            || (EvalConstants.HIERARCHY_QUALIFIER_IS.equals( ruleQualifier ) && siteOrSectionTitle.equals( ruleText ));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#getSortedNodes(java.util.Collection)
     */
    public List<EvalHierarchyNode> getSortedNodes(Collection<EvalHierarchyNode> nodes) {
        List<HierarchyNode> hNodes = new ArrayList<>();
        for (EvalHierarchyNode eNode : nodes) {
            hNodes.add(makeHierarchyNode(eNode));
        }

        List<HierarchyNode> sortedNodes = HierarchyUtils.getSortedNodes(hNodes);

        List<EvalHierarchyNode> sortedENodes = new ArrayList<>();
        for (HierarchyNode hNode : sortedNodes) {
            sortedENodes.add( makeEvalNode(hNode) );
        }
        return sortedENodes;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyPermissions#assignUserNodePerm(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant, boolean cascade) {
        hierarchyService.assignUserNodePerm(userId, nodeId, hierarchyPermConstant, cascade);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyPermissions#removeUserNodePerm(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermConstant, boolean cascade) {
        hierarchyService.removeUserNodePerm(userId, nodeId, hierarchyPermConstant, cascade);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
        boolean allowed;
        if (evalHierarchyProvider != null) {
            allowed = evalHierarchyProvider.checkUserNodePerm(userId, nodeId, hierarchyPermConstant);
        } else {
            allowed = hierarchyService.checkUserNodePerm(userId, nodeId, hierarchyPermConstant);
        }
        return allowed;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodesForUserPerm(java.lang.String, java.lang.String)
     */
    public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant) {
        Set<EvalHierarchyNode> evalNodes = new HashSet<>();
        if (evalHierarchyProvider != null) {
            evalNodes = evalHierarchyProvider.getNodesForUserPerm(userId, hierarchyPermConstant);
        } else {
            Set<HierarchyNode> nodes = hierarchyService.getNodesForUserPerm(userId, hierarchyPermConstant);
            if (nodes != null && nodes.size() > 0) {
                for (HierarchyNode hierarchyNode : nodes) {
                    evalNodes.add( makeEvalNode(hierarchyNode) );
                }
            }
        }
        return evalNodes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)
     */
    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant) {
        Set<String> s;
        if (evalHierarchyProvider != null) {
            s = evalHierarchyProvider.getUserIdsForNodesPerm(nodeIds, hierarchyPermConstant);
        } else {
            s = hierarchyService.getUserIdsForNodesPerm(nodeIds, hierarchyPermConstant);
        }
        return s;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyPermissions#getPermsForUserNodes(java.lang.String, java.lang.String[])
     */
    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds) {
        return hierarchyService.getPermsForUserNodes(userId, nodeIds);
    }

    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds) {
        return hierarchyService.getNodesAndPermsForUser(userIds);
    }

    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds) {
        return hierarchyService.getUsersAndPermsForNodes(nodeIds);
    }


    /**
     * Create an eval node from a basic hierarchy node
     * 
     * @param node
     *           a {@link HierarchyNode}
     * @return an {@link EvalHierarchyNode} based on the basic node
     */
    private EvalHierarchyNode makeEvalNode(HierarchyNode node) {
        EvalHierarchyNode eNode = new EvalHierarchyNode();
        eNode.id = node.id;
        eNode.title = node.title;
        eNode.description = node.description;
        eNode.directChildNodeIds = node.directChildNodeIds;
        eNode.childNodeIds = node.childNodeIds;
        eNode.directParentNodeIds = node.directParentNodeIds;
        eNode.parentNodeIds = node.parentNodeIds;
        return eNode;
    }

    /**
     * Create an eval node from a basic hierarchy node
     * 
     * @param evalNode an {@link EvalHierarchyNode}
     * @return a {@link HierarchyNode} based on the eval node
     */
    private HierarchyNode makeHierarchyNode(EvalHierarchyNode evalNode) {
        HierarchyNode node = new HierarchyNode();
        node.id = evalNode.id;
        node.title = evalNode.title;
        node.description = evalNode.description;
        node.directChildNodeIds = evalNode.directChildNodeIds;
        node.childNodeIds = evalNode.childNodeIds;
        node.directParentNodeIds = evalNode.directParentNodeIds;
        node.parentNodeIds = evalNode.parentNodeIds;
        return node;
    }

    /**
     * Get an eval group node by a nodeid
     * @param nodeId
     * @return the {@link EvalGroupNodes} or null if none found
     */
    private EvalGroupNodes getEvalGroupNodeByNodeId(String nodeId) {
        List<EvalGroupNodes> l = getEvalGroupNodesByNodeId(new String[] {nodeId});
        EvalGroupNodes egn = null;
        if (!l.isEmpty()) {
            egn = (EvalGroupNodes) l.get(0);
        }
        return egn;
    }

    /**
     * Get a set of eval group nodes by a set of nodeIds
     * @param nodeIds
     * @return a list of egn or empty list if none found
     */
    private List<EvalGroupNodes> getEvalGroupNodesByNodeId(String[] nodeIds) {
        List<EvalGroupNodes> l = dao.findBySearch(EvalGroupNodes.class, new Search(
                new Restriction("nodeId", nodeIds),
                new Order("id")
        ) );
        return l;
    }

}
