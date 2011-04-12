/**
 * $Id$
 * $URL$
 * MockExternalHierarchyLogic.java - evaluation - Mar 5, 2008 12:25:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.test.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.utils.EvalUtils;


/**
 * Mock for the hierarchy logic interface,
 * extremely simplistic with only 2 nodes, root -> child,
 * defaults to having no groups associated with the hierarchy to start out
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockExternalHierarchyLogic implements ExternalHierarchyLogic {

    public EvalHierarchyNode root = new EvalHierarchyNode("1", "root", "root");
    public EvalHierarchyNode child = new EvalHierarchyNode("2", "child", "child");
    public HashMap<String, Set<String>> evalGroupNodes = new HashMap<String, Set<String>>();

    public MockExternalHierarchyLogic() {
        root.childNodeIds = new HashSet<String>();
        root.childNodeIds.add(child.id);
        root.directChildNodeIds = root.childNodeIds;

        root.parentNodeIds = new HashSet<String>();
        root.directParentNodeIds = root.parentNodeIds;
        evalGroupNodes.put(root.id, new HashSet<String>());

        child.childNodeIds = new HashSet<String>();
        child.directChildNodeIds = child.childNodeIds;

        child.parentNodeIds = new HashSet<String>();
        child.childNodeIds.add(root.id);
        child.directParentNodeIds = child.parentNodeIds;
        evalGroupNodes.put(child.id, new HashSet<String>());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#addNode(java.lang.String)
     */
    public EvalHierarchyNode addNode(String parentNodeId) {
        // ensure a non-null return
        EvalHierarchyNode node = new EvalHierarchyNode( EvalUtils.makeUniqueIdentifier(5), "fake", "fake");
        node.directParentNodeIds = new HashSet<String>();
        node.directParentNodeIds.add(parentNodeId);
        node.childNodeIds = node.directParentNodeIds;
        return node;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#getAllChildrenNodes(java.util.Collection, boolean)
     */
    public Set<String> getAllChildrenNodes(Collection<EvalHierarchyNode> nodes, boolean includeSuppliedNodeIds) {
        Set<String> children = new HashSet<String>();
        for (EvalHierarchyNode node : nodes) {
            if (includeSuppliedNodeIds) {
                children.add(node.id);
            }
            if (node.id.equals(root.id)) {
                children.add(child.id);
            }
        }
        return children;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#removeNode(java.lang.String)
     */
    public EvalHierarchyNode removeNode(String nodeId) {
        EvalHierarchyNode node = null;
        if (root.id.equals(nodeId)) {
            throw new IllegalArgumentException("Cannot remove the root");
        } else if (child.id.equals(nodeId)) {
            node = root;
        }
        return node;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#setEvalGroupsForNode(java.lang.String, java.util.Set)
     */
    public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds) {
        if (evalGroupIds.isEmpty()) {
            evalGroupNodes.remove(nodeId);
        } else {
            evalGroupNodes.put(nodeId, evalGroupIds);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#updateNodeData(java.lang.String, java.lang.String, java.lang.String)
     */
    public EvalHierarchyNode updateNodeData(String nodeId, String title, String description) {
        EvalHierarchyNode node = getNodeFromId(nodeId);
        if (node != null) {
            node.title = title;
            node.description = description;
        }
        return node;
    }

    /**
     * @param nodeId
     * @return
     */
    private EvalHierarchyNode getNodeFromId(String nodeId) {
        EvalHierarchyNode node = null;
        if (root.id.equals(nodeId)) {
            node = root;
        } else if (child.id.equals(nodeId)) {
            node = child;
        }
        return node;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#countEvalGroupsForNodes(java.lang.String[])
     */
    public Map<String, Integer> countEvalGroupsForNodes(String[] nodeIds) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (String nodeId : nodeIds) {
            Integer count = 0;
            if (evalGroupNodes.containsKey(nodeId)) {
                count = evalGroupNodes.get(nodeId).size();
            }
            counts.put(nodeId, count);
        }
        return counts;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getChildNodes(java.lang.String, boolean)
     */
    public Set<EvalHierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
        Set<EvalHierarchyNode> nodes = new HashSet<EvalHierarchyNode>();
        if (root.id.equals(nodeId)) {
            nodes.add(child);
        }
        return nodes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getEvalGroupsForNode(java.lang.String)
     */
    public Set<String> getEvalGroupsForNode(String nodeId) {
        Set<String> groups = new HashSet<String>();
        if (evalGroupNodes.containsKey(nodeId)) {
            groups = evalGroupNodes.get(nodeId);
        }
        return groups;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getEvalGroupsForNodes(java.lang.String[])
     */
    public Map<String, Set<String>> getEvalGroupsForNodes(String[] nodeIds) {
        Map<String, Set<String>> m = new HashMap<String, Set<String>>();
        for (int i = 0; i < nodeIds.length; i++) {
            m.put(nodeIds[i], getEvalGroupsForNode(nodeIds[i]));
        }
        return m;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodeById(java.lang.String)
     */
    public EvalHierarchyNode getNodeById(String nodeId) {
        return getNodeFromId(nodeId);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodesAboveEvalGroup(java.lang.String)
     */
    public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId) {
        List<EvalHierarchyNode> l = new ArrayList<EvalHierarchyNode>();
        for (String nodeId : evalGroupNodes.keySet()) {
            if (evalGroupNodes.get(nodeId).contains(evalGroupId)) {
                l.add(root);
                if (nodeId.equals(child.id)) {
                    l.add(child);
                }
            }
        }
        return l;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodesByIds(java.lang.String[])
     */
    public Set<EvalHierarchyNode> getNodesByIds(String[] nodeIds) {
        Set<EvalHierarchyNode> nodes = new HashSet<EvalHierarchyNode>();
        for (int i = 0; i < nodeIds.length; i++) {
            EvalHierarchyNode node = getNodeFromId(nodeIds[i]);
            if (node != null) {
                nodes.add( node );
            }
        }
        return nodes;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getRootLevelNode()
     */
    public EvalHierarchyNode getRootLevelNode() {
        return root;
    }


    public List<EvalHierarchyNode> getSortedNodes(Collection<EvalHierarchyNode> nodes) {
        // doesn't really sort anything
        return new ArrayList<EvalHierarchyNode>(nodes);
    }


    // NO PERMS STUFF WORKING YET

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getNodesForUserPerm(java.lang.String, java.lang.String)
     */
    public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalHierarchyProvider#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)
     */
    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant) {
        // TODO Auto-generated method stub
        return null;
    }

    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant,
            boolean cascade) {
        // TODO Auto-generated method stub

    }

    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermConstant,
            boolean cascade) {
        // TODO Auto-generated method stub

    }

    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds) {
        // TODO Auto-generated method stub
        return null;
    }

}
