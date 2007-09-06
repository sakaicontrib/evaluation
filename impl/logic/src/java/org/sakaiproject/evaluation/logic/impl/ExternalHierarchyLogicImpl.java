/**
 * ExternalHierarchyLogicImpl.java - evaluation - 2007 Sep 6, 2007 1:30:29 PM - AZ
 */

package org.sakaiproject.evaluation.logic.impl;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;


/**
 * 
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImpl implements ExternalHierarchyLogic {

   private HierarchyService hierarchyService;
   public void setHierarchyService(HierarchyService hierarchyService) {
      this.hierarchyService = hierarchyService;
   }

//   private PermTokenGeneratorService permTokenGeneratorService;
//   public void setPermTokenGeneratorService(PermTokenGeneratorService permTokenGeneratorService) {
//      this.permTokenGeneratorService = permTokenGeneratorService;
//   }

   public static final String HIERARCHY_ID = "evaluationHierarchyId";  

   /**
    * Place any code that should run when this class is initialized by spring here
    */
   public void init() {
      // create the hierarchy if it is not there already
      if (hierarchyService.getRootNode(HIERARCHY_ID) == null) {
         hierarchyService.createHierarchy(HIERARCHY_ID);
      }
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getRootLevelNode()
    */
   public EvalHierarchyNode getRootLevelNode() {
      HierarchyNode node = hierarchyService.getRootNode(HIERARCHY_ID);
      return makeEvalNode(node);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getNodeById(java.lang.String)
    */
   public EvalHierarchyNode getNodeById(String nodeId) {
      HierarchyNode node = hierarchyService.getNodeById(nodeId);
      // TODO add in perms and associated data?
      return makeEvalNode(node);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#addNode(java.lang.String)
    */
   public EvalHierarchyNode addNode(String parentNodeId) {
      HierarchyNode node = hierarchyService.addNode(HIERARCHY_ID, parentNodeId);
      return makeEvalNode(node);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#removeNode(java.lang.String)
    */
   public EvalHierarchyNode removeNode(String nodeId) {
      HierarchyNode node = hierarchyService.removeNode(nodeId);
      return makeEvalNode(node);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#updateNodeData(java.lang.String, java.lang.String, java.lang.String)
    */
   public EvalHierarchyNode updateNodeData(String nodeId, String title, String description) {
      HierarchyNode node = hierarchyService.saveNodeMetaData(nodeId, title, description, null);
      return makeEvalNode(node);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getChildNodes(java.lang.String, boolean)
    */
   public Set<EvalHierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
      Set<HierarchyNode> nodes = hierarchyService.getChildNodes(nodeId, directOnly);
      Set<EvalHierarchyNode> eNodes = new TreeSet<EvalHierarchyNode>();
      for (HierarchyNode node : nodes) {
         eNodes.add(makeEvalNode(node));
      }
      return null;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#setEvalGroupsForNode(java.lang.String, java.util.Set)
    */
   public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds) {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getEvalGroupsForNode(java.lang.String)
    */
   public Set<EvalGroup> getEvalGroupsForNode(String nodeId) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getNodesAboveEvalGroup(java.lang.String)
    */
   public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId) {
      // TODO Auto-generated method stub
      return null;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#assignUserNodePerm(java.lang.String, java.lang.String, java.lang.String)
    */
   public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)
    */
   public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getNodesForUserPerm(java.lang.String, java.lang.String)
    */
   public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)
    */
   public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      return null;
   }

   
   

   /**
    * Create an eval node from a basic hierarchy node
    * @param node a {@link HierarchyNode}
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
}
