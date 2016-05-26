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
package org.sakaiproject.evaluation.tool.renderers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UISelect;

/*
 * Render a selector which allows a user to choose a single node in the hierarchy
 */
public class HierarchyNodeSelectorRenderer {

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   /**
    * This identifies the template component associated with this renderer
    */
   public static final String COMPONENT_ID = "render-hierarchy-node-selector:";

   /**
    * Render a hierarchy node selector control
    * @param parent any RSF {@link UIContainer} object which will contain the rendered item
    * @param ID the (RSF) ID of this component
    * @param elBinding EL expression to be used as the value binding for the contained String value
    * @param nodeId the unique id for a hierarchy node, this node will be treated as the root node for the selector,
    * if this is null then the hierarchy root node will be used
    * @return a {@link UIJointContainer} which has been populated with the render data
    */
   public UIJointContainer renderHierarchyNodeSelector(UIContainer parent, String ID, String elBinding, String nodeId) {
      UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

      List<String> hierSelectValues = new ArrayList<>();
      List<String> hierSelectLabels = new ArrayList<>();

      // first add in the top level as a choice
      hierSelectValues.add(EvalConstants.HIERARCHY_NODE_ID_NONE);
      hierSelectLabels.add("Top Level"); // TODO - make this i18n

      // now add in the nodes starting with the passed in node or the root node
      EvalHierarchyNode currentRoot = null;
      if (nodeId != null && !nodeId.equals("")) {
         currentRoot = hierarchyLogic.getNodeById(nodeId);
      }

      if (currentRoot == null) {
         currentRoot = hierarchyLogic.getRootLevelNode();
      }
      populateHierSelectLists(hierSelectValues, hierSelectLabels, 0, currentRoot);

      // now render the control
      UISelect.make(parent, "hierarchyNodeSelect",
            hierSelectValues.toArray(new String[]{}), 
            hierSelectLabels.toArray(new String[]{}),
            elBinding);

      return container;
   }

   /**
    * Recursively populates the values and labels that need to be bound to the drop down combo box.
    * 
    * It should end up looking like the following:
    * + Top Level
    * + Hierarchy Root
    *   + School of Something
    *     - Department Blah
    *   + School of Another Thing  
    * 
    * etc.
    * 
    * @param values
    * @param labels
    * @param level
    * @param node
    */
   private void populateHierSelectLists(List<String> values, List<String> labels, int level, EvalHierarchyNode node) {
      // TODO make this more efficient somehow
      values.add(node.id);
      String label = node.title;
      for (int i = 0; i < level; i++) {
         label = "." + label;
      }
      labels.add(label);
      for (String childId : node.directChildNodeIds) {
         populateHierSelectLists(values, labels, level + 2, hierarchyLogic.getNodeById(childId));
      }
   }

}