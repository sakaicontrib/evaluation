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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.providers.EvalHierarchyProvider;


/**
 * This brings in the hierarchy information and gives us the ability to control the
 * hierarchy
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk), bjones86 (ExternalHierarchyRules)
 */
public interface ExternalHierarchyLogic extends EvalHierarchyProvider, ExternalHierarchyPermissions, ExternalHierarchyRules {

   /**
    * Determine what sections are attached (fall under) the given eval group ID.
    * The given eval group ID could be a single site, in which case all sections
    * attached to the site would be returned. The given eval group ID could also
    * be a single section (/site/<site_id>/section/<section_id>), in which case
    * only the single section would be returned.
    * 
    * @param evalGroupID
    * @return a list of Section objects that fall under the given eval group ID
    */
   public List<Section> getSectionsUnderEvalGroup( String evalGroupID );

   /**
    * Utility method to determine which qualifier was chosen based on the final rule text
    * 
    * @param ruleText - the final rule text from the DB
    * @return the qualifier used for the final rule text
    */
   public String determineQualifierFromRuleText( String ruleText );

   /**
    * Utility method to remove the qualifier from the final rule text and return only the raw rule text
    * 
    * @param ruleText - the final rule text from the DB
    * @return the raw rule text with qualifier removed
    */
   public String removeQualifierFromRuleText( String ruleText );

   /**
    * Add a new node to a hierarchy
    * 
    * @param parentNodeId the unique id for the parent of this node, can be null if this is the root or a top level node
    * @return the object representing the newly added node
    */
   public EvalHierarchyNode addNode(String parentNodeId);

   /**
    * Remove a node from the hierarchy if it is possible,
    * nodes can only be removed if they have no children associations,
    * root nodes can never be removed,
    * nodes which have evalgroups assigned to them cannot be removed,
    * exception occurs if these rules are violated
    * <br/>NOTE: this will remove associated template items from their association
    * with the node and place them back in the default level
    * 
    * @param nodeId a unique id for a hierarchy node
    * @return the object representing the parent of the removed node
    */
   public EvalHierarchyNode removeNode(String nodeId);

   /**
    * Save meta data on a node, this has to be done separately from creating a node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param title the title of the node
    * @param description a description for this node
    * @return the object representing the updated node
    */
   public EvalHierarchyNode updateNodeData(String nodeId, String title, String description);

   /**
    * Set this set to be the set of eval group ids associated with this node,
    * can also be used to clear the associated ids
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param evalGroupIds the unique IDs of eval groups to associate, if this is an
    * empty set then this will clear the associations so there are no groups associated
    */
   public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds);

   /**
    * Get all the children node ids which exist under the set of supplied nodes
    * @param nodes a set of eval hierarchy nodes
    * @param includeSuppliedNodeIds include the nodes ids of the supplied collection of nodes 
    * @return a set of all unique child nodes
    */
   public Set<String> getAllChildrenNodes(Collection<EvalHierarchyNode> nodes, boolean includeSuppliedNodeIds);

   /**
    * Create a sorted list of nodes based on a set of input nodes,
    * list goes from root (or highest parent) down to the bottom most node
    * 
    * @param nodes a collection of nodes
    * @return a list of {@link EvalHierarchyNode}
    */
   public List<EvalHierarchyNode> getSortedNodes(Collection<EvalHierarchyNode> nodes);

}
