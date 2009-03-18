package org.sakaiproject.evaluation.tool.renderers;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/**
 * At the moment, this is strictly for rendering the tree of hierarchy nodes
 * ,with potential evalgroup leaves, in the assign page of the create evaluation
 * wizard.  This is going in it's own class for clarity because it requires
 * several recursive methods.
 * 
 * This class should not be used as a singleton, it has instance fields.
 * 
 * @author Steven Githens
 */
public class HierarchyTreeNodeSelectRenderer {

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
       this.hierarchyLogic = logic;
    }
    
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
       this.commonLogic = commonLogic;
    }
    
    String groupsSelectID; 
    String hierNodesSelectID;
    List<String> evalGroupLabels; 
    List<String> evalGroupValues;
    List<String> hierNodeLabels; 
    List<String> hierNodeValues;
    
    
   /**
    * This is the main entry point for rendering the hierarchy with selectable
    * checkboxes.  The parent container should be inside whatever form is being
    * used. The groups and nodes SelectID's are the getFullID's that were generated
    * from the UISelect. We're doing a GET form here so there are no EL Bindings
    * for these checkboxes.  The Lists of things are used for storing the option
    * label/value at each point.  These are live references that are passed in 
    * and used after this method invocation to update the UISelect.optionslist
    * and UISelect.optionnames for the Hierarchy Nodes and Group Nodes.
    * 
    * From a visual perspective, the Hierarchy and Group Nodes are interspersed
    * together throughout the page, but we keep them seperate with the two 
    * UISelects.
    * 
    * @param parent
    * @param clientID
    * @param groupsSelectID
    * @param hierNodesSelectID
    * @param evalGroupLabels
    * @param evalGroupValues
    * @param hierNodeLabels
    * @param hierNodeValues
    */
   public void renderSelectHierarchyNodesTree(UIContainer parent, String clientID, 
            String groupsSelectID, String hierNodesSelectID,
            List<String> evalGroupLabels, List<String> evalGroupValues,
            List<String> hierNodeLabels, List<String> hierNodeValues) {
       this.groupsSelectID = groupsSelectID;
       this.hierNodesSelectID = hierNodesSelectID;
       this.evalGroupLabels = evalGroupLabels;
       this.evalGroupValues = evalGroupValues;
       this.hierNodeLabels = hierNodeLabels;
       this.hierNodeValues = hierNodeValues;
        
       UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");

       UIMessage.make(joint, "node-select-header", "controlhierarchy.table.selectnode.header");
       UIMessage.make(joint, "hierarchy-header", "controlhierarchy.table.heirarchy.header");

       EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();

       renderSelectHierarchyNode(joint, root, 0);
    }
    

   /**
    * Performs the recursive rendering logic for a single hierarchy node.
    * 
    * @param tofill
    * @param node
    * @param level
    */
   private void renderSelectHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level ) {
       renderRow(tofill, "hierarchy-level-row:", level, node);
       
       Set<String> groupIDs = hierarchyLogic.getEvalGroupsForNode(node.id);
       for (String groupID: groupIDs) {
           EvalGroup evalGroupObj = commonLogic.makeEvalGroupObject(groupID);
           renderRow(tofill, "hierarchy-level-row:", level+1, evalGroupObj);
       }
       
       for (String childNodeID: node.directChildNodeIds) {
           EvalHierarchyNode childHierNode = hierarchyLogic.getNodeById(childNodeID);
           renderSelectHierarchyNode(tofill, childHierNode, level+1);
       }
       
    }
    
   /**
    * Renders a single row, which could either be a hierarchy node or eval group
    * with a checkbox.
    *
    * @param parent
    * @param clientID
    * @param level
    * @param toRender
    */
   private void renderRow(UIContainer parent, String clientID, int level, Object toRender) {
        UIBranchContainer tableRow = UIBranchContainer.make(parent, "hierarchy-level-row:");
        
        UIOutput.make(tableRow, "node-select-cell");
        
        String title = "";
        if (toRender instanceof EvalHierarchyNode) {
            EvalHierarchyNode evalHierNode = (EvalHierarchyNode) toRender;
            title = evalHierNode.title;
          
            hierNodeLabels.add(title);
            hierNodeValues.add(evalHierNode.id);
            UISelectChoice.make(tableRow, "select-checkbox",
                hierNodesSelectID, hierNodeLabels.size()-1);
        }
        else if (toRender instanceof EvalGroup) {
            EvalGroup evalGroup = (EvalGroup) toRender;
            title = evalGroup.title;
            
            evalGroupLabels.add(title);
            evalGroupValues.add(evalGroup.evalGroupId);
            
            UISelectChoice.make(tableRow, "select-checkbox",
                groupsSelectID, evalGroupLabels.size()-1);
        }
        else {
            title = "";
        }
        UIOutput name = UIOutput.make(tableRow, "node-name", title);
        name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));
    }
}