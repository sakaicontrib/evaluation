/**
 * ExternalHierarchyLogicImpl.java - evaluation - 2007 Sep 6, 2007 1:30:29 PM - AZ
 */

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.utils.HierarchyUtils;

/**
 * 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImpl implements ExternalHierarchyLogic {

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private HierarchyService hierarchyService;
   public void setHierarchyService(HierarchyService hierarchyService) {
      this.hierarchyService = hierarchyService;
   }

   // private PermTokenGeneratorService permTokenGeneratorService;
   // public void setPermTokenGeneratorService(PermTokenGeneratorService permTokenGeneratorService)
   // {
   // this.permTokenGeneratorService = permTokenGeneratorService;
   // }

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
      }
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getRootLevelNode()
    */
   public EvalHierarchyNode getRootLevelNode() {
      HierarchyNode node = hierarchyService.getRootNode(HIERARCHY_ID);
      return makeEvalNode(node);
   }

   /*
    * (non-Javadoc)
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

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic#removeNode(java.lang.String)
    */
   public EvalHierarchyNode removeNode(String nodeId) {
      HierarchyNode node = hierarchyService.removeNode(nodeId);
      setEvalGroupsForNode(nodeId, new HashSet<String>());
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
    * @see org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider#getChildNodes(java.lang.String,
    *      boolean)
    */
   public Set<EvalHierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
      Set<HierarchyNode> nodes = hierarchyService.getChildNodes(nodeId, directOnly);
      Set<EvalHierarchyNode> eNodes = new TreeSet<EvalHierarchyNode>();
      for (HierarchyNode node : nodes) {
         eNodes.add(makeEvalNode(node));
      }
      return null;
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
      EvalGroupNodes egn = getEvalGroupNodeByNodeId(nodeId);
      Set<String> s = new HashSet<String>();
      if (egn != null) {
         String[] evalGroups = egn.getEvalGroups();
         for (int i = 0; i < evalGroups.length; i++) {
            s.add(evalGroups[i]);
         }
      }
      return s;
   }

   @SuppressWarnings("unchecked")
   public Map<String, Integer> countEvalGroupsForNodes(String[] nodeIds) {
      Map<String, Integer> m = new HashMap<String, Integer>();
      for (int i = 0; i < nodeIds.length; i++) {
         m.put(nodeIds[i], 0);
      }

      List<EvalGroupNodes> l = dao.findByProperties(EvalGroupNodes.class, 
            new String[] {"nodeId"}, 
            new Object[] {nodeIds});
      for (Iterator<EvalGroupNodes> iter = l.iterator(); iter.hasNext();) {
         EvalGroupNodes egn = (EvalGroupNodes) iter.next();
         m.put(egn.getNodeId(), egn.getEvalGroups().length);
      }
      return m;
   }

   public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId) {
      String nodeId = dao.getNodeIdForEvalGroup(evalGroupId);
      List<EvalHierarchyNode> l = new ArrayList<EvalHierarchyNode>();
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
      return l;
   }



   public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      throw new RuntimeException("Not implemented yet");
   }

   public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      throw new RuntimeException("Not implemented yet");
   }

   public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      throw new RuntimeException("Not implemented yet");
   }

   public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant) {
      // TODO Auto-generated method stub
      throw new RuntimeException("Not implemented yet");
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
    * Get an eval group node by a nodeid
    * @param nodeId
    * @return the {@link EvalGroupNodes} or null if none found
    */
   @SuppressWarnings("unchecked")
   private EvalGroupNodes getEvalGroupNodeByNodeId(String nodeId) {
      List<EvalGroupNodes> l = dao.findByProperties(EvalGroupNodes.class, 
            new String[] {"nodeId"}, new Object[] {nodeId},
            new int[] {ByPropsFinder.EQUALS}, new String[] {"id"});
      EvalGroupNodes egn = null;
      if (!l.isEmpty()) {
         egn = (EvalGroupNodes) l.get(0);
      }
      return egn;
   }

}
