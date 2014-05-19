/**
 * HierarchyRenderUtil.java - evaluation - Oct 29, 2007 11:35:56 AM - sgithens
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.tool.producers.ModifyHierarchyNodeGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHierarchyNodeProducer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;
import org.sakaiproject.evaluation.tool.viewparams.ModifyHierarchyNodeParameters;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/** 
 * Should be replaced by either an Evolver or a Real Producer of some sort.
 * 
 * Renders the drop down Hierarchy Node Selector on the Views for Modifying
 * template items.
 * 
 * @author Steven Githens (sgithens@caret.cam.ac.uk)
 */
public class HierarchyRenderUtil {

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    @SuppressWarnings("unused")
    private EvalGroupsProvider groupProvider;
    public void setEvalGroupsProvider(EvalGroupsProvider provider) {
        groupProvider = provider;
    }

    /* 
     * This is currently for the select checkboxes in the new eval wizard.
     */
    // private HierarchyRowRenderer hierarchyRowRenderer;
    // public void setHierarchyRowRenderer(HierarchyRowRenderer renderer) {
    //     hierarchyRowRenderer = renderer;
    // }

    /**
     * This renders a view of the hierarchy that allows modification and addition of
     * nodes.  Operations include adding, modifying, removing, and assigning groups
     * to nodes.  Currently used in the ControlHierarchyProducer view.
     * 
     * @param parent
     * @param clientID
     * @param showCheckboxes if true then the checkboxes are rendered in front of every node
     * @param showGroups if true then all the groups are rendered for each node
     * @param showUsers if true then all users are rendered for each node
     */
    public void renderModifyHierarchyTree(UIContainer parent, String clientID, boolean showCheckboxes, boolean showGroups, boolean showUsers) {
        UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");
        
        translateTableHeaders(joint);
        
        //Hidden header for column with metadata information.
        UIOutput.make(parent, "node-metadata-header");

        // get the root node and the counts of all assigned groups for all nodes
        EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
        String[] allChildrenNodeIds = root.childNodeIds.toArray(new String[root.childNodeIds.size()]);
        Map<String, Set<String>> groupsNodesMap = hierarchyLogic.getEvalGroupsForNodes(allChildrenNodeIds);
        // get all nodes at once for use in rendering the hierarchy
        Set<EvalHierarchyNode> allChildren = hierarchyLogic.getChildNodes(root.id, false);
        Map<String, EvalHierarchyNode> nodeIdToNode = new HashMap<String, EvalHierarchyNode>();
        nodeIdToNode.put(root.id, root);
        for (EvalHierarchyNode node : allChildren) {
            nodeIdToNode.put(node.id, node);
        }

        //showGroups = true;
        renderHierarchyNode(joint, root, 0, groupsNodesMap, nodeIdToNode, showGroups, showUsers);
    }

    //   private void renderSelectHierarchyGroup(UIContainer tofill, String groupID, int level, Set<String> evalGroupIDs, String clientID) {
    //      UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
    //      UIOutput.make(tableRow, "node-select-cell");
    //      //UISelectChoice.make(tableRow, "select-checkbox", clientID, evalGroupIDs.size());
    //      UIBoundBoolean.make(tableRow, "select-checkbox");
    //      evalGroupIDs.add(groupID);
    //      UIOutput name = UIOutput.make(tableRow, "node-name", commonLogic.getDisplayTitle(groupID));
    //      name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));
    //   }

