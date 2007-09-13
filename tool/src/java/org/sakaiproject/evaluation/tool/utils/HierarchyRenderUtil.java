package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.ModifyHierarchyNodeGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHierarchyNodeProducer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;
import org.sakaiproject.evaluation.tool.viewparams.ModifyHierarchyNodeParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/* Should be replaced by either an Evolver or a Real Producer of some
 * sort.
 * 
 * Renders the drop down Hiearchy Node Selector on the Views for Modifying
 * template items.
 */
public class HierarchyRenderUtil {
    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }
    
    public void renderModifyHierarchyTree(UIContainer parent, String clientID) {
        UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");
        EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
        renderHierarchyNode(joint, root, 0);
    }
    
    /*
     * Render this particular HierarchyNode indented at the level.
     * 
     * Leaf nodes get a Remove button and the ability to have groups assigned
     * to them.
     * 
     * @param tofill
     * @param node
     * @param level
     */
    private void renderHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level) {
       System.out.println("Node: " + node);

       String title = node.title != null ? node.title : "Null Title?";
       UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
       UIOutput name = UIOutput.make(tableRow, "node-name", title);
       Map attr = new HashMap();
       attr.put("style", "text-indent:" + (level*2) + "em");
       name.decorate(new UIFreeAttributeDecorator(attr));
       UIInternalLink.make(tableRow, "add-child-link", UIMessage.make("controlhierarchy.add"),
               new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, true));
       UIInternalLink.make(tableRow, "modify-node-link", UIMessage.make("controlhierarchy.modify"),
               new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, false));

       /*
        * If the node has children, render the number of children, but no remove button
        * or assign groups link.
        */
       if (node.directChildNodeIds.size() > 0) {
          UIOutput.make(tableRow, "number-children", node.directChildNodeIds.size() + "");
       } 
       else {
          UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
          UICommand removeButton = UICommand.make(removeForm, "remove-node-button", UIMessage.make("controlhierarchy.remove"));
          removeButton.parameters.add(new UIDeletionBinding("hierNodeLocator."+node.id));
          UIInternalLink.make(tableRow, "assign-groups-link", UIMessage.make("controlhierarchy.assigngroups"), 
                  new HierarchyNodeParameters(ModifyHierarchyNodeGroupsProducer.VIEW_ID, node.id));
       }

       for (String childId : node.directChildNodeIds) {
          renderHierarchyNode(tofill, hierarchyLogic.getNodeById(childId), level+1);
       }
    }
    
    public void makeHierSelect(UIForm form, String selectID, String elBinding) {
        List<String> hierSelectValues = new ArrayList<String>();
        List<String> hierSelectLabels = new ArrayList<String>();
        populateHierSelectLists(hierSelectValues, hierSelectLabels, 0, null);
        UISelect.make(form, selectID,
                hierSelectValues.toArray(new String[]{}), 
                hierSelectLabels.toArray(new String[]{}),
                elBinding, null);
    }
    
    private void populateHierSelectLists(List<String> values, List<String> labels, int level, EvalHierarchyNode node) {
        if (values.size() == 0) {
            values.add(EvalConstants.HIERARCHY_NODE_ID_NONE);
            labels.add("Top Level");
            populateHierSelectLists(values, labels, 0, null);
        }
        else {
            if (level == 0 && node == null) {
                EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
                for (String childId: root.childNodeIds) {
                    populateHierSelectLists(values, labels, 0, hierarchyLogic.getNodeById(childId));
                }
            }
            else {
                values.add(node.id);
                String label = node.title;
                for (int i = 0; i < level; i++) {
                    label = "." + label;
                }
                labels.add(label);
                for (String childId: node.childNodeIds) {
                    populateHierSelectLists(values, labels, level+2, hierarchyLogic.getNodeById(childId));
                }
            }
        }
    }
}
