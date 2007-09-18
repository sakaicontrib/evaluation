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

import java.util.Iterator;
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
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
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
    
    /*
     * This renders a view of the hierarchy that allows modification and addition of
     * nodes.  Operations include adding, modifying, removing, and assigning groups
     * to nodes.  Currently used in the ControlHierarchyProducer view.
     * 
     * @param parent
     * @param clientID
     */
    public void renderModifyHierarchyTree(UIContainer parent, String clientID) {
        UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");
        
        translateTableHeaders(joint);
        
        /*
         * Hidden header for column with metadata information.
         */
        UIOutput.make(parent, "node-metadata-header");
        
        EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
        renderHierarchyNode(joint, root, 0);
    }
    
    public void renderSelectHierarchyNodesTree(UIContainer parent, String clientID, String elbinding) {
        UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");

        UIMessage.make(joint, "node-select-header", "controlhierarchy.table.selectnode.header");
        UIMessage.make(joint, "hierarchy-header", "controlhierarchy.table.heirarchy.header");
        
        EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
        
        UISelect siteCheckboxes = UISelect.makeMultiple(joint, "selectColumnCheckboxes", new String[] {}, elbinding, null);
        String selectID = siteCheckboxes.getFullID();
        List<String> checkboxValues = new ArrayList<String>();
        
        renderSelectHierarchyNode(joint, root, 0, selectID, checkboxValues);
        
        String[] ids = checkboxValues.toArray(new String[]{});
        siteCheckboxes.optionlist = siteCheckboxes.optionnames = UIOutputMany.make(ids);
    }
    
    private void renderSelectHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level, String selectID, List<String> checkboxValues) {
        String title = node.title != null ? node.title : "Null Title?";
        UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
        UIOutput.make(tableRow, "node-select-cell");
        UISelectChoice checkbox = UISelectChoice.make(tableRow, "select-checkbox", selectID, checkboxValues.size());
        checkboxValues.add(node.id);
        UIOutput name = UIOutput.make(tableRow, "node-name", title);
        Map attr = new HashMap();
        attr.put("style", "text-indent:" + (level*2) + "em");
        name.decorate(new UIFreeAttributeDecorator(attr));
        
        for (String childId : node.directChildNodeIds) {
            renderSelectHierarchyNode(tofill, hierarchyLogic.getNodeById(childId), level+1, selectID, checkboxValues);
        }
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

       String title = node.title != null ? node.title : "Null Title?";
       UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
       UIOutput name = UIOutput.make(tableRow, "node-name", title);
       Map attr = new HashMap();
       attr.put("style", "text-indent:" + (level*2) + "em");
       name.decorate(new UIFreeAttributeDecorator(attr));
       
       /* Node Metadata */
       UIOutput.make(tableRow, "node-metadata-cell");
       UIInput.make(tableRow, "node-id-input", null, node.id);
       if (node.parentNodeIds.size() > 0) {
           // TODO In the future there might be multiple parents...
           for (Iterator<String> itr = node.parentNodeIds.iterator(); itr.hasNext();) {
               UIInput.make(tableRow, "parent-id-input", null, itr.next());
           }
       }
       else {
           UIInput.make(tableRow, "parant-id-input", null, "NO_PARENT");
       }
       
       UIOutput.make(tableRow, "add-child-cell");
       UIInternalLink.make(tableRow, "add-child-link", UIMessage.make("controlhierarchy.add"),
               new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, true));
       UIOutput.make(tableRow, "modify-node-cell");
       UIInternalLink.make(tableRow, "modify-node-link", UIMessage.make("controlhierarchy.modify"),
               new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, false));

       /*
        * If the node has children, render the number of children, but no remove button
        * or assign groups link.
        */
       if (node.directChildNodeIds.size() > 0) {
          UIOutput.make(tableRow, "child-info-cell");
          UIOutput.make(tableRow, "number-children", node.directChildNodeIds.size() + "");
       } 
       else {
          UIOutput.make(tableRow, "remove-node-cell");
          UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
          UICommand removeButton = UICommand.make(removeForm, "remove-node-button", UIMessage.make("controlhierarchy.remove"));
          removeButton.parameters.add(new UIDeletionBinding("hierNodeLocator."+node.id));
          UIOutput.make(tableRow, "assign-groups-cell");
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
    
    /*
     * Populates the values and labels that need to be bound to the drop down combo box.
     * The first time in pass an empty array for both.  When we see that it's empty we will
     * add the toplevel and all the children under the root, because the root node doesn't really
     * need to be in there. (This may change if the UI Designer says otherwise.
     * 
     * It should end up looking like the following:
     * + Top Level
     * + School of Something
     *   - Department Blah
     * + School of Another Thing  
     * 
     * etc.
     */
    private void populateHierSelectLists(List<String> values, List<String> labels, int level, EvalHierarchyNode node) {
        if (values.size() == 0) {
            values.add(EvalConstants.HIERARCHY_NODE_ID_NONE);
            labels.add("Top Level");
            EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
            for (String childId: root.directChildNodeIds) {
                EvalHierarchyNode rootChild = hierarchyLogic.getNodeById(childId);
                populateHierSelectLists(values, labels, 0, rootChild);
            }
        }
        else {
            values.add(node.id);
            String label = node.title;
            for (int i = 0; i < level; i++) {
                label = "." + label;
            }
            labels.add(label);
            for (String childId: node.directChildNodeIds) {
                populateHierSelectLists(values, labels, level+2, hierarchyLogic.getNodeById(childId));
            }
        }
    }
    
    /*
     * Translate the table headers and any other decorations on or around the
     * table.
     * 
     * @param tofill
     */
    public void translateTableHeaders(UIContainer tofill) {
        UIMessage.make(tofill, "hierarchy-header", "controlhierarchy.table.heirarchy.header");
        UIMessage.make(tofill, "add-item-header", "controlhierarchy.table.additem.header");
        UIMessage.make(tofill, "modify-item-header", "controlhierarchy.table.modifyitem.header");
        UIMessage.make(tofill, "items-level-header", "controlhierarchy.table.itemslevel.header");
        UIMessage.make(tofill, "assign-groups-header", "controlhierarchy.table.assigngroups.header");
    }
}