    /**
     * Render this particular HierarchyNode indented at the level.
     * 
     * Leaf nodes get a Remove button and the ability to have groups assigned
     * to them.
     * 
     * @param tofill
     * @param node
     * @param level
     * @param groupsNodesMap this is simply passed along to avoid redoing the same query over and over
     * @param nodeIdToNode this is passed along to avoid pummeling the database when rendering the hierarchy
     * @param showGroups if true then show the groups, otherwise only show counts
     * @param showUsers if true then show the users, otherwise just show the link
     */
    private void renderHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level, 
            Map<String, Set<String>> groupsNodesMap, Map<String, EvalHierarchyNode> nodeIdToNode, 
            boolean showGroups, boolean showUsers) {
        String title = node.title != null ? node.title : "Null Title?";
        UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");

        // Node Metadata
        UIOutput.make(tableRow, "node-metadata-cell");
        UIInput.make(tableRow, "node-id-input", null, node.id);
/** http://jira.sakaiproject.org/jira/browse/EVALSYS-631
        if (node.parentNodeIds.size() > 0) {
            // TODO In the future there might be multiple parents...
            for (Iterator<String> itr = node.parentNodeIds.iterator(); itr.hasNext();) {
                UIInput.make(tableRow, "parent-id-input", null, itr.next());
            }
        } else {
            UIInput.make(tableRow, "parant-id-input", null, "NO_PARENT");
        }
**/

        // not rendering the node-select cell right now
        //UIOutput.make(tableRow, "node-select-cell");

        UIOutput name = UIOutput.make(tableRow, "node-name", title);
        name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));

        UIOutput.make(tableRow, "nodes-cell");
        UIOutput.make(tableRow, "groups-cell");
        UIOutput.make(tableRow, "users-cell");

        // If this node has groups assigned to it, we should not be able to add sub-nodes.
        int numberOfAssignedGroups = groupsNodesMap.get(node.id) != null ? groupsNodesMap.get(node.id).size() : 0;
        if (numberOfAssignedGroups <= 0) {
            UIInternalLink.make(tableRow, "add-child-link", UIMessage.make("controlhierarchy.add"),
                    new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, true));
        }
        UIInternalLink.make(tableRow, "modify-node-link", UIMessage.make("controlhierarchy.modify"),
                new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, false));
        // If the node has children, render the number of children, but no remove button.
        int childrenNodesSize = node.directChildNodeIds.size();
        UIOutput.make(tableRow, "number-children", childrenNodesSize + "");
        if (childrenNodesSize <= 0) { 
            // no children nodes
            if (numberOfAssignedGroups <= 0) {
                // remove node if no groups are assigned
                UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
                UICommand removeButton = UICommand.make(removeForm, "remove-node-button", UIMessage.make("controlhierarchy.remove"));
                removeButton.parameters.add(new UIDeletionBinding("hierNodeLocator."+node.id));
            }

            // assigned groups
            UIInternalLink.make(tableRow, "assign-groups-link", UIMessage.make("controlhierarchy.assigngroups"), 
                    new HierarchyNodeParameters(ModifyHierarchyNodeGroupsProducer.VIEW_ID, node.id));
            UIOutput.make(tableRow, "assign-group-count", numberOfAssignedGroups+"");
        }

        // assigned users (permissions)
        UIInternalLink.make(tableRow, "assign-users-link", UIMessage.make("controlhierarchy.assignusers"), 
                new HierarchyNodeParameters(ModifyHierarchyNodeGroupsProducer.VIEW_ID, node.id));

        // if show users is on then we show the full list of all users with perms in this node
        if (showUsers && numberOfAssignedGroups > 0) {
            // TODO hierarchyLogic.get
        }

        // If there are any assigned groups, render them as their own rows if show is on
        if (showGroups && numberOfAssignedGroups > 0) {
            Set<String> assignedGroupIDs = groupsNodesMap.get(node.id) != null ? groupsNodesMap.get(node.id) : new HashSet<String>();
            for (String assignedGroupID: assignedGroupIDs) {
                EvalGroup assignedGroup = commonLogic.makeEvalGroupObject(assignedGroupID);
                UIBranchContainer groupRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
                UIOutput.make(groupRow, "node-metadata-cell");
                UIOutput groupName = UIOutput.make(groupRow, "row-data", assignedGroup.title + " ("+assignedGroup.evalGroupId+") ["+assignedGroup.type+"]");
                groupName.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*4) + "em") ));
            }
        }

        // now render all direct children
        for (String childId : node.directChildNodeIds) {
            EvalHierarchyNode childNode = nodeIdToNode.get(childId);
            if (childNode != null) {
                renderHierarchyNode(tofill, childNode, level+1, groupsNodesMap, nodeIdToNode, showGroups, showUsers);
            }
        }
    }
    
    /**
     * Translate the table headers and any other decorations on or around the
     * table.
     * 
     * @param tofill
     */
    public void translateTableHeaders(UIContainer tofill) {
       UIMessage.make(tofill, "hierarchy-header", "controlhierarchy.table.hierarchy.header");
       UIMessage.make(tofill, "hierarchy-nodes", "controlhierarchy.table.nodes.header");
       UIMessage.make(tofill, "hierarchy-groups", "controlhierarchy.table.groups.header");
       UIMessage.make(tofill, "hierarchy-users", "controlhierarchy.table.users.header");
 //      UIMessage.make(tofill, "assign-groups-header", "controlhierarchy.table.assigngroups.header");
 //      UIMessage.make(tofill, "assigned-group-count-header", "controlhierarchy.table.groupcount.header");
       
    }

}
