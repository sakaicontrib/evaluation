/**
 * $Id$
 * $URL$
 * ExternalHierarchyLogicImpl.java - evaluation - Sep 6, 2007 1:30:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.providers.EvalHierarchyProvider;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.utils.HierarchyUtils;

/**
 * Allows Evaluation to interface with an external hierarchy system,
 * also plugs into the provider
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImpl implements ExternalHierarchyLogic {

    private static Log log = LogFactory.getLog(ExternalHierarchyLogicImpl.class);

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
            log.info("Created the root node for the eval hierarchy: " + HIERARCHY_ID);
        }
        // get the provider if there is one
        // setup provider
        if (evalHierarchyProvider == null) {
            evalHierarchyProvider = (EvalHierarchyProvider) externalLogic.getBean(EvalHierarchyProvider.class);
            if (evalHierarchyProvider != null)
                log.info("EvalHierarchyProvider found...");
        } else {
            log.debug("No EvalHierarchyProvider found...");
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
        Set<EvalHierarchyNode> eNodes = new HashSet<EvalHierarchyNode>();
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
        Set<String> s = new HashSet<String>();
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
        Set<EvalHierarchyNode> s = new HashSet<EvalHierarchyNode>();
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
        Set<String> s = new HashSet<String>();
        if (evalHierarchyProvider != null) {
            s = evalHierarchyProvider.getEvalGroupsForNode(nodeId);
        } else {
            EvalGroupNodes egn = getEvalGroupNodeByNodeId(nodeId);
            if (egn != null) {
                String[] evalGroups = egn.getEvalGroups();
                for (int i = 0; i < evalGroups.length; i++) {
                    s.add(evalGroups[i]);
                }
            }
        }
        return s;
    }

    public Map<String, Set<String>> getEvalGroupsForNodes(String[] nodeIds) {
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot be null");
        }
        Map<String, Set<String>> m = new HashMap<String, Set<String>>();
        if (nodeIds.length > 0) {
            if (evalHierarchyProvider != null) {
                m = evalHierarchyProvider.getEvalGroupsForNodes(nodeIds);
            } else {
                List<EvalGroupNodes> l = getEvalGroupNodesByNodeId(nodeIds);
                for (EvalGroupNodes egn : l) {
                    Set<String> s = new HashSet<String>();
                    String[] evalGroups = egn.getEvalGroups();
                    for (int i = 0; i < evalGroups.length; i++) {
                        s.add(evalGroups[i]);
                    }
                    m.put(egn.getNodeId(), s);
                }
            }
        }
        return m;
    }

    public Map<String, Integer> countEvalGroupsForNodes(String[] nodeIds) {
        Map<String, Integer> m = new HashMap<String, Integer>();
        if (evalHierarchyProvider != null) {
            m = evalHierarchyProvider.countEvalGroupsForNodes(nodeIds);
        } else {
            for (int i = 0; i < nodeIds.length; i++) {
                m.put(nodeIds[i], 0);
            }
    
            List<EvalGroupNodes> l = dao.findBySearch(EvalGroupNodes.class, new Search("nodeId", nodeIds) );
            for (Iterator<EvalGroupNodes> iter = l.iterator(); iter.hasNext();) {
                EvalGroupNodes egn = (EvalGroupNodes) iter.next();
                m.put(egn.getNodeId(), egn.getEvalGroups().length);
            }
        }
        return m;
    }

    public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId) {
        List<EvalHierarchyNode> l = new ArrayList<EvalHierarchyNode>();
        if (evalHierarchyProvider != null) {
            l = evalHierarchyProvider.getNodesAboveEvalGroup(evalGroupId);
        } else {
            String nodeId = dao.getNodeIdForEvalGroup(evalGroupId);
            if (nodeId != null) {
                HierarchyNode currentNode = hierarchyService.getNodeById(nodeId);
                Set<HierarchyNode> parents = hierarchyService.getParentNodes(nodeId, false);
                parents.add(currentNode);
                List<HierarchyNode> sorted = HierarchyUtils.getSortedNodes(parents);
                // now convert the nodes to eval nodes
                for (HierarchyNode node : sorted) {
                    l.add( makeEvalNode(node) );
                }
            }
        }
        return l;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#getSortedNodes(java.util.Collection)
     */
    public List<EvalHierarchyNode> getSortedNodes(Collection<EvalHierarchyNode> nodes) {
        List<HierarchyNode> hNodes = new ArrayList<HierarchyNode>();
        for (EvalHierarchyNode eNode : nodes) {
            hNodes.add(makeHierarchyNode(eNode));
        }

        List<HierarchyNode> sortedNodes = HierarchyUtils.getSortedNodes(hNodes);

        List<EvalHierarchyNode> sortedENodes = new ArrayList<EvalHierarchyNode>();
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
        boolean allowed = false;
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
        Set<EvalHierarchyNode> evalNodes = new HashSet<EvalHierarchyNode>();
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
        Set<String> s = null;
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
